# Stage 1: Build JAR
FROM maven:3.9.3-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/expensetracker-0.0.1-SNAPSHOT.jar expensetracker-v0.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","expensetracker-v0.jar"]
