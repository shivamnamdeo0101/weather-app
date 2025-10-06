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
<img width="1256" height="988" alt="SequenceAPIDig-Weather_Forecast_Request_Flow__with_Rate_Limiter___Status_Codes_" src="https://github.com/user-attachments/assets/876623bf-5c40-4e31-bc79-98e0015e2995" />
<img width="1256" height="482" alt="SequnceSchedularDig-Cache_Maintenance__Refresh_or_Evict_Strategy_" src="https://github.com/user-attachments/assets/f4472a9a-4b83-4476-877e-cf30dcf9c5df" />




````
@startuml
actor User
participant "Weather App" as app
participant "Weather-Cache" as cache
participant "Weather-SVC" as svc
participant "Rate Limiter" as limiter
participant "Open Weather API" as api
participant "Eviction Scheduler" as scheduler
participant "Eviction Strategy (Refresh/Delete + TTL)" as strategy

User -> app: Request weather forecast
app -> cache: Query cached data

alt CACHE HIT
    cache -> app: Return cached data
    app -> User: Display forecast
else CACHE MISS
    cache -> svc: Request fresh data
    svc -> limiter: Validate request quota (60 req/min per client)

    alt WITHIN LIMIT
        limiter -> svc: Allow request
        svc -> api: Fetch latest weather
        api -> svc: Return weather data
        svc -> cache: Store data in cache (TTL = 5 min)
        cache -> app: Return updated data
        app -> User: Display forecast
    else RATE LIMIT EXCEEDED
        limiter -> svc: Reject with 429 Too Many Requests
        svc -> app: Return error (429)
        app -> User: Show rate limit warning
    end
end

== Strategy Pattern Execution ==
scheduler -> strategy: Evaluate cache usage (every 1 min)
strategy -> cache: Analyze access frequency & recency

alt HOT KEYS (Active <6 min)
    strategy -> cache: Delete old data
    strategy -> svc: Fetch fresh data from API
    svc -> cache: Store refreshed data (TTL = 5 min)
else COLD KEYS (Inactive >6 min)
    strategy -> cache: Delete from cache (evict)
else NORMAL KEYS
    strategy -> cache: Maintain TTL = 5 min
end

scheduler -> cache: Repeat evaluation every 1 min
@enduml

````

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
| `weather-cache` | 8081 | Spring Boot caching service (Redis-backed) |
| `weather-svc`   | 8080 | Backend API service fetching weather data  |
| `redis-db`      | 6379 | Local Redis cache                          |
| `weather-app`   | 3001 | Frontend entry point                       |



## 🏆 Best Practices
### Backend

- **Rate Limiter:**  ✅  
  Implemented in `weather-svc` to allow **60 requests per minute per client**.  
  - Excess requests are rejected with `429 Too Many Requests` 
  - Ensures backend stability and prevents overload.  
  - Designed to be extendable for configurable limits per endpoint or user type.

- **Facade / Cache-Aside Design Pattern:**   ✅ 
  `weather-cache` uses the **Cache-Aside pattern** to reduce direct calls to the backend service (`weather-svc`) / (`weather-api`).  
  - On cache hit: returns cached data immediately, reducing backend load.  
  - On cache miss: fetches fresh data from `weather-svc`, stores it in cache, then returns it.  
  - This offloads `weather-svc`, improves response time, and optimizes system performance. 
  - Can be extended to multi-level caching (e.g., global + regional caches).

- **Cache Eviction Strategy (Strategy Pattern):**  
  Uses a combination of **LRU, LFU, and TTL** strategies:  
  - **Hot Keys:** Most active cities in the last 6 minutes → LFU (least frequently used items are kept longer with refresh data in redis so BE call will reduce and increase availability of the latest data).  
  - **Cold Keys:** Less active cities in the last 6 minutes → LRU (least recently used items are evicted first).  
  - **Normal Keys:** TTL (time-to-live) of 5 minutes for standard entries.✅   
  - A scheduler runs **every 1 minute** to evaluate and adjust eviction strategies automatically.  
  - This ensures optimal cache utilization and prioritizes frequently accessed cities.  

- **Inflight Request Pattern:**  
  Handles multiple simultaneous requests for the same city within 1 minute:  
  - Only **one request** is sent to `weather-svc`.  
  - Other requests for the same city wait for the response and then receive the same data.  
  - Implemented using **queue + ConcurrentHashMap** to track inflight requests.  
  - Reduces redundant backend calls and avoids hitting rate limits.  

- **Exception Handling:**  
  Comprehensive error handling ensures proper responses for different scenarios:  
    | HTTP Status | Scenario                          | Notes                                        |
    |------------|----------------------------------|------------------------------------------------|
    | 429        | Too Many Requests                | Triggered by the rate limiter ✅               |
    | 502        | Service Down                     | Returned when `weather-svc` is unreachable; can be handled with retry or fallback mechanisms |
    | 404        | City Not Found                   | Returned when an invalid city is requested ✅  |
    | 400        | Bad Request                       | Triggered for malformed or invalid query parameters |


