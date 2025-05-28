# Chatcord Server

**Spring Boot Backend for Chatcord Desktop Chat Application**

This is the **backend service** for Chatcord — a modern desktop chat application. The server handles authentication, real-time messaging, user management, and security policies. It follows professional-grade software architecture with clean code, secure authentication, and robust WebSocket-based communication.

---

## 🚀 Features

- **OAuth2 Authentication with Custom Logic**  
  No third-party identity providers (like Keycloak). Built-in custom OAuth2 flow using:
  - JWT access and refresh tokens  
  - RSA public/private key pair for token signing/verification  
  - Device-based session management

- **JWT Token Management**
  - Access token (short-lived)  
  - Refresh token (long-lived)  
  - Secure claim mapping and decoding  
  - Redis integration for session control

- **WebSocket-Based Messaging**
  - Spring Messaging + STOMP - **WIP**
  - Real-time private and group messaging **WIP**
  - Dynamic routing based on chat/contact sessions  
  - Authentication handshake using JWT

- **User & Profile Management**
  - One-to-one profile per account
  - Bidirectional relationships and cascade configurations  
  - OTP-based device verification flow  
  - Email verification flow included

- **Clean Architecture**
  - DTO-layer abstraction  
  - RESTful APIs with clear endpoints  
  - Custom exceptions and centralized handling  
  - 3NF-normalized MySQL schema using JPA/Hibernate  
  - Initially used MySQL, migrated to MS SQL Server, and planning to use Liquibase for future database versioning and management

---

## 🧰 Technologies Used

- Java 17+  
- Spring Boot 3.x (3.4.4)  
- Spring Security + OAuth2 Resource Server  
- WebSocket (STOMP)  
- JPA + MySQL → MS SQL Server (migrated)  
- Redis (via Docker)  
- JWT (Nimbus)  
- Maven  
- Docker (for Redis)  
- Proxmox (for deploying server containers on Linux)

---

## 🧱 Project Structure

```
com.chatcord.server
├── config          # Security & JWT config
├── controller      # REST & WebSocket controllers
├── dto             # DTOs for requests/responses
├── entity          # JPA entities (User, Chat, Message, etc.)
├── repository      # Spring Data repositories
├── security        # Token service, filters, encoders
├── service         # Business logic layer
├── util            # Utility classes (RequestUtils, etc.)
```

---

## 📄 How to Run

1. Set up MS SQL Server and Redis (use Docker)  
2. Clone the backend repo:
```bash
git clone https://github.com/Mu7ammedProg159/Chatcord-Server.git
cd Chatcord-Server
```
3. Add RSA keys in `/resources/keys/` << Currently added by default (Dangerous but for show purposes)
4. Configure `application.properties` with DB & Redis credentials  
5. Start the backend:
```bash
./mvnw spring-boot:run
```

---

## 🧳 Deployment

- Pre-configured to run as either a `server.jar` or deployed on a Tomcat server  
- Compatible with production-grade environments  
- Redis managed via Docker, backend containerized using Proxmox VMs on Linux

---

## 📌 Note

This backend was developed with scalability and security in mind. It serves as a professional-grade messaging backend with clean entity design, stateless JWT-based auth, Redis session management, and advanced OAuth2 configuration without relying on external identity providers.

---

## 📄 License

MIT License — (c) 2025 Mu7ammedDev159
