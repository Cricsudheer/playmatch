# Playmatch — Spring Boot 3 (Java 17) Skeleton

Production-grade backend skeleton built with **Spring Boot 3**, **Java 17**, **PostgreSQL**, **Redis**, **Maven**, and **Docker**.  
Includes:
- Structured logging (Logback JSON + Correlation ID)
- Stateless JWT security filter (skeleton only, no business logic)
- OpenAPI/Swagger docs + Actuator
- Dockerfile (multi-stage) + Docker Compose (infra or full stack)

⚠️ **Note**: This is only setup/scaffolding. No controllers/services/repositories are included.

---

## 📂 Tech Stack
- **Runtime**: Java 17, Spring Boot 3.x
- **Build**: Maven + Spring Boot layered JAR
- **Database**: PostgreSQL 16 (via Docker)
- **Cache**: Redis 7 (via Docker)
- **Docs**: springdoc-openapi
- **Security**: JWT skeleton, BCrypt
- **Logging**: Logback JSON (MDC correlationId)
- **Containerization**: Dockerfile (multi-stage), Docker Compose

---

## ⚙️ Prerequisites
- Java 17 (JDK)
- Maven (or included `mvnw` wrapper)
- Docker Desktop (Windows/macOS) or Docker Engine (Linux)
- IntelliJ IDEA (recommended for dev)

---

## 📁 Project Structure
```
src/main/java/com/example/app
├── AppApplication.java
├── config
│   ├── OpenApiConfig.java
│   └── RedisConfig.java
├── logging
│   └── CorrelationIdFilter.java
└── security
    ├── JwtAuthenticationFilter.java
    ├── JwtUtils.java
    └── SecurityConfig.java

src/main/resources
├── application.properties
├── application-dev.properties
├── application-prod.properties
└── logback-spring.xml
```

---

## 🔑 Profiles
- **dev** → Run from IntelliJ, connects to Postgres & Redis in Docker on **localhost**
- **prod** → Runs inside Docker container, uses service names (`postgres`, `redis`)

---

## 🐳 Docker Setup

### Dockerfile (multi-stage)
Builds the Spring Boot fat JAR and runs on JRE 17 Alpine.

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine
ENV TZ=UTC JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
```

`.dockerignore`
```
target
.git
.idea
*.iml
.DS_Store
```

---

### Docker Compose (infra only for dev)

```yaml
version: "3.9"
services:
  postgres:
    image: postgres:16-alpine
    container_name: playmatch-postgres
    environment:
      POSTGRES_DB: playmatchdb
      POSTGRES_USER: playmatch
      POSTGRES_PASSWORD: playmatch
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: playmatch-redis
    command: ["redis-server", "--appendonly", "yes"]
    ports:
      - "6379:6379"
    volumes:
      - redisdata:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

volumes:
  pgdata:
  redisdata:
```

### Docker Compose (full stack including app)
```yaml
app:
  build: .
  image: playmatch:latest
  container_name: playmatch-service
  depends_on:
    postgres:
      condition: service_healthy
    redis:
      condition: service_healthy
  environment:
    SPRING_PROFILES_ACTIVE: prod
    DB_URL: jdbc:postgresql://postgres:5432/playmatchdb
    DB_USERNAME: playmatch
    DB_PASSWORD: playmatch
    REDIS_HOST: redis
    REDIS_PORT: 6379
    JWT_SECRET: ${JWT_SECRET:-local-dev-secret-123456-123456-123456}
    JAVA_OPTS: "-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
  ports:
    - "8080:8080"
  restart: unless-stopped
```

---

## 🚀 How to Run

### Option 1 — Local Dev (recommended)
1. Start infra (Postgres + Redis):
   ```bash
   docker compose up -d postgres redis
   ```
2. Run app in IntelliJ:  
   - Run Config → Spring Boot → Active profile: `dev`
   - JRE: 17
3. Visit:
   - Swagger UI → `http://localhost:8080/swagger-ui/index.html`
   - Health → `http://localhost:8080/actuator/health`

---

### Option 2 — All in Docker
```bash
docker compose up --build -d
docker compose logs -f app
```
Visit:  
- Swagger UI → `http://localhost:8080/swagger-ui/index.html`  
- Health → `http://localhost:8080/actuator/health`

---

### Option 3 — Manual build & run
```bash
./mvnw clean package -DskipTests
docker build -t playmatch:latest .
docker run --rm -p 8080:8080   -e SPRING_PROFILES_ACTIVE=prod   -e DB_URL=jdbc:postgresql://localhost:5432/playmatchdb   -e DB_USERNAME=playmatch -e DB_PASSWORD=playmatch   -e REDIS_HOST=localhost -e REDIS_PORT=6379   -e JWT_SECRET=local-dev-secret-123456-123456-123456   playmatch:latest
```

---

## 📖 Endpoints
- Health: `GET /actuator/health`
- Swagger: `GET /swagger-ui/index.html`
- OpenAPI: `GET /v3/api-docs`

---

## 🔐 Security
- JWT filter skeleton (parses `Authorization: Bearer <token>`)
- BCryptPasswordEncoder bean
- Permits: `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**`
- Everything else requires auth
- Dev secret in `application-dev.properties` → override with `JWT_SECRET` in prod

---

## 🛠 Common Issues
- **`database "<name>" does not exist`** → Align `POSTGRES_DB` in docker-compose with `spring.datasource.url` DB name.
- **Port already in use** → Change host port mapping (`5432:5432` → `55432:5432`) and update JDBC URL.
- **Dialect errors** → Ensure:
  ```properties
  spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
  spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false
  ```

---

## ✅ Quick Start (TL;DR)
```bash
docker compose up -d postgres redis   # start infra
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # run app (IntelliJ or CLI)
```
App available at → `http://localhost:8080/swagger-ui/index.html`
