# 🌦️ Weather Prediction Microservice – Reference

## 1. Project Overview
**Title:** Weather Prediction Microservice  
**Objective:** This project provides a high-performance, cache-enabled weather forecasting system. It uses Spring Boot for backend APIs, 
Redis for caching frequently (on-demand, periodically scheduled) eventually consistent accessed data, and the OpenWeather API for real-time weather information.
Built with microservices architecture, it implements rate limiting, cache-aside pattern, and LRU/LFU strategies for optimal efficiency and scalability.

### CONSTRAINTS

#### Memory Constraints
- Max cities globally: 200,000 (OPEN-WEATHER-API)
- Per-city Redis size: 8 KB + 0.12 KB metadata
- Total Redis memory (global): ~1.624 GB → rounded ~2 GB
- Per-region Redis memory (6 regions): ~300–400 MB
- HOT/Medium/Low city split per region (~40k cities)
- LOW cities use TTL / on-demand fetch to save memory

#### Load / Traffic Constraints
- 90% traffic from Most active cities only that is 1400 avg (40k / 3 City Category (based on tech adoptions/app uses/more constraints)) of 10 %.
- City constraints basis 10%(140) req/per min will hit SVC  which we already refreshing through schedular and on-demand strategy.
- On-Demand refresh + Periodic schedule for load reduction for User req + schedular calls
- Scheduler thresholds: HOT ACTIVE ≥140 hits, MEDIUM ACTIVE ≥70 hits
- Scheduler frequency: every 15 minutes
- Backend SVC quota per region: 150 req/min
- Avg SVC API call duration: 0.5 sec
- Scheduler refresh load: 105 req/min → leaves 45 req/min for user requests
- Cache hit target: ~90% from HOT city cache
- Staggering, lastAccess, lastRefresh checks to avoid spikes
- Locally tested with 500 req/sec throughput

 
## 2. Features
- Input city → get 3-day forecast  
- Predictions:
  - Rain → "Carry umbrella"  
  - Temp >40°C → "Use sunscreen lotion"  
  - Wind >10 mph → "Too windy!"  
  - Thunderstorm → "Don't step out!"  
- Offline mode / OpenweatherAPI fallback  - last 1 hours of redis data

### Data Flow

- **CAHCHE HIT/MISS/ON-DEMAND REFRESH Flow:**
  - Frontend ->  hits -> Weather-Cache service
  - Weather-Cache -checks Redis for city data
    - ✅ If found → return data immediately and also check ON-DEMAND needed with abstract strategies then refresh from SVC
    - ❌ If not found → call Weather-SVC
  - Weather-SVC -> Check if (rate limiter allowed 60 calls per min) then calls external OpenWeather API, then SVC put the data into cache then return data on FE

- **SCHEDULAR Flow:**
  - The **Weather Cache Scheduler** inside `weather-cache`  manages cached weather data in Redis efficiently, balancing **freshness** and **performance**. It monitors city-level cache entries using **hit counts** , **lastRefresh** and **last access time**.
  - Scheduler runs **every 15 min** to evaluate all cities.
  - **Hot(Most Active) Cities:** `hits ≥ 140` → Most active refreshed latest weather every 20 min and reset hits
  - **Medium Active Cities:** `70 ≤ hits <= 140` → refreshed latest weather every 30 min and reset hits.
  - **Low Cities**:** Eviction / remove record if no lastAccess in last 1 hour.

### Diagrams

<img width="2153" height="723" alt="AWS_ARCH-_Global_Weather_App__AWS_Multi_Region_Unified_Pod_Architecture" src="https://github.com/user-attachments/assets/44680b12-9249-486e-85e0-4722ec8a5c9b" />

<img width="1261" height="1017" alt="SequenceAPIDig-Weather_Forecast_Request_Flow__with_Rate_Limiter___Status_Codes_" src="https://github.com/user-attachments/assets/a57d5f2d-0696-465e-9e8a-8dd0e2f25bfe" />

<img width="1007" height="1668" alt="SequnceSchedularDig-Weather_Cache_Scheduler__Virtual_Threaded_Refresh___Eviction_Flow" src="https://github.com/user-attachments/assets/c98ca4c5-8986-47f5-9cbb-1b1447c86c68" />

## 3. Technical Details
- **Backend:** Java/Spring Boot or Node.js  
- **Frontend:** Next js
- **API:** OpenWeatherMap forecast  
- **BE Port:** 8081
- **FE Port:** 3001
- **Dockerized Images & Containers**
- **Kubernetes AKS Cluster**
- **GitHub Actions**

## 4. System Design Principles
- BASE: Cache refresh is async, eventually sync with API data.
- SOLID / KISS / DRY / Separation of Concerns: Simple, maintainable, reusable, Cache, Scheduler, and Service layers have distinct responsibilities.
- Performance Optimization – Hot/Medium/Cold segregation using LFU + LRU + Cache eviction strategies.
- Rate Limiter: Implemented in weather-svc to allow 150 requests per minute.
- 12-Factor App and HATEOAS Principles
- 

## 5. Design Patterns
- Strategy Pattern – Defines different refresh logics (HOT, MEDIUM, LOW) for cache management. 
- Factory Pattern – Selects and provides the correct refresh strategy based on city hits. 
- Scheduler Pattern – Periodically triggers cache refresh to maintain data freshness. 
- Template Method Pattern – Provides a common refresh algorithm skeleton reused by all strategies. 
- Repository – Service Pattern – Abstracts Redis operations into a clean service layer. 
- Facade Pattern – Simplifies Weather API interaction.

## 6. Security & Performance
- API key protected via env vars and secret in GitHub. 
- Minimal API calls, caching offline data  
- Refresh On-Demand and Periodic schedule based on Cities On-demand categories automations
- Production ready: logging & error handling  

## 7. Testing
- BE-Unit (JUnit / Jest)
- FE-@testing-library/react @testing-library/jest-dom babel-jest

## 8. CI/CD & Deployment
- GitHub Actions
- Dockerized Services
- Kubernetes Cluster Deployment with AKS(Azure)

