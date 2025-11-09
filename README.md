# Überböse Api 🔈🎶

A Spring Boot API for Bose streaming services, providing XML-based endpoints for source providers.

## Features

- REST API with XML response support
- Custom Bose streaming media type (`application/vnd.bose.streaming-v1.2+xml`)
- OpenAPI 3.0.3 specification with code generation
- Docker containerization with Spring Boot buildpacks
- Automated CI/CD with GitHub Actions

## Development

### Prerequisites

- Java 21
- Maven 3.6+
- Docker (optional, for local container testing)

### Running the Application

```bash
# Generate OpenAPI code
mvn generate-sources

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

### API Endpoints

**Main Application (Port 8080):**
- `GET /streaming/sourceproviders` - Returns list of source providers in XML format
- All other requests are proxied to the configured target hosts based on content:
  - Auth-related requests (containing "auth" anywhere) → Auth target host
  - All other requests → Default target host

**Management/Actuator Endpoints (Port 8081):**
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/env` - Environment properties
- `GET /actuator/loggers` - Logging configuration

### Docker

#### Local Docker Build

```bash
# Build Docker image using Spring Boot buildpacks
mvn spring-boot:build-image

# Run the container (expose both main and management ports)
docker run -p 8080:8080 -p 8081:8081 ueberboese-api:0.0.1-SNAPSHOT
```

#### GitHub Container Registry

Docker images are automatically built and pushed to GitHub Container Registry (GHCR) via GitHub Actions:

- **Image location**: `ghcr.io/USERNAME/REPOSITORY_NAME`
- **Tags**:
  - `latest` (main branch)
  - `branch-name` (feature branches)
  - `sha-COMMIT_HASH` (all commits)

### CI/CD Pipeline

The project uses a unified GitHub Actions workflow (`ci-cd.yml`) for continuous integration and deployment:

1. **Test Application Job**: Runs on all pushes and pull requests
   - Sets up Java 21 with Maven caching
   - Generates OpenAPI sources
   - Builds and tests the project with Maven
   - Uploads test results and JAR artifacts

2. **Build & Push Docker Image Job**: Runs on pushes to main/develop branches (not on PRs)
   - Builds Docker image using Spring Boot buildpacks
   - Pushes to GitHub Container Registry
   - Tags images appropriately based on branch/commit

#### Required GitHub Settings

For the CI/CD pipeline to work, ensure your GitHub repository has:

1. **Actions enabled**: Go to repository Settings → Actions → General
2. **Packages permissions**: The workflow uses `GITHUB_TOKEN` with `packages: write` permission (automatically available)
3. **Container registry access**: No additional secrets needed, uses built-in `GITHUB_TOKEN`

#### Running Your Container

After the pipeline runs, pull and run your image:

```bash
# Login to GHCR (if repository is private)
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull and run the latest image
docker pull ghcr.io/USERNAME/REPOSITORY_NAME:latest
docker run -p 8080:8080 -p 8081:8081 ghcr.io/USERNAME/REPOSITORY_NAME:latest
```

#### Docker Compose

For easier deployment and configuration, you can use Docker Compose:

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  ueberboese-api:
    container_name: ueberboese-api
    image: ghcr.io/julius-d/ueberboese-api:latest
    ports:
      - "8080:8080"      # Main application
      - "8081:8081"      # Management/Actuator endpoints
    environment:
      # Configure the target hosts for proxy requests
      - PROXY_TARGET_HOST=https://your-target-host.com
      - PROXY_AUTH_TARGET_HOST=https://auth.your-target-host.com
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

```

**Usage**:
```bash
# Start the services
docker compose up -d

# View logs
docker compose logs -f ueberboese-api

# Stop the services
docker compose down

# Update to latest image and restart
docker compose pull && docker compose up -d
```

**Configuration Options**:

The application supports the following environment variables:

| Variable                           | Default                | Description                                                 |
|------------------------------------|------------------------|-------------------------------------------------------------|
| `PROXY_TARGET_HOST`                | `https://example.org`  | Default target host for proxying unknown requests          |
| `PROXY_AUTH_TARGET_HOST`           | -                      | Auth-specific target host for requests containing "auth"   |
| `SPRING_PROFILES_ACTIVE`           | -                      | Active Spring profiles (e.g., `production`, `development`) |
| `SERVER_PORT`                      | `8080`                 | Port the main application runs on                          |
| `MANAGEMENT_SERVER_PORT`           | `8081`                 | Port for actuator/management endpoints                     |
| `LOGGING_LEVEL_COM_GITHUB_JULIUSD` | `INFO`                 | Logging level for application packages                     |

### Testing

The project includes comprehensive tests using REST Assured:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UeberboeseControllerTest
```
