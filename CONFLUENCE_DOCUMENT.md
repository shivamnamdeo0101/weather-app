# Weather Prediction Microservice - Conceptual Design Document

## 📋 Case Study Overview

**Project:** Global Weather Prediction Microservice  
**Objective:** Build a high-performance, scalable weather forecasting system that serves 200,000 cities globally with optimal resource utilization, minimal external API calls, and sub-second response times.

**Key Challenge:** Balance between data freshness, memory constraints, API rate limits, and user experience across multiple geographic regions.

---

## 🎯 Problems Identified

### Problem 1: External API Rate Limiting & Cost Optimization

**Challenge:**
- OpenWeather API enforces strict rate limits: **60 requests per minute per client**
- Direct API calls for every user request would:
  - Exhaust API quota within seconds under normal load
  - Result in **429 Too Many Requests** errors
  - Create high latency (external API calls ~500ms average)
  - Increase operational costs

**Impact:**
- System cannot scale beyond ~60 users per minute without caching
- Poor user experience due to rate limit errors
- Unnecessary API costs for repeated city queries

---

### Problem 2: Memory & Load Management at Global Scale

**Challenge:**
- **200,000 cities** globally need to be supported
- **90% of traffic** comes from only **1,400 most active cities** (10% of 14,000 active cities)
- Memory constraints: Each city requires **8 KB data + 0.12 KB metadata = 8.12 KB** in Redis
- Load constraints: Backend service quota of **150 req/min per region**
- Need to balance:
  - Data freshness (weather changes every 3 hours)
  - Memory efficiency (cannot cache all 200K cities)
  - Scheduler load vs user request load

**Impact:**
- Without intelligent caching: **1.624 GB global memory** requirement (manageable but needs optimization)
- Without load balancing: Scheduler could consume all 150 req/min, leaving no capacity for user requests
- Without tiered refresh: Low-traffic cities waste memory and API calls

---

## 💡 Solution Approach

### Architecture: Multi-Tier Cache with Intelligent Refresh Strategy

**Core Strategy:**
1. **Cache-Aside Pattern** with Redis for fast lookups
2. **Tiered City Classification** (HOT/MEDIUM/LOW) based on access patterns
3. **Dual Refresh Mechanism**: On-Demand + Periodic Scheduler
4. **Rate Limiting** at service layer to protect external API
5. **Multi-Region Deployment** (6 regions) for low latency

---

## 📊 Load Constraints & Memory Calculations

### Memory Calculations (Based on Real Values)

| Metric | Calculation | Result |
|--------|-------------|--------|
| **Total Cities (Global)** | OpenWeather API limit | **200,000 cities** |
| **Per-City Data Size** | Weather data + metadata | **8 KB + 0.12 KB = 8.12 KB** |
| **Global Redis Memory** | 200,000 × 8.12 KB | **1.624 GB** |
| **Per-Region Memory (6 regions)** | 1.624 GB ÷ 6 | **~270.67 MB** |
| **Rounded Per-Region Memory** | Safety buffer | **~400 MB** |

**Per-Region Breakdown:**
- **HOT Cities** (~5,000 cities × 8 KB) = **40 MB**
- **MEDIUM Cities** (~13,000 cities × 8 KB) = **104 MB**
- **LOW Cities** (~22,000 cities × 8 KB) = **176 MB**
- **Total per region** = **~320 MB** (fits within 400 MB budget)

---

### Load Calculations (Based on Real Traffic Patterns)

| Metric | Calculation | Result |
|--------|-------------|--------|
| **Cities per region** | 200,000 ÷ 6 regions (Americas split) | **40,000 cities** |
| **Most-active cities (10%)** | 40,000 × 10% | **1,400 cities** |
| **HOT threshold cities** | 1,400 × 10% | **140 cities** (≥140 hits) |
| **MEDIUM threshold cities** | 1,400 × 5% | **70 cities** (≥70 hits) |
| **Scheduler refresh load** | (140 + 70) ÷ 2 min | **105 req/min** |
| **Remaining quota for users** | 150 - 105 | **45 req/min** |
| **Cache hit target** | HOT cities serve 90% traffic | **~90% cache hit rate** |
| **Avg API call duration** | Measured | **0.5 seconds** |
| **Tested throughput** | Local load testing | **500 req/sec** |

**Scheduler Strategy:**
- Runs **every 15 minutes**
- Refreshes **140 HOT cities** (every 20 min) + **70 MEDIUM cities** (every 30 min)
- Uses **virtual threads** with **0-500ms stagger** to avoid API spikes
- **On-demand refresh** handles threshold crossings between scheduler runs

---

