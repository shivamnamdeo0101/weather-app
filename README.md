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

## 1.1 Installation & Setup

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for frontend)
- Java 17+ (for backend)
- Redis

### Quick Start

**Clone & Install:**
```bash
git clone https://github.com/shivamnamdeo0101/weather-app.git
cd weather-app
```

**Using Docker Compose (Recommended):**
```bash
docker-compose up -d
# Frontend: http://localhost:3001
# Backend APIs: http://localhost:8081
```

**Manual Setup:**
```bash
# Backend
cd server/weather-svc && ./mvnw spring-boot:run
cd server/weather-cache && ./mvnw spring-boot:run

# Frontend
cd client && npm install && npm run dev
```

**Environment Variables:**

Create `.env` files in each service:

**Backend (`server/weather-svc/.env`):**
```
# Connects to the 'weather-svc' container within the Docker network
WEATHER_SVC_URL=http://weather-svc:8080/api/weather-svc/forecast
# Connects to the 'redis-db' service container
REDIS_HOST=redis-db
REDIS_PORT=6379
 #in REDIS_COMMAND_TIMEOUT Seconds
REDIS_COMMAND_TIMEOUT=60
#In Seconds REDIS_TTL 1-hours Min In Sec
REDIS_TTL=3600000
# Uses the specified credentials for the local Redis instance (if configured)
REDIS_USERNAME=default
REDIS_PASSWORD=0t*******YOUR_API_KEY**********3A
# Sets the Spring profile
SPRING_PROFILES_ACTIVE=prod
#Rest Template
#1 Min
#in MILLISECONDS
REST_CONNECT_TIMEOUT=200000
REST_READ_TIMEOUT=200000

#Schedular Configs
HOT_HIT_THRESHOLD=140                  #hits count most hot/active/priority cities
MEDIUM_HIT_THRESHOLD=70                #hits count mid  hot/active/priority cities

HOT_REFRESH_INTERVAL_MS=1200000         #20 minutes
MEDIUM_REFRESH_INTERVAL_MS=1800000      #30 minutes
LOW_ACTIVE_REFRESH_INTERVAL_MS=2400000  #40 minutes
MAX_AGE_MS=2700000                      #45 minutes

```
**Cache Service (`server/weather-cache/.env`):**
```
# Weather API config
WEATHER_API_KEY=d2*************************e
WEATHER_API_URL=http://api.openweathermap.org/data/2.5/forecast
# Rate Limiter configuration
RATE_LIMITER_MAX_REQ_PER_MIN=60
RATE_LIMITER_MAX_WINDOW_SIZE_IN_SEC=60
WEATHER_API_UNITS=metric
WEATHER_API_CNT=24

```
**Get OpenWeather API Key:**
1. Sign up at [openweathermap.org](https://openweathermap.org/api)
2. Generate API key from account settings
3. Add to respective `.env` files

 
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
  - Weather-SVC -> Check if (rate limiter allowed 150 calls per min) then calls external OpenWeather API, then SVC put the data into cache then return data on FE

- **SCHEDULAR Flow:**
  - The **Weather Cache Scheduler** inside `weather-cache`  manages cached weather data in Redis efficiently, balancing **freshness** and **performance**. It monitors city-level cache entries using **hit counts** , **lastRefresh** and **last access time**.
  - Scheduler runs **every 15 min** to evaluate all cities.
  - **Hot(Most Active) Cities:** `hits ≥ 140` → Most active refreshed latest weather every 20 min and reset hits
  - **Medium Active Cities:** `70 ≤ hits <= 140` → refreshed latest weather every 30 min and reset hits.
  - **Low Cities**:** Eviction / remove record if no lastAccess in last 1 hour.

### Diagrams

<img width="1164" height="464" alt="image" src="https://github.com/user-attachments/assets/a65b7865-c8e5-4674-935e-1c5e0f759c0e" />

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

