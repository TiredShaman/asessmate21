FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/AssessMate-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
