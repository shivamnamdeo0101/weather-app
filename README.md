# 🌤 Weather App
This project provides a high-performance, cache-enabled weather forecasting system. It uses **Spring Boot** for backend APIs, Redis for caching frequently accessed data, and the **OpenWeather API** for real-time weather information. Built with microservices architecture, it implements **rate limiting, cache-aside pattern, and LRU/LFU strategies** for optimal efficiency and scalability.

# 💡Features

- 🔍 Search weather by city
- 📅 View 5-day forecast
- ⏰ See hourly updates
- 🌡️ Temperature in °C
- ☁️ Weather condition: clear, cloudy, or rain
- 💨 Check wind speed & humidity
- ⚠️ Receive friendly error messages
- 📱 Fully mobile-responsive
- 👀 Prediction / Advice for the user

## 🧩 Sequnce Diagrams
<img width="1261" height="1017" alt="SequenceAPIDig-Weather_Forecast_Request_Flow__with_Rate_Limiter___Status_Codes_" src="https://github.com/user-attachments/assets/ca2af486-e7c6-4554-a489-29192c0774e4" />
<img width="1029" height="1643" alt="SequnceSchedularDig-Weather_Cache_Scheduler__Internal_Cache_Refresh___Eviction_Flow" src="https://github.com/user-attachments/assets/fc966047-fa9e-406c-9b89-a011bab06608" />



## ⚙️ Tech Stack

| **Frontend**                                           | **Backend**                                           | **DevOps / Infrastructure**                                           |
| ------------------------------------------------------ | ----------------------------------------------------- | --------------------------------------------------------------------- |
| **Framework:** Next.js                                 | **Framework:** Spring Boot (Java 21)                  | **Containerization:** Docker / Podman                                 |
| **Language:** TypeScript                               | **Services:** `weather-cache`, `weather-svc`          | **Orchestration:** Docker Compose / Podman Compose                    |
| **UI Styling:** Tailwind CSS                           | **Database:** Redis (for caching)                     | **Monitoring:** Spring Boot Actuator                                  |
| **State Management:** React Hooks + Memoization        | **External API:** OpenWeather API                     | **Environment Management:** `.env` files per service                  |
| **Networking:** Fetch API with `AbortSignal.timeout()` | **Patterns:** Cache-Aside, Strategy, Inflight Request | **Build Tools:** Maven, Shell scripts (`run.sh`, `build_all_jars.sh`) |
| **Type Safety:** TypeScript strict mode                | **Rate Limiting:** Sliding Window (60 req/min)        | **Base Image:** `amazoncorretto:21`                                   |
| **Routing:** App Router (`layout.tsx`, `page.tsx`)     | **Error Handling:** Custom 400/404/429/502/200 for OK | **Cache Strategy:** LRU, LFU, TTL combined                            |


## 🚀 Services

| Service         | Port | Description                                |
| --------------- | ---- | ------------------------------------------ |
| `weather-cache` | 8081 | Spring Boot caching service + Schedular    |
| `weather-svc`   | 8080 | Backend API service fetching weather data  |
| `redis-db`      | 6379 | Local Redis cache                          |
| `weather-app`   | 3001 | Frontend entry point                       |




## 🏆 Best Practices

- **System Design Principles**
  - Separation of Concerns / SOLID – Cache, Scheduler, and Service layers have distinct responsibilities.
  - BASE: Cache refresh is async, eventual sync with API data.
  - KISS / DRY / Separation of Concerns: Simple, maintainable, reusable
  - Performance Optimization – Hot/Medium/Cold segregation using LFU + LRU strategies.
  - Resilience & Fault Tolerance – Retry, Circuit Breaker, and fallback handling external failures.


- **Design Patterns Used**
  - Strategy Pattern – Different cache refresh/evict logic (HOT, MEDIUM, LOW).
  - Scheduler Pattern – Periodic refresh and cleanup triggered automatically.
  - Facade Pattern - Unified interface to fetch weather data (handles API calls, retries, circuit breaker, fallback)
  - Circuit Breaker / Retry Pattern – For API call resilience.
  - Repository-Service Pattern– Redis operations abstracted in GenericRedisServiceImpl.

### 🧩 Detailed Flow

- **Basic Flow:**
  - Frontend ->  hits -> Weather-Cache service
  - Weather-Cache -checks Redis for city data
    - ✅ If found → return data immediately
    - ❌ If not found → call Weather-SVC
  - Weather-SVC -> Check if (rate limiter allowed 60 calls per min) then calls external OpenWeather API, then SVC put the data into cache then return data on FE
  

