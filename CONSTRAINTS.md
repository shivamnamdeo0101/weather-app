
##   GLOBAL WEATHER APP - ARCHITECTURE & STRATEGY


---
### Redis Memory & Scaling

We have around 2 lakh cities (MAX_CITIES_OPEN_WEATHER = 200,000)  
Each city in Redis will take ~8 KB (PER_CITY_DATA_SIZE_IN_REDIS)  

Total Redis memory we need globally = 200,000 * 8 KB = 1.6 GB  
If we deploy in 5 regions, that comes to around 327.68 MB per region.  
This is easily manageable in Redis, even in small nodes.

CONSTRAINTS:
- MAX_CITIES_OPEN_WEATHER = 200,000
- PER_CITY_DATA_SIZE_IN_REDIS = 8 KB
- MAX_REDIS_MEMORY = 1.6 GB GLOBALLY
- PER_REGION_REDIS_MEMORY ≈ 327.68 MB (IF 5 REGIONS)

---
### Redis Memory Planning Per Region

- Most Active (~5k cities × 8 KB) = 40 MB
- Medium (~13k cities × 8 KB) = 104 MB
- Low (~22k cities × 8 KB) = 176 MB
- Total ≈ 320 MB = Fits nicely within 327 MB per region

- ✅ This ensures high hit ratio for most-used cities, while keeping memory under control.
- ✅ Low-demand cities don’t occupy Redis unnecessarily; TTL handles expiry.

---
### Regional Deployment Strategy

| Continent    | Central AWS Region                  | Reason / Coverage                                                             |
|-------------|-----------------------------------|----------------------------------------------------------------------------------|
| Asia        | Singapore (ap-southeast-1)        | Covers India, China, Japan, SE Asia – centrally located, low latency             |
| Europe      | Frankfurt (eu-central-1)          | Best for Western, Central, Southern Europe                                       |
| Africa      | South Africa (af-south-1)         | Covers Southern Africa, optional northern coverage via Europe                    |
| Americas    | US East (us-east-1), São Paulo    | North & South America are far apart – need 2 regions                             |
| Oceania     | Sydney (ap-southeast-2)           | Covers Australia & New Zealand                                                   |

- **Notes:**
- Regions without users (Antarctica, remote islands) can just rely on CloudFront edge caches.
- Each region has 2–6 Availability Zones (AZs) for HA.
- Central AWS region per continent selected based on population density & latency.

---
### Traffic Flow Example

1. User in Singapore opens the site.  
2. CloudFront serves video/image from nearest edge node.  
3. API call goes to api.weatherapp.com.  
4. Route 53 figures out user is in Asia → sends request to BE_ASIA.  
5. Backend checks Redis cache → if miss, hits OpenWeather API → stores in Redis → returns JSON.

| Region                    | Total Cities | HOT Cities (Frequent Access) | MEDIUM Cities (Moderate Access) | LOW Cities (Infrequent Access) | Notes / Examples                                                                                                                                                                                                   |
| ------------------------- | ------------ | ---------------------------- | ------------------------------- | ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **North & South America** | 40,000       | 10% (~4,000)                 | 25% (~10,000)                   | 65% (~26,000)                  | HOT: New York, Los Angeles, Toronto, São Paulo, Rio de Janeiro. Cities with high population density, tech adoption, and urban lifestyle. Medium: mid-size US/Brazil/Canada cities. Low: small towns / rural areas. |
| **Europe**                | 40,000       | 12% (~4,800)                 | 28% (~11,200)                   | 60% (~24,000)                  | HOT: London, Paris, Berlin, Madrid, Milan. Highly urbanized cities with active weather API usage. Medium: medium-size EU cities. Low: rural towns, low-density regions.                                            |
| **Asia**                  | 40,000       | 8% (~3,200)                  | 27% (~10,800)                   | 65% (~26,000)                  | HOT: Mumbai, Delhi, Tokyo, Singapore, Shanghai. Mega-cities with high digital adoption. Medium: Tier 2 cities. Low: smaller towns, villages, low app usage.                                                        |
| **Africa**                | 40,000       | 5% (~2,000)                  | 15% (~6,000)                    | 80% (~32,000)                  | HOT: Johannesburg, Cairo, Lagos. Main urban centers with digital connectivity. Medium: mid-size cities. Low: rural regions with low API usage.                                                                     |
| **Oceania**               | 40,000       | 7% (~2,800)                  | 23% (~9,200)                    | 70% (~28,000)                  | HOT: Sydney, Melbourne, Auckland. Medium: other urban areas. Low: small towns and rural areas.                                                                                                                     |

---
### Load Calculation

