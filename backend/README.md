# Phone Security Monitor API

Spring Boot REST API for the Android Phone Security Monitor.

## Requirements

- Java 17+
- Maven 3.9+ (or Maven Wrapper if added)
- PostgreSQL for production (H2 is used by default for local development)

## Run locally

```bash
mvn spring-boot:run
```

Health check:

```text
GET http://localhost:8080/api/health
```

Expected:

```json
{"status":"UP","service":"Phone Security Monitor API"}
```

## Register a device

```http
POST /api/devices/register
Content-Type: application/json
```

Example:

```json
{
  "deviceKey": "android-demo-001",
  "deviceName": "My Phone",
  "androidVersion": "15",
  "manufacturer": "Example",
  "model": "Example Model",
  "appVersion": "1.0.0"
}
```

## Submit a scan

```http
POST /api/devices/{deviceId}/scan
Content-Type: application/json
```

Example:

```json
{
  "riskScore": 15,
  "riskLevel": "LOW",
  "reportJson": "{"accessibilityServices":[],"vpn":false,"deviceAdminApps":[]}"
}
```

## Scan history

```text
GET /api/devices/{deviceId}/scan
```

## Production

Set:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

The Android application should only send security telemetry that the user has authorized. This API does not remotely uninstall applications, disable security controls, or take control of the phone.

## AWS

The project can be deployed to AWS App Runner, Elastic Beanstalk, ECS, or another Java-compatible service. Build first:

```bash
mvn clean package -DskipTests
```

The resulting JAR is:

```text
target/phone-security-api-1.0.0.jar
```
