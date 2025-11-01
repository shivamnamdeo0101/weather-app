
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


lookfront : {
    "daily" : 1,
    "weekly" : 7,
    "month" : 30,
    "quarter": 90,
}
lookfront : {
    "daily" : 1,
    "weekly" : 7,
    "month" : 30,
    "quarter": 90,
}


# Code review against questions -  (from live casestudy or project sources or innersource/opensource/tools contributions) (in the sequence)

**1. Is the end user able to view results by changing the input parameters?**  
Yes – pass the city name in search bar to see dynamic results.

**2. Is the service ready to be released to production or live environment?**  
Yes – weather-cache service is production-ready with configuration, error handling, caching, and scheduler.
REMAINING  - prod/dev/test env setup with each env variables/tokens

**3. Is the service accessible via web browser or Postman (or using JS frameworks, HTML, or JSON)?**  
Yes – accessible via Postman or any HTTP client, returning JSON responses.

**4. Is the solution support offline mode with toggles?**  
REMAINING – toggle to force offline on frontend.

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
Yes – externalized configs, stateless services, console logging, separate environments.

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
Yes – Single Responsibility, Open/Closed, Dependency Inversion, Strategy Pattern used in Weather Cache app.

**31. Is able to demonstrate IDE Fluency & Tooling?**  
Yes – Git integration, Lombok, Actuator, build automation, test coverage runs.

**32. Is hands-on with Design Patterns (Creational, Structural, Behavioral)?**  
Yes – Creational (Singleton), Structural (Facade), Behavioral (Strategy for LRU/LFU, Observer for Scheduler).
