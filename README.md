---

# Weather App Microservices

A lightweight microservices setup to fetch and cache weather forecasts for cities, using **Spring Boot**, **Redis**, and **Open Weather API**.

---

## 🧩 Architecture Overview

<img width="895" height="563" alt="image" src="https://github.com/user-attachments/assets/acbf69d1-9f37-4df4-bc8f-95a3aa410104" />


```plantuml
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
```

---

## 🚀 Services

| Service         | Port | Description                                |
| --------------- | ---- | ------------------------------------------ |
| `weather-cache` | 8081 | Spring Boot caching service (Redis-backed) |
| `weather-svc`   | 8080 | Backend API service fetching weather data  |
| `redis-db`      | 6379 | Local Redis cache                          |
| `weather-app`   | 80   | (Optional) Frontend entry point            |

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
./build_all_jars.sh
```

3. Start services using Docker Compose / Podman Compose:

```bash
podman-compose up --build
# or
docker-compose up --build
```

4. Access services:

* `weather-svc`: `http://localhost:8080`
* `weather-cache`: `http://localhost:8081`
* `weather-app` (if enabled): `http://localhost`

---

## ⚙️ Environment Variables

* **weather-cache/.env**

```text
# Connects to the 'weather-svc' container within the Docker network
WEATHER_SVC_URL=http://weather-svc:8080/api/weather-svc/forecast

# Connects to the 'redis-db' service container
REDIS_HOST=redis-db
REDIS_PORT=6379
REDIS_COMMAND_TIMEOUT=5
REDIS_TTL=300

# Uses the specified credentials for the local Redis instance (if configured)
REDIS_USERNAME=default
REDIS_PASSWORD=0tr***NJx3A

# Sets the Spring profile
SPRING_PROFILES_ACTIVE=prod
```

* **weather-svc/.env**

```text
# Weather API config
WEATHER_API_KEY=d2************63e
WEATHER_API_URL=http://api.openweathermap.org/data/2.5/forecast
# Rate Limiter configuration
RATE_LIMITER_MAX_REQ_PER_MIN=10
RATE_LIMITER_MAX_WINDOW_SIZE_IN_SEC=10

```

---

## 🧪 Notes

* Ensure Redis password in `.env` matches the `redis` service command if manually configured.
* For development, you can disable caching or use a test API key.

---

## 📦 Docker Images

* `openjdk:21-jdk-slim` or `amazoncorretto:21` recommended for lightweight runtime.
* Build and run containers using `docker-compose` or `podman-compose`.

---
