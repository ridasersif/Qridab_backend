# Qridaba Backend Platform

Qridaba is an innovative digital platform for peer-to-peer and professional equipment and tool rentals.

## 🚀 Technology Stack
- **Java 17**
- **Spring Boot 3.4.0** (Downgraded for stability)
- **PostgreSQL 16**
- **MapStruct 1.5.5** (High-performance mapping)
- **Lombok** (Boilerplate reduction)
- **JWT (jjwt)** (Secure authentication)
- **Docker & Docker Compose**

## 🛠️ Setup & Installation

### 1. Environment Configuration
Create a `.env` file in the root directory (use `.env.example` as a template):
```env
POSTGRES_USER=qridaba_user
POSTGRES_PASSWORD=secret_password_123
POSTGRES_DB=qridaba_db
JWT_SECRET_KEY=your_256_bit_secret
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_app_password
```

### 2. Start the Application
```bash
docker-compose -f docker-compose.dev.yml up -d --build
```
*Port 8085: Backend | Port 5433: DB | Port 5051: pgAdmin*

## 🔐 Authentication Flow

The system uses a **Deferred Persistence** strategy with **Email Verification**:

1.  **Registration**: User submits details. A `PendingUser` is created, and an OTP is sent via email. No account is created in the main `users` table yet.
2.  **Verification**: User submits the OTP. If valid, the `PendingUser` data is moved to the `users` table, and account is enabled.
3.  **Login**: User authenticates to get an **Access Token** (JWT) and a **Refresh Token** (Stored in DB).
4.  **Token Refresh**: Use the Refresh Token to get a new Access Token without re-logging in.

## 📡 API Endpoints

### Auth Module (`/api/v1/auth`)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/register` | `POST` | Start registration (sends OTP) |
| `/verify-email` | `POST` | Verify OTP and finalize registration |
| `/login` | `POST` | Login and get tokens |
| `/refresh-token` | `POST` | Renew Access Token using Refresh Token |
| `/logout` | `POST` | Clear security context |

## 🛠️ Key Technical Features
- **Soft Delete**: Entities are flagged as deleted rather than removed from DB.
- **JPA Auditing**: Automatic tracking of `createdAt`, `updatedAt`, and `createdBy`.
- **Database-backed Refresh Tokens**: Secure session management with revocation support.
- **Secure Configuration**: Critical credentials are never committed to Git, managed via `.env`.
- **Global Error Handling**: Standardized professional JSON error responses.

## 🧪 Testing with Postman
A complete Postman guide and collection structure can be found in `C:\Users\pc\.gemini\antigravity\brain\... (artifact)`.
Short summary:
1. `POST /register` -> Check your email for code.
2. `POST /verify-email` (with code) -> Get your tokens.
3. `POST /login` -> Get tokens again anytime.
4. `POST /refresh-token` (with refresh_token) -> Get a new access_token.
