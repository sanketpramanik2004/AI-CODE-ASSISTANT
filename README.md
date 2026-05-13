# Code Quest AI

Code Quest AI is an AI-powered coding practice and code review platform. It combines a LeetCode-style DSA learning track with repository scanning, authentication, XP/streak progress, leaderboard ranking, and AI-generated feedback for failed submissions.

## Features

- Premium React frontend with landing page, auth pages, dashboard, learning tracks, problem editor, and repo scanner
- Spring Boot REST API with JWT authentication, refresh tokens, Google/GitHub OAuth support, and role-free protected endpoints
- Curated DSA roadmap with tracks such as Arrays, Strings, Binary Search, Recursion, Trees, Graphs, DP, Greedy, and more
- Monaco-powered code editor with Java, Python, JavaScript, TypeScript, C++, C, Go, and Rust options
- Local evaluator and optional Judge0/OpenAI integrations for code feedback
- AI-style failed-answer explanation with examples, edge cases, suggested code, and learning resources
- Repository/source upload scanner for bugs, fixes, complexity notes, test ideas, and explanations
- MySQL support for local development, with H2 fallback for quick tests

## Tech Stack

Backend:
- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- Maven Wrapper
- H2 or MySQL
- JWT + OAuth2 Client

Frontend:
- React 18
- React Router
- Create React App / React Scripts
- Monaco Editor
- Custom CSS design system

## Project Structure

```text
.
├── frontend/                         # React frontend
│   ├── src/App.jsx                   # Main app, routes, pages, API calls
│   ├── src/styles.css                # UI system and page styling
│   └── package.json
├── src/main/java/com/aibugfinder/backend/
│   ├── config/                       # Security, seed data, schema helpers
│   ├── controller/                   # REST controllers
│   ├── dto/                          # Request/response DTOs
│   ├── entity/                       # JPA entities
│   ├── repository/                   # Spring Data repositories
│   ├── security/                     # JWT, cookies, OAuth success handling
│   └── service/                      # Auth, learning, submissions, scanning
├── src/main/resources/
│   ├── application.properties
│   └── META-INF/additional-spring-configuration-metadata.json
├── src/test/                         # Backend tests
├── local.properties.example          # Local config template
└── pom.xml
```

## Prerequisites

- Java 17 or newer
- Node.js 18 or newer
- npm
- MySQL 8 if you want persistent local data

The app can also run with the default in-memory H2 database if MySQL is not configured.

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/sanketpramanik2004/AI-CODE-ASSISTANT.git
cd AI-CODE-ASSISTANT
```

### 2. Configure local properties

Copy the example file:

```powershell
Copy-Item local.properties.example local.properties
```

For MySQL, create a database:

```sql
CREATE DATABASE ai_code_assistant;
```

Then update `local.properties`:

```properties
DATABASE_URL=jdbc:mysql://localhost:3306/ai_code_assistant?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_mysql_password_here
FRONTEND_URL=http://localhost:3000
```

Keep `local.properties` private. It is ignored by Git.

### 3. Optional API keys

Add these only when you have real credentials:

```properties
OPENAI_API_KEY=your_openai_api_key_here
JUDGE0_BASE_URL=
JUDGE0_API_KEY=
```

OAuth is optional. If you configure Google or GitHub OAuth, add both client ID and client secret for that provider:

```properties
spring.security.oauth2.client.registration.google.client-id=your_google_client_id
spring.security.oauth2.client.registration.google.client-secret=your_google_client_secret
spring.security.oauth2.client.registration.google.scope=openid,email,profile

spring.security.oauth2.client.registration.github.client-id=your_github_client_id
spring.security.oauth2.client.registration.github.client-secret=your_github_client_secret
spring.security.oauth2.client.registration.github.scope=user:email,read:user
```

Do not leave OAuth client IDs blank in config files.

## Run The Backend

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Backend URL:

```text
http://localhost:8081
```

## Run The Frontend

```bash
cd frontend
npm install
npm start
```

Frontend URL:

```text
http://localhost:3000
```

The frontend calls the backend using:

```text
http://localhost:8081/api
```

You can override this in `frontend/.env`:

```properties
REACT_APP_API_BASE_URL=http://localhost:8081/api
```

## Main API Areas

Base path:

```text
/api
```

Auth:
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`

Learning:
- `GET /api/tracks`
- `GET /api/tracks/{id}`
- `GET /api/problems/{id}`

Submissions:
- `POST /api/submissions`

Leaderboard:
- `GET /api/leaderboard`
- `GET /api/leaderboard/me`

Repo/code scan:
- `POST /api/analyze`
- `POST /api/analyze/upload`

## Testing

Backend:

```powershell
.\mvnw.cmd test
```

Frontend production build:

```bash
cd frontend
npm run build
```

## Security Notes

- Never commit `local.properties`
- Never put OpenAI, Judge0, Google, GitHub, or database secrets in frontend code
- OAuth credentials should stay in `local.properties` or deployment environment variables
- `local.properties.example` contains placeholders only

## Current Status

Code Quest AI includes the full local learning flow, auth flow, repo scanner UI, premium landing page, redesigned login/signup pages, and a modern dashboard experience. The backend tests and frontend production build should pass before pushing changes.
