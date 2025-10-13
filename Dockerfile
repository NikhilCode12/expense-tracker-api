FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/expensetracker-0.0.1-SNAPSHOT.jar expensetracker-v0.jar

EXPOSE 9090

ENTRYPOINT ["java","-jar","expensetracker-v0.jar"]