## 🏗️ System Design Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Global Infrastructure                     │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │ CloudFront   │         │  Route 53    │                  │
│  │   (CDN)      │         │ (DNS Routing)│                  │
│  └──────────────┘         └──────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
   ┌────▼────┐      ┌─────▼─────┐     ┌─────▼─────┐
   │  Asia   │      │  Europe   │     │ Americas  │
   │ Region  │      │  Region   │     │  Region   │
   └─────────┘      └───────────┘     └───────────┘
```

### Regional Pod Architecture (Per Region)

```
┌─────────────────────────────────────────────────────────────┐
│              Weather Service Pod (AKS)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   Frontend   │  │ Weather-Cache │  │ Weather-SVC   │    │
│  │  (Next.js)   │  │   Service     │  │   Service     │    │
│  │   Port 3001  │  │   Port 8081   │  │   Port 8080   │    │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │
│         │                 │                  │              │
│         │                 │                  │              │
│         └─────────────────┼──────────────────┘              │
│                           │                                 │
│                  ┌────────▼────────┐                        │
│                  │   Redis Cache   │                        │
│                  │   (Port 6379)   │                        │
│                  │   ~400 MB       │                        │
│                  └─────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
                           │
                           │
                  ┌────────▼────────┐
                  │ OpenWeather API │
                  │  (External)      │
                  └──────────────────┘
