# Stage 1 — build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download deps first for layer caching
RUN mvn dependency:go-offline -B -q
COPY src ./src
# Skip tests here; CI already ran them
RUN mvn package -DskipTests -B -q

# Stage 2 — runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
