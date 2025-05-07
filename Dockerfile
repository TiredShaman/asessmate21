# Build stage
FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/AssessMate-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
