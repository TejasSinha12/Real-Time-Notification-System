# Real-Time Notification System

A production-ready real-time notification system built with Spring Boot, featuring WebSocket support for instant delivery, in-memory queue processing, retry mechanism, and rate limiting.

## Features

- **RESTful API** for notification management
- **WebSocket** support for real-time delivery (`/ws?userId={id}`)
- **In-memory queue** for async notification processing
- **Retry mechanism** (up to 3 attempts)
- **Rate limiting** (10 notifications per minute per user)
- **Status tracking** (PENDING, SENT, DELIVERED, FAILED)
- **H2 Database** for local development (MySQL ready)

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring WebSocket
- Spring Data JPA
- H2 Database (MySQL compatible)
- Maven

## Architecture

```
API → Service → Queue → Worker → WebSocket → Update Status
```

1. **API Layer**: REST endpoints receive notification requests
2. **Service Layer**: Validates, saves to DB, pushes to queue
3. **Queue**: In-memory ConcurrentLinkedQueue for async processing
4. **Worker**: Background thread consumes queue and delivers via WebSocket
5. **WebSocket**: Real-time delivery to connected users

## API Endpoints

### Create Notification
```bash
POST /api/notifications
Content-Type: application/json

{
  "recipientId": "user123",
  "title": "New Message",
  "message": "You have a new message"
}
```

### Get Notifications by Recipient
```bash
GET /api/notifications?recipientId=user123
```

### Get Single Notification
```bash
GET /api/notifications/{id}
```

### Update Notification Status
```bash
PUT /api/notifications/{id}/status
Content-Type: application/json

{
  "status": "DELIVERED"
}
```

## WebSocket Connection

Connect to WebSocket endpoint:
```
ws://localhost:8080/ws?userId=user123
```

Subscribe to notifications:
```
/user/queue/notifications
```

## Project Structure

```
src/main/java/com/notification/
├── NotificationApplication.java
├── config/
│   ├── GlobalExceptionHandler.java
│   └── WebSocketConfig.java
├── controller/
│   └── NotificationController.java
├── dto/
│   ├── NotificationRequest.java
│   ├── NotificationResponse.java
│   └── StatusUpdateRequest.java
├── model/
│   ├── Notification.java
│   └── NotificationStatus.java
├── repository/
│   └── NotificationRepository.java
├── service/
│   ├── NotificationQueue.java
│   ├── NotificationService.java
│   └── NotificationWorker.java
└── websocket/
    ├── CustomWebSocketHandler.java
    ├── NotificationWebSocketHandler.java
    └── UserHandshakeInterceptor.java
```

## How to Run Locally

### Prerequisites
- Java 17+
- Maven 3.8+

### Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Using H2 Console (Optional)

Access H2 database console at: `http://localhost:8080/h2-console`

 JDBC URL: `jdbc:h2:mem:notificationdb`
 Username: `sa`
 Password: (empty)

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:notificationdb
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8080
```

For MySQL, update the datasource URL:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notificationdb
    username: root
    password: yourpassword
```

## Logging

The system logs:
- Notification created
- Delivered
- Failed
- Retried

Check logs for debugging and monitoring.

## Example Requests

### cURL Examples

```bash
# Create notification
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{"recipientId":"user1","title":"Hello","message":"Welcome!"}'

# Get all notifications for user
curl http://localhost:8080/api/notifications?recipientId=user1

# Get specific notification
curl http://localhost:8080/api/notifications/1

# Update status
curl -X PUT http://localhost:8080/api/notifications/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"DELIVERED"}'
```
