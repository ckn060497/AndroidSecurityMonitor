FROM eclipse-temurin:17-jre
WORKDIR /app
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY backend/target/phone-security-api-1.0.0.jar app.jar

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "app.jar"]
