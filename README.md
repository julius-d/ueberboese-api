# Überböse Api 🔈🎶
**Überböse:** /ˈyːbɐˌbøːzə/ *(german) adjective - extremely or supremely evil; beyond ordinary wickedness.*

Bose has announced the end of life for its consumer streaming boxes called SoundTouch ☹️
This will render millions of completely working streaming boxes useless.

The aim of this project is to make sure that SoundTouch boxes can still be used for a long time.

The idea to achieve that is to reverse-engineer and rebuild the Bose streaming HTTP API.

## Documentation

**Full documentation is available at: https://julius-d.github.io/ueberboese-api/**

- [Quick Start Guide](https://julius-d.github.io/ueberboese-api/quick-start) - Step-by-step installation and configuration
- [Configuration Reference](https://julius-d.github.io/ueberboese-api/quick-start#configuration-reference) - Environment variables and settings

## Features

- Easy installation with Docker Compose
- Spotify OAuth integration support
- Self-hosted and open source
- Comprehensive logging for API research

## Installation

See the [Quick Start Guide](https://julius-d.github.io/ueberboese-api/quick-start).

## Researching the API

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
