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

#### Load Constraints
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
- Offline mode / API fallback  

## 3. Technical Details
- **Backend:** Java/Spring Boot or Node.js  
- **Frontend:** Next js
- **API:** OpenWeatherMap forecast  
- **BE Port:** 8081
- **FE Port:** 3001
- **Docker**
- **Kubernetes AKS Cluster**
- **GitHub Actions**

## 4. System Design Principles
- BASE: Cache refresh is async, eventually sync with API data.
- SOLID / KISS / DRY / Separation of Concerns: Simple, maintainable, reusable, Cache, Scheduler, and Service layers have distinct responsibilities.
- Performance Optimization – Hot/Medium/Cold segregation using LFU + LRU + Cache eviction strategies.
- Rate Limiter: Implemented in weather-svc to allow 150 requests per minute.

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

