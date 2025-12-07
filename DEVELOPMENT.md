# Development

## Prerequisites

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

## Features

- REST API with XML response support
- Custom Bose streaming media type (`application/vnd.bose.streaming-v1.2+xml`)
- OpenAPI 3.0.3 specification with code generation
- Docker containerization with Spring Boot buildpacks
- Automated CI/CD with GitHub Actions

### Docker

#### Local Docker Build

```bash
# Build Docker image using Spring Boot buildpacks
mvn spring-boot:build-image

# Run the container (expose both main and management ports)
docker run -p 8080:8080 -p 8081:8081 ueberboese-api:0.0.1-SNAPSHOT
```


### CI/CD Pipeline

The project uses a GitHub Actions workflow (`ci-cd.yml`) for continuous integration and deployment with **automatic semantic versioning**:

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


*Supply Chain Security**:

All Docker images published to GitHub Container Registry include:

- **Build Provenance Attestations**: Cryptographically signed attestations that document how the image was built, including:
  - Build environment details
  - Git commit SHA and repository
  - Build workflow and runner information
  - Dependencies and build steps

- **SBOM (Software Bill of Materials) Attestations**: CycloneDX format SBOM containing:
  - Complete list of dependencies
  - Version information
  - License information
  - Component relationships

These attestations are signed using Sigstore and stored in the GitHub Attestations registry. They can be verified using:

```bash
# Verify build provenance
gh attestation verify oci://ghcr.io/julius-d/ueberboese-api:latest --owner julius-d

# View attestations
gh attestation list --owner julius-d
```

This provides transparency and allows you to verify the authenticity and integrity of the container images.


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

### Docker

#### GitHub Container Registry

Docker images are automatically built and pushed to GitHub Container Registry (GHCR) via GitHub Actions:

- **Image location**: `ghcr.io/julius-d/ueberboese-api`
- **Tags**:
  - `X.Y.Z` (semantic version - automatically calculated)
  - `latest` (main branch)
  - `branch-name` (feature branches)
  - `sha-COMMIT_HASH` (all commits)

#### Running Your Container

After the pipeline runs, pull and run your image:

```bash
# Login to GHCR (if repository is private)
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull and run the latest image
docker pull ghcr.io/julius-d/ueberboese-api:latest
docker run -p 8080:8080 -p 8081:8081 ghcr.io/julius-d/ueberboese-api:latest
```


### Testing

The project includes comprehensive tests using REST Assured:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UeberboeseControllerTest
```

### API Endpoints

**Main Application (Port 8080):**
- `GET /streaming/sourceproviders` - Returns a list of source providers in XML format
- `POST /streaming/account/{accountId}/device/{deviceId}/recent` - Add recent item to device history (XML format)
- `GET /streaming/account/{accountId}/full` - Experimental endpoint (requires `ueberboese.experimental.enabled=true`)
- `POST /oauth/device/{deviceId}/music/musicprovider/{providerId}/token/{tokenType}` - OAuth token refresh endpoint (JSON format, conditionally enabled)
- All other requests are proxied to the configured target hosts based on the Host header:
  - Auth-related requests (Host contains "auth") → Auth target host
  - Software update requests (Host contains "downloads") → Software update target host
  - All other requests → Default target host

**Management/Actuator Endpoints (Port 8081):**
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/env` - Environment properties
- `GET /actuator/loggers` - Logging configuration
