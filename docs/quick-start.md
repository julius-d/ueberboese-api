---
layout: page
title: Quick Start Guide
description: Step-by-step installation guide for Überböse API
---

Follow this guide to deploy Überböse API and configure your Bose SoundTouch devices.


## Step 1: Docker Deployment

Create a `docker-compose.yml` file (or extend your existing one):

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
      - TZ=Europe/Berlin
      # Spotify OAuth is disabled by default, you need it only for Spotify presets
      - UEBERBOESE_OAUTH_ENABLED=true
      # Spotify API authentication (required for OAuth token refresh)
      - SPOTIFY_AUTH_CLIENT_ID=your-spotify-client-id
      - SPOTIFY_AUTH_CLIENT_SECRET=your-spotify-client-secret
      # Management API Basic Auth credentials (change these!)
      - UEBERBOESE_MGMT_USERNAME=admin
      - UEBERBOESE_MGMT_PASSWORD=your-secure-password
    volumes:
      # REQUIRED: Persist cached account data across container restarts
      - ~/ueberboese-data:/data:rw
      # Persist application logs on the host system
      - ~/ueberboese-logs:/workspace/logs:rw
    restart: unless-stopped
```

### Create Required Directories

```bash
# Create directories on host
mkdir -p ~/ueberboese-data
mkdir -p ~/ueberboese-logs

# Set user ID for proper permissions (Linux/macOS)
export UID=$(id -u)
export GID=$(id -g)
```

### Start the Service

```bash
# Start the services
docker compose up -d

# View logs (Docker container logs)
docker logs ueberboese-api

# View application logs (persistent log files)
tail -f ~/ueberboese-logs/proxy-requests.log
```

## Step 2: Domain Setup

You need to configure three domains that point to your deployment server:

- `ueberboese.your-example-host.org`
- `ueberboeseoauth.your-example-host.org`
- `ueberboese-downloads.your-example-host.org`

**Important:** Replace `your-example-host.org` with whatever domain you like.
The domains do not need to be available on the public internet,
but they **must be resolvable** in the local network where your SoundTouch boxes run.

### Local DNS Configuration

Configure your local DNS server (router, Pi-hole, etc.) to point these domains to your server's IP address.


## Step 3: Device Configuration

Now you need to configure each SoundTouch device to use your Überböse API deployment.

### Find Device IP Addresses

Find the local IP addresses of your SoundTouch devices (e.g., `192.168.178.2`).
You can usually find these in your router's admin interface.
When you are already in the router's admin interface, make sure to select that you speaker always
get the same IP address assigned.

### Configure Each Device

For **each device**, run the following commands:

#### 1. Connect to Device Service Port

```bash
nc 192.168.178.2 17000
```

Replace `192.168.178.2` with your device's IP address.

#### 2. Execute Configuration Command

Once connected via netcat, enter:

```bash
envswitch boseurls set http://ueberboese.your-example-host.org:8080 http://ueberboese-downloads.your-example-host.org:8080/updates/soundtouch
```

Replace `your-example-host.org` with your actual domain.

Than type `exit` to leave the speaker
```bash
exit
```

### 3. Restart your speaker
After executing the configuration commands, unplug each speaker to restart the speaker.

#### 4. Verify Configuration

The device should acknowledge the configuration change. You can verify by checking the logs
- you should see requests coming from your SoundTouch device.

## Step 4: Install Überböse Android App (Optional)

Visit [Überböse companion app](https://github.com/julius-d/ueberboese-app)

[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="40">](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/julius-d/ueberboese-app)

## Step 5: Spotify OAuth (Optional)

If you want to use Spotify with your SoundTouch devices, follow these additional steps:

### Create Spotify App

1. Go to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Create a new app
   - App name: as you like. E.g.: ueberboese-api
   - App description: as you like. E.g.: Replacement for bose api, that will be shut down soon.
   - Website: blank, not needed
   - **Redirect URIs**: `ueberboese-login://spotify`
   - Bundle IDs: blank, not needed
   - Android packages: blank, not needed
   - **APIs used**:
     - Web API
     - Web Playback SDK

3. Note down your **Client ID** and **Client Secret**

### Configure Environment Variables

Update your `docker-compose.yml` to enable OAuth and add your Spotify credentials:

```yaml
environment:
  - UEBERBOESE_OAUTH_ENABLED=true
  - SPOTIFY_AUTH_CLIENT_ID=your-actual-client-id
  - SPOTIFY_AUTH_CLIENT_SECRET=your-actual-client-secret
```

### Connect your Spotify account

#### Option 1: Using the Überböse Companion App (Recommended)

