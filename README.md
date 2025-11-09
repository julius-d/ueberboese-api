# Überböse Api 🔈🎶
**Überböse:** /ˈyːbɐˌbøːzə/ *adjective — extremely or supremely evil; beyond ordinary wickedness.*

Bose has announced the end of life for its consumer streaming boxes called SoundTouch ☹️
This will render millions of completely working streaming boxes useless.

The aim of this project is to make sure that SoundTouch can still be used for a long time.

The idea on how to achieve that is to revers-engineer and rebuild the bose streaming http api.



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

- **Image location**: `ghcr.io/julius-d/ueberboese-api`
- **Tags**:
  - `X.Y.Z` (semantic version - automatically calculated)
  - `latest` (main branch)
  - `branch-name` (feature branches)
  - `sha-COMMIT_HASH` (all commits)

### CI/CD Pipeline

The project uses a unified GitHub Actions workflow (`ci-cd.yml`) for continuous integration and deployment with **automatic semantic versioning**:

1. **Semantic Versioning Job**: Calculates version based on conventional commits
   - Analyzes commit messages following [Conventional Commits](https://www.conventionalcommits.org/)
   - Determines next semantic version (MAJOR.MINOR.PATCH)
   - Available for use in subsequent jobs

2. **Test Application Job**: Runs on all pushes and pull requests
   - Sets up Java 21 with Maven caching
   - Generates OpenAPI sources
   - Builds and tests the project with Maven using calculated semantic version
   - Uploads test results and JAR artifacts

3. **Build & Push Docker Image Job**: Runs on pushes to main/develop branches (not on PRs)
   - Builds Docker image using Spring Boot buildpacks with semantic version
   - Pushes to GitHub Container Registry
   - Tags images with semantic version, commit SHA, and branch-specific tags

#### Required GitHub Settings

For the CI/CD pipeline to work, ensure your GitHub repository has:

1. **Actions enabled**: Go to repository Settings → Actions → General
2. **Packages permissions**: The workflow uses `GITHUB_TOKEN` with `packages: write` permission (automatically available)
3. **Container registry access**: No additional secrets needed, uses built-in `GITHUB_TOKEN`

#### Semantic Versioning with Conventional Commits

The project uses [Conventional Commits](https://www.conventionalcommits.org/) for automatic semantic versioning. Commit messages should follow this format:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Version bumping rules:**
- `feat:` → Minor version bump (new feature)
- `fix:` → Patch version bump (bug fix)
- `BREAKING CHANGE:` or `!` → Major version bump (breaking change)
- Other types (`docs:`, `style:`, `refactor:`, `test:`, `chore:`) → No version bump

**Examples:**
```bash
git commit -m "feat: add user authentication endpoint"     # 1.1.0
git commit -m "fix: resolve null pointer in proxy service" # 1.0.1
git commit -m "feat!: change API response format"          # 2.0.0
git commit -m "docs: update README with new examples"      # No version bump
```

The calculated semantic version is automatically:
- Applied to the Maven build (`pom.xml` uses `${revision}`)
- Used for Docker image tagging in GitHub Container Registry
- Stored in the built JAR file manifest

#### Running Your Container

After the pipeline runs, pull and run your image:

```bash
# Login to GHCR (if repository is private)
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull and run the latest image
docker pull ghcr.io/julius-d/ueberboese-api:latest
docker run -p 8080:8080 -p 8081:8081 ghcr.io/julius-d/ueberboese-api:latest
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
    user: "${UID:-1000}:${GID:-1000}"  # Run as current user to avoid permission issues
    ports:
      - "8080:8080"      # Main application
      - "8081:8081"      # Management/Actuator endpoints
    environment:
      # Configure the target hosts for proxy requests
      - PROXY_TARGET_HOST=https://your-target-host.com
      - PROXY_AUTH_TARGET_HOST=https://auth.your-target-host.com
    volumes:
      # Persist application logs on the host system
      - ~/ueberboeselogs:/workspace/logs
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
# Login to GitHub Container Registry (if using private repo)
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Create logs directory on host
mkdir -p ~/ueberboeselogs

# Set user ID for proper permissions (Linux/macOS)
export UID=$(id -u)
export GID=$(id -g)

# Start the services
docker compose up -d

# View logs (Docker container logs)
docker compose logs -f ueberboese-api

# View application logs (persistent log files)
tail -f ~/ueberboeselogs/proxy-requests.log

# Stop the services
docker compose down

# Update to latest image and restart
docker compose pull && docker compose up -d
```

**Persistent Logging**:

The docker-compose configuration includes a volume mount that persists application log files on the host system:
- **Host path**: `~/ueberboeselogs` (user's home directory)
- **Container path**: `/workspace/logs` (where the application writes log files)
- **Log files**: `proxy-requests.log` and other application logs will be persisted
- **Permissions**: Container runs as current user (`${UID}:${GID}`) to avoid permission issues

This ensures that log files are retained even when containers are stopped, restarted, or updated.

**Configuration Options**:

The application supports the following environment variables:

| Variable                           | Default               | Description                                                |
|------------------------------------|-----------------------|------------------------------------------------------------|
| `PROXY_TARGET_HOST`                | `https://example.org` | Default target host for proxying unknown requests          |
| `PROXY_AUTH_TARGET_HOST`           | -                     | Auth-specific target host for requests containing "auth"   |
| `SPRING_PROFILES_ACTIVE`           | -                     | Active Spring profiles (e.g., `production`, `development`) |
| `SERVER_PORT`                      | `8080`                | Port the main application runs on                          |
| `MANAGEMENT_SERVER_PORT`           | `8081`                | Port for actuator/management endpoints                     |
| `LOGGING_LEVEL_COM_GITHUB_JULIUSD` | `INFO`                | Logging level for application packages                     |

### Testing

The project includes comprehensive tests using REST Assured:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UeberboeseControllerTest
```