- **Rate Limiter:**    
  Implemented in `weather-svc` to allow **60 requests per minute per client**.  
  - Excess requests are rejected with `429 Too Many Requests` 
  - Ensures backend stability and prevents overload.  
  - Designed to be extendable for configurable limits per endpoint or user type.

- **Facade Design Pattern:**    
  `weather-cache` uses the **Cache-Aside pattern** to reduce direct calls to the backend service (`weather-svc`) / (`weather-api`).  
  - On cache hit: returns cached data immediately, reducing backend load.  
  - On cache miss: fetches fresh data from `weather-svc`, stores it in cache, then returns it.  
  - This offloads `weather-svc`, improves response time, and optimizes system performance. 
  - Can be extended to multi-level caching (e.g., global + regional caches).

- **Weather Cache Scheduler:**
  -The **Weather Cache Scheduler** inside `weather-cache`  manages cached weather data in Redis efficiently, balancing **freshness** and **performance**. It monitors city-level cache entries using **hit counts** , **lastRefresh** and **last access time**.

  - **Cache Eviction Behavior:**  
    - Scheduler runs **every 5 min** to evaluate all cities.
    - 🔥 **Hot Cities:** `hits ≥ 50` → Most active refreshed latest weather every 10 min and reset hits
    - 🌤 **Medium Cities:** `20 ≤ hits < 50` → refreshed latest weather every 30 min and reset hits.
    - ❄️ **Low Cities**:** Eviction / remove record if no lastAccess in last 1 hour.

  - **Strategy Design Pattern:**  
    - Uses a combination of **LFU, LRU, and TTL**:
    - **Hot Keys:** LFU → keep frequently accessed data longer with latest data.
    - **Cold Keys:** LRU → evict least recently used entries first.
    - **Normal Keys:** TTL → 1 hour min standard refresh.
    - This ensures **optimal cache usage**, reduces backend API calls, and keeps frequently used data updated in redis, while low-traffic keys aren’t removed immediately but   gradually evicted after inactivity.
 

- **Exception Handling:**  
  Comprehensive error handling ensures proper responses for different scenarios:  
    | HTTP Status | Scenario                          | Notes                                        |
    |------------|----------------------------------|------------------------------------------------|
    | 429        | Too Many Requests                | Triggered by the rate limiter                |
    | 502        | Service Down                     | Returned when `weather-svc` is unreachable; can be handled with retry fallback mechanisms |
    | 404        | City Not Found                   | Returned when an invalid city is requested   |
    | 400        | Bad Request                      | Triggered for malformed or invalid query parameters |


- **Cross-Origin Policy:**   
  The cache service only accepts requests from the **frontend host**.  
  - Prevents unauthorized access from other origins.  

- **Other Enhancements / Future Considerations:**  
    - Support **regional hotkeys & edge caching** for frequently accessed regions.  
    - Enhance **logging and monitoring** for better observability and troubleshooting.  

  - **Future Scope - Inflight Request Design Pattern:**  
    - Handles multiple simultaneous requests for the same city within 1 minute:  
    - Only **one request** is sent to `weather-svc`.  
    - Other requests for the same city wait for the response and then receive the same data.  
    - Implemented using **queue + ConcurrentHashMap** to track inflight requests.  
    - Reduces redundant backend calls and avoids hitting rate limits. 
    - For global - multi-region Redis replication (AWS Global Datastore).


### 🧩 Frontend

- **App Router and File-Based Structure (Next.js 15)**  
  - Uses `app/` directory with `layout.tsx` and `page.tsx` for clear route and layout composition.
  - Co-locates UI, hooks, and types under `src/` for discoverability.

- **Separation of Concerns**  
  - `services/weatherApi.ts` isolates network logic and error mapping from UI.
  - `components/` are presentational and stateless where possible; `hooks/` hold view logic.

- **Typed Contracts and Safety**  
  - Central `types/weather.ts` defines request/response shapes.
  - Strict TypeScript settings (`"strict": true`) to catch issues early.

- **Declarative Data Flow with Custom Hooks**  
  - `useWeatherSearch` manages async state (loading/error/data/city) with a simple API for pages.
  - `useGroupedForecast`, `useFormattedForecastDate`, `useWeatherEmoji` encapsulate formatting and grouping logic.

- **Memoization and Rendering Performance**  
  - `React.memo` for pure components (`UnifiedWeatherCard`, `PredictionBadge`, etc.).
  - `useMemo`/`useCallback` to stabilize derived values and handlers, reducing unnecessary re-renders.

- **Composable, Small Components**  
  - UI is decomposed into focused pieces (`AppHeader`, `SearchBar`, `WeatherTimeSlot`, `WeatherDetails`, etc.) for reuse and testability.

- **Explicit Loading and Error States**  
  - Dedicated components (`LoadingState`, `ErrorAlert`, `EmptyState`) improve UX and readability.