| Metric                               |        Calculation |               Result | Description                                                                                 |
| ------------------------------------ | -----------------: | -------------------: | ------------------------------------------------------------------------------------------- |
| Cities per region                    |                  — |           **40,000** | America split into 2 regions, each region has 40k cities                                    |
| Cities per category (≈1/3 split)     | 40,000 ÷ 3 → round | **≈13,333 → 14,000** | Approximate equal split into HOT/MEDIUM/LOW buckets (rounded)                               |
| Most-active set (10% of category)    |       14,000 × 10% |            **1,400** | Top 10% of the category considered “most active”                                            |
| HOT candidates (10% of active)       |        1,400 × 10% |              **140** | Cities reaching HOT threshold (e.g., ≥140 hits)                                             |
| MEDIUM candidates (5% of active)     |         1,400 × 5% |               **70** | Cities reaching MEDIUM threshold (e.g., ≥70 hits)                                           |
| Total refresh-triggering cities      |           140 + 70 |              **210** | Cities that will trigger refresh in the cycle                                               |
| Average sustained refresh per minute |            210 ÷ 2 |      **105 req/min** | Approximate sustained refresh rate (210 spread over ~2 minutes of effective refresh window) |

- **Notes**
- Arithmetic shown exactly — 210 ÷ 2 = 105 requests/min (rounded) direct schedular calls to SVC 
- Schedular run every 15 mins if hits > THRESHOLD_HITS for HOT(hit>=140) and MEDIUM(hit>= 70)

| Metric / Scenario                              | Value / Calculation                           | Notes                                                                                    |
| ---------------------------------------------- | --------------------------------------------- | ---------------------------------------------------------------------------------------- |
| **Total HOT+MEDIUM active cities**             | 1,400                                         | Top 10% of the 14,000 most active cities                                                 |
| **Scheduler target (top HOT 10%)**             | 140                                           | Only 10% of 1,400 are refreshed by scheduler                                             |
| **SVC quota**                                  | 150 req/min                                   | Total allowed per region                                                                 |
| **Scheduler usage**                            | 105 req/min                                   | Refreshing 140 HOT cities                                                                |
| **Remaining quota for user cache misses**      | 150 − 105 = 45 req/min                        | Available for occasional user-triggered misses                                           |
| **Avg call duration**                          | 500 ms (0.5 sec)                              | Each SVC request takes ~0.5 sec                                                          |
| **Time to refresh 140 cities sequentially**    | 140 × 0.5 sec = 70 sec                        | ~1 min 10 sec if done back-to-back without stagger                                       |
| **Time to refresh 140 cities with stagger**    | 140 × (0.5 + avg_stagger) sec                 | If we stagger ~0.5 sec between threads → total ~2 min                                    |
| **Impact on user requests**                    | Limited to 45 req/min during scheduler window | Scheduler + user requests should not exceed 150 req/min                                  |
| **Cache hit expectation**                      | 90% of traffic comes from HOT 140 cities      | Most user requests served from cache, minimal SVC load                                   |
| **Remaining HOT/MEDIUM (1,400 − 140 = 1,260)** | Only hit SVC if user requests happen          | Scheduler does not refresh these; traffic is low and handled from cache or SVC as needed |



#### Assumptions Used

- **HOT Cities:**
    - Population > 1M, high internet penetration, frequent app usage.
    - Cities are tech-forward, likely to make multiple API calls per hour.

- **MEDIUM Cities:**
    - Population 0.1M – 1M, moderate internet/tech adoption.
    - Weather API calls once every few hours.

- **LOW Cities:**
    - Small towns, villages, rural areas.
    - Rare access; TTL or on-demand fetch only.

- **Notes**
    - Percentages reflect practical load for caching + scheduler planning, not fixed Redis storage.
    - Scheduler staggering + lastAccess checks ensure not all HOT/Medium cities hit API at once — reduces spike risk.

---
### AWS Services Role

| Service    | Role                                                           |
|------------|----------------------------------------------------------------|
| CloudFront | Caches static JS/CSS/images globally – fast page load          |
| Route 53   | Latency-based DNS – sends API traffic to nearest backend       |
| Redis      | Regional cache for city weather – avoids unnecessary API calls |
| EC2/ECS    | Backend API servers – handle dynamic requests / SSR if needed  |


---
### Why This Setup Makes Sense

- Global scale: Each continent has one main AWS region.  
- Low latency: CloudFront + Route 53 ensures fast response.  
- Efficient caching: Redis ~328 MB per region is enough.  
- High availability: Multi-AZ setup, independent regional deployment.  
- Easy to scale: Add regions or Redis nodes if user base grows.

---