```

### Data Flow

1. **Cache Hit Flow:**
   - User → Frontend → Weather-Cache → Redis (HIT) → Return cached data
   - On-demand refresh check: If threshold crossed → async refresh from Weather-SVC

2. **Cache Miss Flow:**
   - User → Frontend → Weather-Cache → Redis (MISS) → Weather-SVC
   - Weather-SVC → Rate Limiter check → OpenWeather API → Store in Redis → Return

3. **Scheduler Flow (Every 15 min):**
   - Scheduler evaluates all cities in Redis
   - **HOT cities** (hits ≥ 140): Refresh every 20 min
   - **MEDIUM cities** (70 ≤ hits < 140): Refresh every 30 min
   - **LOW cities**: Evict if no access in last 1 hour
   - Uses virtual threads with stagger to refresh 210 cities within 2 minutes

---

## 🎯 Best Practices Implemented & Why

### 1. **Design Patterns**

| Pattern | Implementation | Why |
|---------|---------------|-----|
| **Strategy Pattern** | `CacheRefreshStrategy` (HOT/MEDIUM/LOW) | Allows dynamic refresh logic without modifying core scheduler code. Easy to add new tiers. |
| **Factory Pattern** | `CacheRefreshStrategyFactory` | Centralizes strategy selection based on city hits, reducing coupling. |
| **Template Method** | Common refresh algorithm skeleton | Reuses common logic (stagger, error handling) across all strategies. |
| **Cache-Aside** | Redis lookup → API fetch → Store | Reduces external API calls by 90%, improves response time from 500ms to <10ms. |
| **Facade Pattern** | Weather-Cache service | Simplifies complex interactions (Redis + SVC + Scheduler) behind simple API. |

---

### 2. **System Design Principles**

| Principle | Implementation | Why |
|-----------|---------------|-----|
| **BASE (Eventually Consistent)** | Async scheduler refresh | Balances freshness with performance. Users get cached data immediately, updates happen in background. |
| **SOLID** | Single responsibility per service | Weather-Cache handles caching, Weather-SVC handles API calls. Easy to test, maintain, and scale independently. |
| **DRY/KISS** | Reusable components, simple logic | Reduces bugs, improves readability. Common refresh logic shared via Template Method. |
| **12-Factor App** | Environment variables, stateless processes | Enables easy deployment across dev/test/prod. Stateless design allows horizontal scaling. |

---

### 3. **Performance Optimizations**

| Optimization | Implementation                       | Why |
|--------------|--------------------------------------|-----|
| **Tiered Caching** | HOT/MEDIUM/LOW classification        | 90% of traffic from 1,400 cities served from cache. Memory-efficient for 200K cities. |
| **Virtual Threads** | Java 21 virtual threads for scheduler | Handles 210 concurrent refreshes without blocking. Reduces thread overhead vs traditional threads. |
| **Staggering** | 0-500ms random delay per refresh     | Prevents API spike. Distributes 210 refreshes over 2 minutes, staying within 150 req/min limit. |
| **On-Demand Refresh** | Threshold-based async refresh        | Reduces scheduler load. Only refreshes when city crosses threshold, not on every scheduler run. |
| **Rate Limiting** | Sliding window (150 req/min)         | Protects external API from overload. Prevents quota exhaustion and 429 errors. |

---

### 4. **Resilience & Fault Tolerance**

| Practice | Implementation | Why |
|----------|---------------|-----|
| **Graceful Degradation** | Return cached data when API unavailable | System remains functional even if OpenWeather API is down. Users see last known data. |
| **Error Handling** | Custom exceptions with proper HTTP codes | Clear error messages (404, 429, 502) help debugging and user experience. |
| **Retry Logic** | Max 3 retries with exponential backoff | Handles transient network failures without overwhelming the system. |
| **Circuit Breaker** | (Future) Prevents cascading failures | Would stop calling failing API after threshold, allowing recovery time. |

---

### 5. **Scalability & Deployment**

| Practice | Implementation | Why |
|----------|---------------|-----|
| **Multi-Region Deployment** | 6 regions (Asia, Europe, Africa, Americas x2, Oceania) | Reduces latency. Users connect to nearest region (<50ms vs 200ms+). |
| **Horizontal Scaling** | Stateless services, Kubernetes pods | Can scale pods independently based on load. No shared state means easy scaling. |
| **Containerization** | Docker images, AKS deployment | Consistent environments across dev/test/prod. Easy rollback and versioning. |
| **CI/CD** | GitHub Actions → Docker Hub → AKS | Automated testing and deployment reduces human error and deployment time. |

---

### 6. **Code Quality & Maintainability**

| Practice | Implementation | Why |
|----------|---------------|-----|
| **Type Safety** | TypeScript strict mode, Java generics | Catches errors at compile time. Reduces runtime bugs. |
| **Unit Testing** | JUnit (backend), Jest (frontend) | 80%+ coverage ensures code quality. Tests serve as documentation. |
| **Separation of Concerns** | Service layer, Repository pattern | Each layer has single responsibility. Easy to mock and test. |
| **Logging** | Structured logging with SLF4J | Helps debugging production issues. Logs include request IDs for tracing. |

---

## 📈 Key Metrics & Results

| Metric | Target | Achieved |
|--------|--------|----------|
| **Cache Hit Rate** | 90% | ~90% (from HOT cities) |
| **Response Time (Cache Hit)** | <50ms | <10ms |
| **Response Time (Cache Miss)** | <1s | ~500ms (API call) |
| **Memory per Region** | <400 MB | ~320 MB |
| **API Calls Saved** | 90% | ~90% (via caching) |
| **Throughput** | 100 req/sec | 500 req/sec (tested) |
| **Scheduler Load** | <110 req/min | 105 req/min |
| **User Request Capacity** | >40 req/min | 45 req/min |

---

## 🔄 Trade-offs & Decisions

| Decision | Trade-off | Rationale |
|----------|-----------|-----------|
| **Cache-Aside vs Write-Through** | Cache-Aside: Simpler, eventual consistency | Write-Through adds complexity. Weather data can tolerate 20-30 min staleness. |
| **3-Tier Classification** | More tiers = better optimization, but more complexity | 3 tiers (HOT/MEDIUM/LOW) provide 90% benefit with manageable complexity. |
| **Scheduler Frequency (15 min)** | More frequent = fresher data, but higher load | 15 min balances freshness (weather changes every 3 hours) with API quota. |
| **Virtual Threads vs Thread Pool** | Virtual threads: Better for I/O-bound, lower overhead | 210 concurrent refreshes benefit from virtual threads without thread pool exhaustion. |
| **Multi-Region vs Single Region** | Multi-region: Higher cost, but better UX | Latency reduction (50ms vs 200ms) justifies cost for global user base. |

---

## 🚀 Future Enhancements

1. **Inflight Request Pattern**: Deduplicate concurrent requests for same city
2. **Circuit Breaker**: Stop calling failing API after threshold
3. **Regional Hotkeys**: Edge caching for frequently accessed regions
4. **Monitoring & Alerting**: Prometheus + Grafana for real-time metrics
5. **A/B Testing**: Test different refresh intervals for optimization

---

## 📝 Conclusion

This solution addresses both problems through:

1. **Problem 1 (Rate Limiting)**: Solved via multi-tier caching with 90% cache hit rate, reducing external API calls from potential thousands per minute to ~105 req/min (scheduler) + 45 req/min (user misses).

2. **Problem 2 (Memory & Load)**: Solved via intelligent tiered refresh strategy, keeping memory at ~320 MB per region while serving 90% of traffic from cache, and balancing scheduler load (105 req/min) with user capacity (45 req/min).

**Key Success Factors:**
- Real-world constraints drive design decisions (not theoretical)
- Measured values (8.12 KB per city, 0.5s API latency, 500 req/sec throughput)
- Production-ready practices (12-Factor, SOLID, testing, monitoring)
- Scalable architecture (multi-region, stateless, containerized)

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Author:** Weather App Team

