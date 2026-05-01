# 🚀 Bulk Email Architecture: Resilient & Decoupled Background Processing

This project demonstrates a production-grade **Decoupled Background Task Architecture** built with **Spring Boot 3**, **RabbitMQ**, and **Redis**. It solves the common backend bottleneck of API latency during heavy workloads (like bulk emailing) by transitioning from synchronous execution to an asynchronous, event-driven pattern.

---

## 💡 The Core Problem: API Latency
Processing 1,000+ emails in a single request makes an API slow, prone to timeouts, and impossible to scale. This project re-architects the system so the API remains lightning-fast while the heavy lifting happens reliably in the background.

## 🏗️ Architecture Overview
The system uses a multi-layered approach to ensure every message is processed without blocking the user.

### 1. Client & API Layer (Accept Fast)
*   **Non-Blocking**: The controller validates the request and immediately returns `202 Accepted`.
*   **Decoupled**: The API doesn't wait for the email service; it only ensures the task is safely queued.

### 2. Dual-Messaging Strategy
*   **Redis (Speed Layer)**: Utilizes `opsForList()` for high-speed, lightweight task buffering—perfect for simple notifications.
*   **RabbitMQ (Reliability Layer)**: Uses a **Direct Exchange** and **Durable Queues** for mission-critical tasks requiring guaranteed delivery.

### 3. Resilience & Failure Handling 
*   **Throttling (Backpressure)**: Workers use a **Prefetch Count of 5** to avoid overwhelming the CPU or Database.
*   **Exponential Backoff**: If an external service (like an SMTP server) is down, the system retries at increasing intervals (2s, 4s, 8s).
*   **Dead Letter Queue (DLQ)**: After all retries are exhausted, a `RejectAndDontRequeueRecoverer` moves the message to a DLQ for manual inspection, ensuring zero data loss.

---

## 🛠️ Technical Stack
*   **Java 21**
*   **Spring Boot 3**
*   **RabbitMQ** (Spring AMQP)
*   **Redis** (Spring Data Redis)
*   **Lombok & SLF4J**
