Got it! Here’s the **entire README** in **one code block** you can copy at once:
# Weather App Microservices
A lightweight microservices setup to fetch and cache weather forecasts for cities, using **Spring Boot**, **Redis**, and **Open Weather API**.


## 🧩 Architecture Overview
<img width="895" height="563" alt="image" src="https://github.com/user-attachments/assets/acbf69d1-9f37-4df4-bc8f-95a3aa410104" />


````
plantuml
@startuml
actor User
participant "Weather App" as app
participant "Weather-Cache" as cache
participant "Weather-SVC" as svc
participant "Open Weather API" as api

User -> app: Request for weather forecast
app -> cache: Call cache API

alt CACHE HIT
    cache -> cache: Check cache
    cache -> app: Return cached data
    app -> User: Response Weather forecast
else CACHE MISS
    cache -> svc: Request data
    svc -> api: Fetch latest data from Open Weather API
    api -> svc: Return data
    svc -> cache: Store data in cache
    cache -> app: Return data
    app -> User: Response Weather forecast
end
@enduml
````

---

## 🚀 Services

| Service         | Port | Description                                |
| --------------- | ---- | ------------------------------------------ |
| `weather-cache` | 8081 | Spring Boot caching service (Redis-backed) |
| `weather-svc`   | 8080 | Backend API service fetching weather data  |
| `redis-db`      | 6379 | Local Redis cache                          |
| `weather-app`   | 3001 | Frontend entry point                       |

---

## ⚡ Features

* **Cache-first approach**: Returns cached data if available; fetches from API only on cache miss.
* **Redis-based caching** for improved performance.
* **Dockerized services** for easy deployment.
* **Environment variables** managed via `.env` files for easy configuration.

---

## 🛠️ Setup & Run

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

3. (Optional) Start services using Docker Compose / Podman Compose:

```bash
# Container UP
podman-compose up --build
# or
docker-compose up --build

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
WEATHER_SVC_URL=http://weather-svc:8080/api/weather-svc/forecast
REDIS_HOST=redis-db
REDIS_PORT=6379
REDIS_COMMAND_TIMEOUT=5
REDIS_TTL=300
REDIS_USERNAME=default
REDIS_PASSWORD=0tr***NJx3A
SPRING_PROFILES_ACTIVE=prod
```

* **weather-svc/.env**

```text
WEATHER_API_KEY=d2************63e
WEATHER_API_URL=http://api.openweathermap.org/data/2.5/forecast
RATE_LIMITER_MAX_REQ_PER_MIN=10
RATE_LIMITER_MAX_WINDOW_SIZE_IN_SEC=10
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

* `cnt=10` → returns 10 forecast blocks (~1.25 days)
* `units=metric` → temperature in °C, wind speed in m/s
* `/actuator/health` → checks service status

---

## 📦 Docker Images

* `openjdk:21-jdk-slim` or `amazoncorretto:21` recommended for lightweight runtime.
* Build and run containers using `docker-compose` or `podman-compose`.

---

```

This is **one copy-paste-ready block** for your README including **all setup, environment variables, architecture, and cURL commands**.  

Do you want me to also add a **“Quick Test Example Responses” table** at the end for clarity?
```
