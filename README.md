# AI Code Assistant Backend

Spring Boot backend for analyzing code snippets and uploaded source files with OpenAI-backed bug finding and fallback local analysis.

## Stack

- Java 17
- Spring Boot
- Maven Wrapper
- OpenAI Java SDK

## What This Backend Does

- Accepts source code as JSON and returns bug analysis, fixes, explanations, edge cases, and complexity notes
- Accepts source files or zip uploads for analysis
- Uses OpenAI when `OPENAI_API_KEY` is configured
- Falls back to a local rule-based response when the API key is missing or the OpenAI call fails

## Project Structure

- `src/main/java/com/aibugfinder/backend/controller` - REST API controllers
- `src/main/java/com/aibugfinder/backend/service` - analysis logic and OpenAI client integration
- `src/main/java/com/aibugfinder/backend/dto` - request and response DTOs
- `src/main/resources/application.properties` - app configuration

## Requirements

- Java 17+
- Internet access for dependency download and OpenAI API calls

## Run Locally

### 1. Set the OpenAI API key

PowerShell, current terminal only:

```powershell
$env:OPENAI_API_KEY="your_real_key_here"
```

Permanent Windows user variable:

```powershell
[System.Environment]::SetEnvironmentVariable("OPENAI_API_KEY", "your_real_key_here", "User")
```

Important:
- Do not commit the real API key into the repository
- If you set the key permanently, reopen your terminal or IDE before starting the app again

### 2. Start the backend

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS or Linux:

```bash
./mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8081
```

## API Endpoints

Base path:

```text
/api
```

### POST `/api/analyze`

Analyze raw code sent in JSON.

Request body:

```json
{
  "code": "public class Demo { public static void main(String[] args) {} }",
  "language": "java"
}
```

Sample response shape:

```json
{
  "bugs": ["Possible misuse of '==' instead of equals()"],
  "fixedCode": "...",
  "explanation": "...",
  "edgeCasesToTest": ["..."],
  "timeComplexity": "...",
  "spaceComplexity": "...",
  "optimality": "...",
  "learningResources": ["..."]
}
```

### POST `/api/analyze/upload`

Analyze an uploaded source file or zip archive.

Form fields:
- `file` - required multipart file
- `language` - optional language override

Example with PowerShell:

```powershell
curl.exe -X POST "http://localhost:8081/api/analyze/upload?language=java" ^
  -F "file=@D:\path\to\BinarySearch.java"
```

## Frontend Team Notes

- The frontend should call this backend only
- The OpenAI API key must stay on the backend or deployment environment
- Do not place the OpenAI API key in frontend code, `.env` frontend files, or client-side requests
- If the backend is deployed centrally, frontend developers do not need the OpenAI key at all

## Configuration

Current app settings live in `src/main/resources/application.properties`:

```properties
spring.application.name=demo
openai.model=gpt-5-mini
openai.api-key=${OPENAI_API_KEY}
server.port=8081
```

## Testing

Run tests with the Maven wrapper:

Windows:

```powershell
.\mvnw.cmd test
```

macOS or Linux:

```bash
./mvnw test
```

## Notes

- If `OPENAI_API_KEY` is invalid, the service returns fallback analysis instead of crashing
- Error messages are sanitized so API keys are not echoed back in responses
- CORS is enabled on the controller for frontend integration
