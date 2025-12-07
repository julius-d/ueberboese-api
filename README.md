# Überböse Api 🔈🎶
**Überböse:** /ˈyːbɐˌbøːzə/ *(german) adjective - extremely or supremely evil; beyond ordinary wickedness.*

Bose has announced the end of life for its consumer streaming boxes called SoundTouch ☹️
This will render millions of completely working streaming boxes useless.

The aim of this project is to make sure that SoundTouch boxes can still be used for a long time.

The idea to achieve that is to reverse-engineer and rebuild the Bose streaming HTTP API.

## Installation

1. The Docker image of this project needs to be deployed via Docker Compose (see below)
2. Make the running service available under 3 domains:
   - `ueberboese.your-example-host.org`
   - `ueberboeseoauth.your-example-host.org`
   - `ueberboese-downloads.your-example-host.org`

   Replace `your-example-host.org` with whatever you like.
   The domains do not need to be available on the public internet,
   but they must be resolvable in the local network where your SoundTouch boxes run.
3. Every SoundTouch box needs to be configured to use this API deployment
   - Collect the local IPs of the SoundTouch boxes (e.g.: 192.168.178.2)
   - Execute the following shell commands for every IP:
     - First, connect to the service port of the box via
       ```shell
       nc 192.168.178.2 17000
       ```
     - Then enter
       ```shell
       envswitch boseurls set https://ueberboese.your-example-host.org https://ueberboese-downloads.your-example-host.org
       ```
4. For Spotify auth support, additional steps need to be done:
   - Create an app in the [Spotify dashboard](https://developer.spotify.com/dashboard)
   - Configure `SPOTIFY_AUTH_CLIENT_ID` and `SPOTIFY_AUTH_CLIENT_SECRET` (see below)
   - For now, the creation of `SPOTIFY_AUTH_REFRESH_TOKEN` is manual and bumpy, but hey, it works!

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

### Docker

#### GitHub Container Registry

Docker images are automatically built and pushed to GitHub Container Registry (GHCR) via GitHub Actions:

- **Image location**: `ghcr.io/julius-d/ueberboese-api`
- **Tags**:
  - `X.Y.Z` (semantic version - automatically calculated)
  - `latest` (main branch)
  - `branch-name` (feature branches)
  - `sha-COMMIT_HASH` (all commits)

#### Docker Compose

For deployment and configuration, use Docker Compose:

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
      # OAuth is disabled by default
      - UEBERBOESE_OAUTH_ENABLED=false
      # Spotify API authentication (required for OAuth token refresh)
      - SPOTIFY_AUTH_CLIENT_ID=your-spotify-client-id
      - SPOTIFY_AUTH_CLIENT_SECRET=your-spotify-client-secret
      - SPOTIFY_AUTH_REFRESH_TOKEN=your-spotify-refresh-token
    volumes:
      # REQUIRED: Persist cached account data across container restarts
      - ~/ueberboese-data:/data
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

# Create required directories on host
mkdir -p ~/ueberboese-data
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

# View logs from running container
docker logs ueberboese-api
```

**Persistent Data and Logging**:

The Docker Compose configuration includes volume mounts that persist data and log files on the host system:

**Cached Account Data** (Required):
- **Host path**: `~/ueberboese-data` (user's home directory)
- **Container path**: `/data` (where the application caches account data)
- **Purpose**: Stores cached account XML files
- **Permissions**: Container runs as current user (`${UID}:${GID}`) to avoid permission issues

**Application Logs**:
- **Host path**: `~/ueberboeselogs` (user's home directory)
- **Container path**: `/workspace/logs` (where the application writes log files)
- **Log files**: `proxy-requests.log` and other application logs will be persisted
- **Permissions**: Container runs as current user (`${UID}:${GID}`) to avoid permission issues

This ensures that both cached data and log files are retained even when containers are stopped, restarted, or updated.

**Configuration Options**:

The application supports the following environment variables:

| Variable                            | Default                           | Description                                                                                                        |
|-------------------------------------|-----------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `PROXY_TARGET_HOST`                 | `https://streaming.bose.com`      | Default target host for proxying unknown requests                                                                  |
| `PROXY_AUTH_TARGET_HOST`            | `https://streamingoauth.bose.com` | Auth-specific target host for requests with Host header containing "auth"                                          |
| `PROXY_SOFTWARE_UPDATE_TARGET_HOST` | `https://downloads.bose.com`      | Software update target host for requests with Host header containing "downloads"                                   |
| `UEBERBOESE_DATA_DIRECTORY`         | `/data`                           | Directory for cached account data. **Must be mounted as volume for persistence!**                                  |
| `UEBERBOESE_OAUTH_ENABLED`          | `false`                           | Enable OAuth token endpoints (set to `true` to activate)                                                           |
| `UEBERBOESE_EXPERIMENTAL_ENABLED`   | `false`                           | Enable experimental endpoints (set to `true` to activate)                                                          |
| `SPOTIFY_AUTH_CLIENT_ID`            | -                                 | Spotify API client ID from [developer dashboard](https://developer.spotify.com/dashboard) (required for OAuth)     |
| `SPOTIFY_AUTH_CLIENT_SECRET`        | -                                 | Spotify API client secret from [developer dashboard](https://developer.spotify.com/dashboard) (required for OAuth) |
| `SPOTIFY_AUTH_REFRESH_TOKEN`        | -                                 | Spotify refresh token                                                                                              |
| `SPRING_PROFILES_ACTIVE`            | -                                 | Active Spring profiles (e.g., `production`, `development`)                                                         |
| `SERVER_PORT`                       | `8080`                            | Port the main application runs on                                                                                  |
| `MANAGEMENT_SERVER_PORT`            | `8081`                            | Port for actuator/management endpoints                                                                             |
| `LOGGING_LEVEL_COM_GITHUB_JULIUSD`  | `INFO`                            | Logging level for application packages                                                                             |

**OAuth Controller Configuration**:

The OAuth token refresh endpoint (`POST /oauth/device/{deviceId}/music/musicprovider/{providerId}/token/{tokenType}`)
is conditionally enabled via the `UEBERBOESE_OAUTH_ENABLED` environment variable.
This allows you to run the application with or without the **experimental** OAuth support.

When OAuth is enabled, you must also configure the Spotify API credentials:
- `SPOTIFY_AUTH_CLIENT_ID` - Your Spotify client ID from the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
- `SPOTIFY_AUTH_CLIENT_SECRET` - Your Spotify client secret from the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
- `SPOTIFY_AUTH_REFRESH_TOKEN` - Refresh token

### Researching the API

When running and using this Docker image, the log file folder will collect all requests that are made.
To get a simple statistic, run:
```bash
grep -r -h -o -P "Target URL: \K\S+" /path/to/your/log_folder | sort | uniq -c | sort -nr
```

This will return something like
```
    753 https://streaming.bose.com/streaming/account/6921042/full
     74 https://streamingoauth.bose.com/oauth/device/587A628A4042/music/musicprovider/15/token/cs3
     28 https://streaming.bose.com/streaming/account/6921042/device/587A628A4042/recent
      9 https://streaming.bose.com/streaming/account/6921042/device/587A628A4042/recents
      5 https://streaming.bose.com/streaming/support/power_on
      2 https://streaming.bose.com/?serialnumber=123123AW
```
