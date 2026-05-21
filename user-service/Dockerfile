# Multi-stage build. Build context is the project root (parent pom included).
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Parent pom references all modules — every module's pom.xml must exist.
COPY pom.xml ./
COPY user-service/pom.xml ./user-service/
COPY theatre-service/pom.xml ./theatre-service/
COPY booking-service/pom.xml ./booking-service/
COPY api-gateway/pom.xml ./api-gateway/

COPY user-service/src ./user-service/src

RUN mvn -B -pl user-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/user-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-Xmx300m", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]
