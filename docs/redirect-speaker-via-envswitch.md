---
layout: page
title: Redirect speaker via port 17000 (envswitch)
description: How to redirect a SoundTouch speaker's cloud URLs to the Überböse API using the built-in envswitch CLI on port 17000 — an alternative to editing the config file via SSH.
---

On some SoundTouch devices, port **17000** exposes a built-in CLI called `envswitch` that can redirect the speaker's cloud URLs. This is an alternative to editing `/opt/Bose/etc/SoundTouchSdkPrivateCfg.xml` via SSH, and is useful when USB stick access is difficult or unreliable.

Note: this covers the URL redirect step only. For the full advanced setup (Sources.xml, SSH access to persistent storage), the [USB stick method](advanced-set-up.md) is still required.

This was confirmed working on a **SoundTouch 10** (firmware `27.0.6.46330.5043500`).

## Step 1: Confirm the speaker is reachable

```bash
curl -s http://<SPEAKER_IP>:8090/info | grep margeURL
```

Expected output before setup:
```
<margeURL>https://streaming.bose.com</margeURL>
```

## Step 2: Confirm port 17000 is available

```bash
nmap -p 17000 <SPEAKER_IP>
```

If the port shows `open`, the envswitch CLI is available.

## Step 3: Redirect the speaker to your Überböse API

```bash
printf "envswitch boseurls set http://<UEBERBOESE_IP>:<PORT> http://<UEBERBOESE_IP>:<PORT>\r\n" \
  | nc -w 3 <SPEAKER_IP> 17000
```

Expected response:
```
->Setting Bose Server URLs to http://<UEBERBOESE_IP>:<PORT> and http://<UEBERBOESE_IP>:<PORT>
->OK
```

The two arguments set the **marge** (streaming) URL and the **stats** URL respectively.

## Step 4: Power cycle the speaker

The envswitch change does **not** take effect immediately. You must physically unplug the speaker, wait 5 seconds, and plug it back in.

There is no software reboot command available on port 17000.

## Step 5: Confirm the change

After the speaker comes back up:

```bash
curl -s http://<SPEAKER_IP>:8090/info | grep margeURL
```

Expected output after setup:
```
<margeURL>http://<UEBERBOESE_IP>:<PORT></margeURL>
```

## Notes

- Port 23 (telnet) and port 22 (SSH) remain **closed** — the envswitch method gives you URL redirection only, not a shell.
- If port 17000 is not open, fall back to the [USB stick method](advanced-set-up.md).
- The POWER key via the SoundTouch HTTP API (`POST /key`) only toggles standby — it does **not** trigger a real reboot.
