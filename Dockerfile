# One image, one Cloud Run service: Spring Boot serves the built frontend.
#   docker run <image>           → the web app (needs PROTECTION_MODE and SPRING_DATASOURCE_*)
#   docker run <image> migrate   → Flyway migrations, then exit (pipeline migrations step, CAAS-1380)
# The local seed (backend/src/local) is never copied into the jar.

FROM node:24-alpine AS frontend
WORKDIR /src
RUN corepack enable
COPY config/gates ./config/gates
COPY frontend/package.json frontend/pnpm-lock.yaml ./frontend/
WORKDIR /src/frontend
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm build

FROM eclipse-temurin:25-jdk AS backend
WORKDIR /src/backend
COPY backend/.mvn ./.mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY backend/src/main ./src/main
COPY --from=frontend /src/frontend/dist ./src/main/resources/static
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 app
WORKDIR /app
COPY --from=backend /src/backend/target/app.jar ./app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
