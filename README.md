# Real-Time Notification System

A backend system built with Spring Boot for delivering notifications in real-time using WebSocket communication, asynchronous queue processing, and retry mechanisms for reliability.

---

## 🚀 Features

* REST APIs for notification creation and retrieval
* WebSocket-based real-time delivery to connected users
* In-memory queue for asynchronous processing
* Retry mechanism for handling failed deliveries
* Rate limiting to prevent abuse
* Structured logging and centralized error handling

---

## 🏗️ Architecture

```
Client → REST API → Queue → Worker → WebSocket → User
```

### Flow Explanation

1. Client sends request via REST API
2. Notification is stored and pushed to queue
3. Worker processes queue asynchronously
4. System attempts delivery via WebSocket
5. If user is offline → remains pending
6. Retry mechanism ensures delivery reliability

---

## 📡 API Endpoints

### Create Notification

`POST /api/notifications`

```json
{
  "recipientId": "user123",
  "title": "Hello",
  "message": "Test message"
}
```

---

### Get All Notifications for User

`GET /api/notifications?recipientId={id}`

---

### Get Notification by ID

`GET /api/notifications/{id}`

---

### Update Notification Status

`PUT /api/notifications/{id}/status`

---

### Health Check

`GET /api/health`

```json
{
  "status": "UP"
}
```

---

## 🔄 Notification Lifecycle

```
PENDING → SENT → DELIVERED
           ↓
         FAILED
```

---

## ⚡ Real-Time Delivery

* WebSocket endpoint: `/ws?userId={id}`
* If user is online → notification delivered instantly
* If user is offline → stored and delivered on reconnect

---

## 🔁 Retry Mechanism

* Retries failed deliveries up to 3 times
* Adds delay between retries
* Marks notification as FAILED if all retries fail

---

## 🛠️ Tech Stack

* Java (Spring Boot)
* WebSocket
* JPA / Hibernate
* MySQL / H2

---

## ▶️ Run Locally

```bash
git clone https://github.com/TejasSinha12/Real-Time-Notification-System.git
cd Real-Time-Notification-System

mvn clean install
mvn spring-boot:run
```

---

## 📌 Engineering Notes

* Queue-based processing decouples API requests from delivery logic, improving scalability
* Retry handling ensures reliability in case of temporary failures
* Rate limiting protects the system from excessive requests
* Clean architecture separates concerns across controller, service, and repository layers

---

## 📄 License

This project is open-source and available under the MIT License.

---

## 👨‍💻 Author

**Tejas Sinha**
Backend Developer | System Builder

GitHub: https://github.com/TejasSinha12
