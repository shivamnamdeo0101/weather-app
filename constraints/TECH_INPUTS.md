
# Overall PE & Engg. readiness Observations

1. Demonstrates ability to do E2E Design, Develop, Deploy, Own & Operate
2. Demonstrates Understanding of the "Why" aspects along with "How"
3. Demonstrates focus on NFRs, Trade-offs
4. Demonstrates strong problem solving and able to consider different perspectives
5. Demonstrates Agile processes & Engg. Practices in practice
6. Demonstrates Product & Design thinking, with awareness of MVP & MVO
7. Demonstrates Live Production experience, dealing w internet Scale & Volume
8. Demonstrates PE Mindset covering engineering craft & competencies + 12 Factor Apps
10. Demonstrates Full-stack expertise & mindset
11. Demonstrates Polyglot experience & mindset
12. Demonstrates CI/CD & Dev. Ops Mindset
13. Demonstrates being the Change Advocate & PE/SE Evangelist w Learning ability & Fire in the belly
"14. Demonstrates client facing traits - 
clear articulation, manage situations on feet independently, alternate solutions over blockers! "
15. Demonstrates technology breadth & depth


# Code review against questions -  (from live casestudy or project sources or innersource/opensource/tools contributions) (in the sequence)

**1. Is the end user able to view results by changing the input parameters?**  
Yes – pass the city name in search bar to see dynamic results.

**2. Is the service ready to be released to production or live environment?**  
Yes – weather-cache service is production-ready with configuration, error handling, caching, and scheduler.
Yes  - prod/dev/test env setup with each env variables/tokens

**3. Is the service accessible via web browser or Postman (or using JS frameworks, HTML, or JSON)?**  
Yes – accessible via Postman or any HTTP client, returning JSON responses.

**4. Is the solution support offline mode with toggles?**  
Yes – toggle to force offline on frontend.

**5. Is the service returning relevant results even if dependencies (Public API) are unavailable?**  
Yes – returns cached data when upstream API is unavailable with X-Cache HIT/MISS info.

**6. Is the extended solution using own code/logic/data structures and without 3rd party libraries or DB?**  
Yes – implemented custom sliding-window rate limiter using `ConcurrentLinkedDeque`.

**7. Is able to improvise the solution & deal with edge conditions?**  
Yes – handles Redis cache misses and upstream API failures reliably.

**8. Is able to use SonarQube / code quality, coverage, static analysis tools?**  
Yes – backend coverage reports, frontend with Jest for full visibility.

**9. Is able to use relevant tools (Postman, Fiddler, Newman, HTTPie, etc.)?**  
Yes – Postman used for API testing and debugging.

**10. Is hands-on with UI frameworks (React, Vue, Angular)?**  
Yes – Next.js (React) frontend, TailwindCSS, state management, responsive design, tested with Jest/RTL.

**11. Is hands-on with SCM (Git CLI / GitHub / GitLab / BitBucket)?**  
Yes – Git CLI & GitHub used for branch/commit management and pull requests.

**12. Is hands-on with DevOps - CI pipelines (Jenkins)?**  
Yes – GitHub Actions pipelines for build, test, and Docker image push.

**13. Is hands-on with DevOps - CD deployment (Chef, Shell scripts, Terraform)?**  
Yes – automated deployments with GitHub Actions, Docker Hub, Kubernetes manifests.

**14. Is hands-on with containerization & orchestration (Docker, K8S, minikube)?**  
Yes – Docker containers deployed on AKS with environment-specific manifests.

**15. Is hands-on with Cloud Technologies (AWS/Azure/GCP)?**  
Yes – deployed on Azure AKS, managed secrets, secure API access.

**16. Is hands-on with 12 Factor Apps?**  
Yes — in the weather app project, all 12 Factor App principles are practically followed as:

1. **Codebase** – Single Git repository holds both frontend (Next.js) and backend (Spring Boot) code. ✅
2. **Dependencies** – Explicitly declared in `package.json` for FE and `pom.xml` for BE. ✅
3. **Config** – Externalized via `application-{dev/prod}.properties` and environment variables for endpoints, API keys, and toggles. ✅
4. **Backing Services** – Redis cache used as an attached stateless service for weather data. ✅
5. **Build, Release, Run** – Docker images separate the build/runtime environment; same image can be deployed across dev/test/prod. ✅
6. **Processes** – Stateless Spring Boot app with virtual threads for async cache refresh; no in-memory state tied to requests. ✅
7. **Port Binding** – Backend exposes REST APIs via HTTP; frontend runs on a configurable port (Next.js dev server). ✅
8. **Concurrency** – Multiple stateless processes/containers can run concurrently on AKS cluster, supporting scale-out. ✅
9. **Disposability** – App starts and shuts down quickly; scheduler and cache refresh tasks are managed safely. ✅
10. **Dev/Prod Parity** – Separate property files, environment variables, and Docker manifests ensure parity. ✅
11. **Logs** – All logs printed to console for aggregation in Kubernetes/AKS logging. ✅
12. **Admin Processes** – Cache refresh, scheduler, and other maintenance tasks run as separate one-off processes (or background jobs). ✅


