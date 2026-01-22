<div align="center">

# AutoViaUpdater v10.1.0

Keep your Via stack up‑to‑date —> automatically, safely, and on schedule.

![Platforms](https://img.shields.io/badge/Platforms-Spigot%20%7C%20Paper%20%7C%20Folia%20%7C%20Velocity%20%7C%20BungeeCord-5A67D8)
![MC](https://img.shields.io/badge/Minecraft-1.8%E2%86%92Latest-2EA043)
![Java](https://img.shields.io/badge/Java-8%2B-1F6FEB)
![License](https://img.shields.io/badge/License-MIT-0E8A16)

</div>

> TL;DR
> Drop in the jar ➜ it checks Jenkins and updates ViaVersion / ViaBackwards / ViaRewind (and ViaRewind‑Legacy on Spigot)
> on a schedule. Snapshots optional, DEV and Java8 jobs supported.

---

## Table of Contents

* [Highlights](#highlights)
* [What It Updates](#what-it-updates)
* [Platforms & Requirements](#platforms--requirements)
* [Installation](#installation)
* [Quick Start](#quick-start)
* [Configuration](#configuration)
    * [Spigot/Bungee (`config.yml`)](#spigotbungee-configyml)
    * [Velocity (`config.toml`)](#velocity-configtoml)
    * [Scheduling Cheat Sheet](#scheduling-cheat-sheet)
* [Commands & Permissions](#commands--permissions)
* [How It Works](#how-it-works)
* [Troubleshooting & FAQ](#troubleshooting--faq)
* [Building from Source](#building-from-source)
* [Changelog Highlights](#changelog-highlights)

---

## Highlights

* Automatic updates for the Via ecosystem from Jenkins.
* Snapshot handling you control: newest overall vs newest non‑snapshot.
* DEV and Java 8 job support per plugin.
* Simple interval or UNIX cron scheduling, plus a boot delay.
* Safe restarts: optional broadcast and delayed shutdown.

## What It Updates

* ViaVersion
* ViaBackwards
* ViaRewind
* ViaRewind Legacy Support (Spigot only)

## Platforms & Requirements

* Platforms: Spigot, Paper, Folia; Velocity; BungeeCord
* Minecraft: 1.8 → Latest
* Java: 8+

## Installation

1. Download the latest release from Spigot.
2. Place the jar in your server’s `plugins/` folder (on proxies, use the proxy’s `plugins/`).
3. Start the server to generate config files and `versions.yml`.
4. Adjust settings and restart.

## Quick Start

* Run `/updatevias` to check immediately.
* Leave `snapshot: true` to always take the newest build; set `false` to take the newest non‑snapshot.
* Flip `dev` or `java8` on a per‑plugin basis as needed.

## Configuration

Config files live here:

* Spigot/Bungee: `plugins/AutoViaUpdater/config.yml`
* Velocity: `plugins/AutoViaUpdater/config.toml`

### Spigot/Bungee (`config.yml`)

```yaml
ViaVersion:
  enabled: true
  snapshot: true   # newest overall; false = newest non-snapshot
  dev: false       # use Jenkins -DEV job
  java8: false     # use Jenkins -Java8 job

ViaBackwards:
  enabled: true
  snapshot: true
  dev: false
  java8: false

ViaRewind:
  enabled: true
  snapshot: true
  dev: false
  java8: false

ViaRewind-Legacy:
  enabled: true    # Spigot only
  snapshot: true
  dev: false       # DEV path uses "...%20Support%20DEV" under ViaRewind view

# Scheduling
Check-Interval: 60        # minutes; used when cron is blank
Cron-Expression: ""       # UNIX cron (5 fields); overrides interval when set
Delay: 5                  # seconds after boot before first check

# Optional safe restart after successful update
AutoRestart: false
AutoRestart-Delay: 60
AutoRestart-Message: '&cServer is restarting in 1 minute!'
```

### Velocity (`config.toml`)

```toml
Check-Interval = 60
Cron-Expression = ""
Delay = 5

AutoRestart = false
AutoRestart-Delay = 60
AutoRestart-Message = '&cServer is restarting in 1 minute!'

[ViaVersion]
enabled = true
snapshot = true
dev = false
java8 = false

[ViaBackwards]
enabled = true
snapshot = true
dev = false
java8 = false

[ViaRewind]
enabled = true
snapshot = true
dev = false
java8 = false

# ViaRewind-Legacy is Spigot-only; Velocity ignores it
```

### Scheduling Cheat Sheet

* Every 2 hours: `0 */2 * * *`
* Every day at 05:00: `0 5 * * *`
* Every 15 minutes: `*/15 * * * *`

If the cron is blank, the plugin uses `Check-Interval` (minutes) with an initial `Delay` (seconds).

## Commands & Permissions

* `/updatevias` — triggers an immediate check
* Permission: `autoviaupdater.admin` (required on Velocity/Bungee; OP on Spigot by default)

## How It Works

* The plugin calls the Jenkins API for each selected job.
* Selection:
    * Snapshot ON → newest build overall (regardless of "-SNAPSHOT").
    * Snapshot OFF → newest non‑snapshot build.
    * DEV/Java8 flags pick the `-DEV` / `-Java8` jobs when available.
* Download:
    * New jar goes to `plugins/`. If a matching jar exists, it is staged to `plugins/update/` for a clean swap on
      restart.
* Tracking:
    * Last installed build numbers are saved in `plugins/AutoViaUpdater/versions.yml`.

Jenkins shortcuts

* ViaVersion DEV: `https://ci.viaversion.com/job/ViaVersion-DEV/`
* ViaBackwards DEV: `https://ci.viaversion.com/view/ViaBackwards/job/ViaBackwards-DEV/`
* ViaRewind DEV: `https://ci.viaversion.com/view/ViaRewind/job/ViaRewind-DEV/`
* ViaRewind Legacy Support DEV: `https://ci.viaversion.com/view/ViaRewind/job/ViaRewind%20Legacy%20Support%20DEV/`

## Troubleshooting & FAQ

* Nothing updated — Make sure the target Via plugin is installed (the updater replaces what exists).
* Wrong channel — Check `snapshot/dev/java8` flags for the specific plugin.
* Downloaded but not applied — Enable `AutoRestart` or restart manually. If `plugins/update` exists, jars move on
  restart.
* Build not found — Jenkins may be down or the job moved. Try `/updatevias` again or verify the job URL.

## Building from Source

```bash
mvn -DskipTests package
```

Grab the shaded jar from `target/`.

## Changelog Highlights

* Folia‑safe scheduling and strict Bukkit access on Spigot.
* Clear snapshot selection rules (newest overall vs non‑snapshot only).
* Correct DEV job for ViaVersion and ViaRewind Legacy Support.
* Human‑friendly filename for ViaRewind‑Legacy‑Support.
* HTTP timeouts and improved error handling.
