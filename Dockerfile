FROM eclipse-temurin:22-jre-alpine
WORKDIR /app
COPY target/assessmate.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]