**17. Is hands-on with TDD, Unit tests, Mocks & E2E + Test Reports (JUnit/TestNG)?**  
Yes – unit & integration tests with JUnit and Mockito, coverage reports generated.

**18. Is hands-on with BDD (Cucumber, Karate, Serenity)?**  
REMAINING – context known but not implemented.

**19. Is hands-on with Core language features & concepts (Threading, Generics, Collections)?**  
Yes – Java Thread/virtual threads, Generics, Collections for cache management.

**20. Is hands-on with Clean Code & Code Refactoring?**  
Yes – followed SOLID, modular, readable, maintainable code with DRY/KISS principles.

**21. Is hands-on with Error Handling?**  
Yes – centralized error handling with `@RestControllerAdvice`, proper HTTP status codes.

**22. Is hands-on with Exception handling & Custom Exceptions?**  
Yes – global exception handling with custom exceptions (`WeatherServiceException`, `BadRequestException`).

**23. Is hands-on with Code Optimizations (GC opt., Performance tuning)?**  
Yes – performance tuned via Redis caching, TTL management, virtual threads, batch updates.

**24. Is hands-on with Functional / Reactive Programming?**  
Yes – functional/concurrent programming using Java virtual threads; not fully reactive (no Reactor/RxJava).

**25. Is hands-on with RESTful service concepts, creation?**  
Yes – REST APIs in Spring Boot with proper HTTP methods, status codes, headers, query params, JSON responses.

**26. Is hands-on with Microservice Frameworks (Spring Boot / VertX / Akka / Play / Micronaut)?**  
Yes – Spring Boot microservice with REST APIs, Redis caching, scheduler, exception handling.

**27. Is hands-on with HTTP Protocol (Headers, Statuscodes, Content Types, HATEOS)?**  
Yes – proper HTTP status codes, headers (`X-Cache`), content types (`application/json`); HATEOAS not used.

**28. Is hands-on with Configurability, Environmental profiles, Feature Toggles?**  
REMAINING – separate config files for dev/test/prod, frontend toggles for offline mode.

**29. Is hands-on with Backward compatibility & Traceability from req. to deliveries?**  
Yes – backward compatible API endpoints docker image version controlled, old clients continue to work; traceability partially implemented.
REMAINING - Traceability

**30. Is hands-on with SOLID & Other Principles?**  
Yes – The weather app project demonstrates hands-on application of SOLID and other clean code principles:

- **Single Responsibility Principle (SRP):** Each class has a clear responsibility, e.g., `WeatherCacheServiceImpl` handles caching logic, `WeatherSvcClient` handles API calls.
- **Open/Closed Principle (OCP):** System is easily extendable without modifying existing code; e.g., new cache refresh strategies can be added via `CacheRefreshStrategyFactory`.
- **Liskov Substitution Principle (LSP):** Subclasses or implementations of `CacheRefreshStrategy` (LRU, LFU) can replace the abstract type without breaking behavior.
- **Interface Segregation Principle (ISP):** Interfaces are small and specific; classes implement only relevant methods.
- **Dependency Inversion Principle (DIP):** High-level modules depend on abstractions (interfaces), not concrete implementations, allowing easy swapping of cache strategies or API clients.

The code also follows **DRY**, **KISS**, and modular design for readability, maintainability, and robust engineering practices.

**31. Is able to demonstrate IDE Fluency & Tooling?**  
Yes – Git integration, Lombok, Actuator, build automation, test coverage runs.

**32. Is hands-on with Design Patterns (Creational, Structural, Behavioral)?**  
Yes – the project implements multiple design patterns:
- **Creational (Singleton):** `WeatherCacheServiceImpl` and `SlidingWindowRateLimiter` use Singleton-like behavior to ensure single instance management.
- **Structural (Facade):** The Weather Cache app hides complexity of external API and caching behind a simple service interface.
- **Behavioral (Strategy & Observer):**
    - **Strategy:** Different cache refresh strategies (LRU, LFU) are selected via `CacheRefreshStrategyFactory`.
    - **Observer:** Scheduler notifies services to refresh cached data automatically.
- Patterns ensure modularity, extensibility, and maintainability in code structure.
