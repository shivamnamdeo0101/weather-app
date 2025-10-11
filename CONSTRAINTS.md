
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

Notes:
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