- **Cross-Origin Policy:**  ✅ 
  The cache service only accepts requests from the **frontend host**.  
  - Prevents unauthorized access from other origins.  
  - Can be extended to allow dynamic frontend origins with strict CORS rules.  

- **Other Enhancements / Future Considerations:**  
  - Support **regional hotkeys & edge caching** for frequently accessed regions.  
  - Enhance **logging and monitoring** for better observability and troubleshooting.  


## Frontend

- **App Router and File-Based Structure (Next.js 15)** ✅ 
  - Uses `app/` directory with `layout.tsx` and `page.tsx` for clear route and layout composition.
  - Co-locates UI, hooks, and types under `src/` for discoverability.

- **Separation of Concerns** ✅ 
  - `services/weatherApi.ts` isolates network logic and error mapping from UI.
  - `components/` are presentational and stateless where possible; `hooks/` hold view logic.

- **Typed Contracts and Safety** ✅ 
  - Central `types/weather.ts` defines request/response shapes.
  - Strict TypeScript settings (`"strict": true`) to catch issues early.

- **Declarative Data Flow with Custom Hooks** ✅ 
  - `useWeatherSearch` manages async state (loading/error/data/city) with a simple API for pages.
  - `useGroupedForecast`, `useFormattedForecastDate`, `useWeatherEmoji` encapsulate formatting and grouping logic.

- **Memoization and Rendering Performance** ✅ 
  - `React.memo` for pure components (`UnifiedWeatherCard`, `PredictionBadge`, etc.).
  - `useMemo`/`useCallback` to stabilize derived values and handlers, reducing unnecessary re-renders.

- **Composable, Small Components** ✅ 
  - UI is decomposed into focused pieces (`AppHeader`, `SearchBar`, `WeatherTimeSlot`, `WeatherDetails`, etc.) for reuse and testability.

- **Explicit Loading and Error States** ✅ 
  - Dedicated components (`LoadingState`, `ErrorAlert`, `EmptyState`) improve UX and readability.

- **Resilient Networking** ✅ 
  - Centralized response handling with typed `WeatherError`. ✅ 
  - Uses `AbortSignal.timeout(10000)` to avoid hanging requests and provide user feedback.

- **Styling Consistency** ✅ 
  - Tailwind CSS with design tokens via CSS variables in `globals.css`. ✅ 
  - Utility-first classes keep styles close to markup, reducing CSS drift.

- **Accessibility and Semantics** ✅ 
  - Interactive controls are proper `button`/`form` elements; labels, icons, and focusable elements adhere to expected semantics.

- **Keying and List Stability** ✅ 
  - Stable keys combining timestamps and indices for dynamic lists to prevent UI glitches.


## 🛠️ Setup & Run ✅ 

1. Clone the repository:

```bash
git clone <repo-url>
cd weather-app
```

2. Build JARs for backend services:

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

3. (Optional) Start services using Docker Compose / Podman Compose: ✅ 

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

4. Access services: ✅ 

* `weather-svc`: `http://localhost:8080`
* `weather-cache`: `http://localhost:8081`
* `weather-app`: `http://localhost:3001`

---

## ⚙️ Environment Variables 

* **weather-cache/.env** ✅ 

```text
WEATHER_SVC_URL=http://weather-svc:8080/api/weather-svc/forecast
REDIS_HOST=redis-db
REDIS_PORT=6379
REDIS_COMMAND_TIMEOUT=5
REDIS_TTL=300
REDIS_USERNAME=default
REDIS_PASSWORD=0tr***NJx3A
SPRING_PROFILES_ACTIVE=prod
```

* **weather-svc/.env** ✅ 

```text
WEATHER_API_KEY=d2************63e
WEATHER_API_URL=http://api.openweathermap.org/data/2.5/forecast
RATE_LIMITER_MAX_REQ_PER_MIN=10
RATE_LIMITER_MAX_WINDOW_SIZE_IN_SEC=10
WEATHER_API_UNITS=metric
WEATHER_API_CNT=24
```

---

## 🧪 API Testing (cURL Commands) 

### 1️⃣ Weather Cache Service (`localhost:8081`) ✅ 

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

### 2️⃣ Weather Service (`localhost:8080`) ✅ 

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

### 3️⃣ OpenWeather API (External) ✅ 

```bash
curl --location 'https://api.openweathermap.org/data/2.5/forecast?q=banaras&appid=YOUR_API_KEY&cnt=10&units=metric'
```

**Notes:**

* `cnt=8` → returns total forecast blocks to be retured cnt=8 (return 8 forecast blocks forecast blocks per 3 hours window size)
* `units=metric` → temperature in °C, wind speed in m/s
* `/actuator/health` → checks service status

---

## 📦 Docker Images 

* `openjdk:21-jdk-slim` or `amazoncorretto:21` recommended for lightweight runtime. ✅ 
* Build and run containers using `docker-compose` or `podman-compose`. ✅ 

---
