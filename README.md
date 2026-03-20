# Real-Time Notification System

A Spring Boot backend for real-time notification delivery using WebSocket and in-memory queue processing.

## Overview

REST API that creates notifications and delivers them instantly via WebSocket. Uses an async queue with worker thread for reliable delivery and retry logic.

## Features

- RESTful API for notification CRUD
- WebSocket real-time delivery (`/ws?userId={id}`)
- In-memory queue with background worker
- Retry mechanism (3 attempts, 2s delay)
- Rate limiting (10/min per user)
- Status tracking: PENDING → SENT → DELIVERED/FAILED

## Architecture

```
Client → REST API → Service → Queue → Worker → WebSocket → Client
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/notifications` | Create notification |
| GET | `/api/notifications?recipientId={id}` | Get user's notifications |
| GET | `/api/notifications/{id}` | Get single notification |
| PUT | `/api/notifications/{id}/status` | Update status |
| GET | `/api/health` | Health check |

## Tech Stack

- Java 17 | Spring Boot 3.2 | Spring WebSocket | JPA | H2 | Maven

## Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

Server: `http://localhost:8080`

## Example Request

```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{"recipientId":"user1","title":"Hello","message":"Welcome!"}'
```

## WebSocket

Connect: `ws://localhost:8080/ws?userId=user1`  
Subscribe: `/user/queue/notifications`
