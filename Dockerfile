# Stage 1: Build the JAR
FROM maven:3.9.11-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR without running tests
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/expensetracker-0.0.1-SNAPSHOT.jar expensetracker-v0.jar

# Expose application port
EXPOSE 9090

# Run the JAR
ENTRYPOINT ["java","-jar","expensetracker-v0.jar"]