- **Resilient Networking**  
  - Centralized response handling with typed `WeatherError`.  
  - Uses `AbortSignal.timeout(10000)` to avoid hanging requests and provide user feedback with max 3 retries.

- **Styling Consistency**  
  - Tailwind CSS with design tokens via CSS variables in `globals.css`.  
  - Utility-first classes keep styles close to markup, reducing CSS drift.

- **Accessibility and Semantics**  
  - Interactive controls are proper `button`/`form` elements; labels, icons, and focusable elements adhere to expected semantics.

- **Keying and List Stability**  
  - Stable keys combining timestamps and indices for dynamic lists to prevent UI glitches.


## 🛠️ Setup & Run  

1. Clone the repository:

```bash
git clone <repo-url>
cd weather-app
```

2. Build JARs for backend services:

```bash
Install Java21+ and Maven


```bash
# Backend
chmod +x run.sh
chmod +x build_all_jars.sh
./run.sh

# Frontend (client)
cd client
npm install -g yarn
yarn install
yarn run dev
```

3. (Optional) Start services using Docker Compose / Podman Compose:  

```bash
# Container UP
podman-compose up --build -d
# or
docker-compose up --build -d

# Container Down
podman-compose down -v
# or
docker-compose down -v
```

4. Access services:  

* `weather-svc`: `http://localhost:8080`
* `weather-cache`: `http://localhost:8081`
* `weather-app`: `http://localhost:3001`

---

## ⚙️ Environment Variables 

* **weather-cache/.env**  

```text
# Connects to the 'weather-svc' container within the Docker network
WEATHER_SVC_URL=http://weather-svc:8080/api/weather-svc/forecast
# Connects to the 'redis-db' service container
REDIS_HOST=redis-db
REDIS_PORT=6379
 #in REDIS_COMMAND_TIMEOUT Seconds
REDIS_COMMAND_TIMEOUT=60
#In Seconds REDIS_TTL
REDIS_TTL=900
# Uses the specified credentials for the local Redis instance (if configured)
REDIS_USERNAME=default
REDIS_PASSWORD=0tr************************3A
# Sets the Spring profile
SPRING_PROFILES_ACTIVE=prod
#Rest Template
#1 Min
#in MILLISECONDS
REST_CONNECT_TIMEOUT=200000
REST_READ_TIMEOUT=200000
#Schedular Configs
HOT_HIT_THRESHOLD=50 #hits count
MEDIUM_HIT_THRESHOLD=20 #hits count
HOT_REFRESH_INTERVAL_MS=300000       # 5 minutes
MEDIUM_REFRESH_INTERVAL_MS=900000   # 15 minutes
MAX_AGE_MS=3600000                   # 1 hour

```

* **weather-svc/.env**  

```text
# Weather API config
WEATHER_API_KEY=d2*************************e
WEATHER_API_URL=http://api.openweathermap.org/data/2.5/forecast
# Rate Limiter configuration
RATE_LIMITER_MAX_REQ_PER_MIN=60
RATE_LIMITER_MAX_WINDOW_SIZE_IN_SEC=60
WEATHER_API_UNITS=metric
WEATHER_API_CNT=24

```

---

## 🧪 API Testing (cURL Commands) 

### 1️⃣ Weather Cache Service (`localhost:8081`)  

**Fetch forecast via cache:**

```bash
curl --location 'http://localhost:8081/api/weather-cache/forecast?city=indore' \
--header 'accept: application/json' \
--header 'Content-Type: application/json'
```

**Health check:**

```bash
curl --location 'http://localhost:8081/actuator/health'
```

---

### 2️⃣ Weather Service (`localhost:8080`)  

**Fetch forecast directly:**

```bash
curl --location 'http://localhost:8080/api/weather-svc/forecast?city=indore' \
--header 'accept: application/json' \
--header 'Content-Type: application/json' \
--data ''
```

**Health check:**

```bash
curl --location 'http://localhost:8080/actuator/health'
```

---

### 3️⃣ OpenWeather API (External)  

```bash
curl --location 'https://api.openweathermap.org/data/2.5/forecast?q=banaras&appid=YOUR_API_KEY&cnt=10&units=metric'
```

**Notes:**

* `cnt=8` → returns total forecast blocks to be retured cnt=8 (return 8 forecast blocks forecast blocks per 3 hours window size)
* `units=metric` → temperature in °C, wind speed in m/s
* `/actuator/health` → checks service status

---

## 📦 Docker Images 

* `openjdk:21-jdk-slim` or `amazoncorretto:21` recommended for lightweight runtime.  
* Build and run containers using `docker-compose` or `podman-compose`.  

---