The easiest way is to install and use the [Überböse companion App](https://github.com/julius-d/ueberboese-app)

[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="80">](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/julius-d/ueberboese-app)

#### Option 2: Using curl Commands

If you prefer to connect your Spotify account manually without the companion app, follow these steps:

**Step 1: Initialize the OAuth flow**

```bash
curl -u admin:your-secure-password \
  http://localhost:8080/mgmt/spotify/auth/init
```

This will return a JSON response with a redirect URL:

```json
{
  "redirectUrl": "https://accounts.spotify.com/authorize?client_id=...&response_type=code&redirect_uri=ueberboese-login://spotify&scope=..."
}
```

**Step 2: Open the authorization URL in your browser**

Copy the `redirectUrl` from the response and open it in your browser. You'll be prompted to log in to Spotify and authorize the application.

**Step 3: Extract the authorization code**

After authorizing, Spotify will redirect to `ueberboese-login://spotify?code=AUTHORIZATION_CODE`. Your browser will show an error (since this is a custom URL scheme), but you can copy the authorization code from the URL bar.

For example, from `ueberboese-login://spotify?code=AQBx7y...xyz`, copy the value after `code=`.

**Step 4: Complete the authentication**

Use the authorization code to complete the connection:

```bash
curl -u admin:your-secure-password \
  "http://localhost:8080/mgmt/spotify/auth/confirm?code=YOUR_AUTHORIZATION_CODE"
```

If successful, you'll receive a confirmation:

```json
{
  "success": true,
  "message": "Spotify account connected successfully",
  "accountId": "your_spotify_user_id"
}
```

**Verify the connection:**

```bash
curl -u admin:your-secure-password \
  http://localhost:8080/mgmt/spotify/accounts
```

This will list all connected Spotify accounts.

**Important:** You must connect at least one Spotify account via the management API or companion app before your SoundTouch devices can use Spotify. The API will automatically use the first connected account.

## Step 6 (optional)
Recommended for **working internet radio**


With the help of an USB-Stick you can take even more control of your speaker:

[Do the advanced setup](advanced-set-up.md)

## Troubleshooting

### Update to Latest Version

```bash
# Pull latest image and restart
docker compose pull && docker compose up -d
```

### Restart the Container

```bash
docker compose down
docker compose up -d
```

### View Logs

```bash
# View Docker container logs
docker logs ueberboese-api -f

# View persistent application logs
tail -f ~/ueberboese-logs/proxy-requests.log
```

### Check Container Health
```bash
# Check if container is running
docker ps

# Access health check endpoint directly
curl http://localhost:8081/actuator/health
```

### Common Issues

#### Device Not Connecting

- **Verify DNS resolution:** Ensure the three domains resolve to your server IP from the device's network
- **Verify device configuration:** Double-check the `envswitch` command was executed successfully

#### OAuth Not Working

- **Check credentials:** Verify your Spotify Client ID and Client Secret are correct
- **Enable OAuth:** Ensure `UEBERBOESE_OAUTH_ENABLED=true` in your environment variables
- **Check logs:** Look for OAuth-related errors in `docker logs ueberboese-api`

#### Permission Issues

- **Ensure volumes are writable:** The directories `~/ueberboese-data` and `~/ueberboese-logs` must be writable by the user running the container
- **Check user/group ID:** Verify `UID` and `GID` environment variables are set correctly

### Get Help

- **Report issues:** [GitHub Issues](https://github.com/julius-d/ueberboese-api/issues)
- **View source code:** [GitHub Repository](https://github.com/julius-d/ueberboese-api)

## Configuration Reference

### Environment Variables

| Variable                            | Default                           | Description                                                                      |
|-------------------------------------|-----------------------------------|----------------------------------------------------------------------------------|
| `PROXY_TARGET_HOST`                 | `https://streaming.bose.com`      | Default target host for proxying unknown requests                                |
| `PROXY_AUTH_TARGET_HOST`            | `https://streamingoauth.bose.com` | Auth-specific target host for requests with Host header containing "auth"        |
| `PROXY_SOFTWARE_UPDATE_TARGET_HOST` | `https://downloads.bose.com`      | Software update target host for requests with Host header containing "downloads" |
| `UEBERBOESE_DATA_DIRECTORY`         | `/data`                           | Directory for cached account data (must be mounted as volume!)                   |
| `UEBERBOESE_OAUTH_ENABLED`          | `false`                           | Enable OAuth token endpoints (set to `true` to activate)                         |
| `UEBERBOESE_MGMT_USERNAME`          | `admin`                           | Username for Basic Auth on `/mgmt/**` endpoints                                  |
| `UEBERBOESE_MGMT_PASSWORD`          | `change_me!`                      | Password for Basic Auth on `/mgmt/**` endpoints (change this!)                   |
| `SPOTIFY_AUTH_CLIENT_ID`            | -                                 | Spotify API client ID from developer dashboard (required for OAuth)              |
| `SPOTIFY_AUTH_CLIENT_SECRET`        | -                                 | Spotify API client secret from developer dashboard (required for OAuth)          |
| `UEBERBOESE_BMX_ENABLED`            | `false`                           | Enable BMX streaming endpoints (TuneIn, custom streams)                          |
| `UEBERBOESE_BMX_BASE_URL`           | `http://localhost:8080`           | Base URL for BMX services (used in service registry)                             |
| `SERVER_PORT`                       | `8080`                            | Port the main application runs on                                                |
| `MANAGEMENT_SERVER_PORT`            | `8081`                            | Port for actuator/management endpoints                                           |

### Persistent Data

The Docker Compose configuration includes volume mounts that persist data on the host:

- **`~/ueberboese-data`** → `/data` in container (cached account data - REQUIRED)
- **`~/ueberboese-logs`** → `/workspace/logs` in container (application logs)

This ensures that both cached data and log files are retained even when containers are stopped, restarted, or updated.

## Next Steps

Once your Überböse API is running and devices are configured:

1. **Test playback:** Try playing music through your SoundTouch device
2. **Monitor logs:** Keep an eye on `~/ueberboese-logs/proxy-requests.log` to see API requests
3. **Enable OAuth:** If you want Spotify support, follow Step 4 above
4. **Contribute:** This is an open-source project - contributions are welcome!

---

**Need more help?** Check out the [GitHub repository](https://github.com/julius-d/ueberboese-api) or [open an issue](https://github.com/julius-d/ueberboese-api/issues).
