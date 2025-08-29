AutoViaUpdater
==============

Keep your Via stack fresh across Spigot, BungeeCord, and Velocity — automatically.

Links

- Spigot: https://www.spigotmc.org/resources/autoviaupdater.109331/

Highlights

- Multi‑platform: Spigot, BungeeCord, Velocity.
- Folia‑ready: safe scheduling, no legacy Bukkit thread violations.
- Smart updates: picks Release or Snapshot builds; Java‑8 jobs supported.
- Flexible scheduling: simple minute interval or UNIX cron expressions.
- Safe restarts: optional broadcast + delayed restart on update.
- bStats: lightweight, async metrics (opt‑out supported).

What gets updated

- ViaVersion
- ViaBackwards
- ViaRewind
- ViaRewind Legacy Support (Spigot only)

Compatibility

- Spigot/Paper/Folia: 1.8+ (api‑version 1.13 for modern servers; still loads on legacy).
- BungeeCord and Velocity latest stable APIs.

Install

- Drop the built jar in your `plugins` folder (on proxy, drop on the proxy’s `plugins`).
- Start the server once to generate config and `versions.yml`.
- Adjust settings, restart.

Config Overview

- Spigot/Bungee: `plugins/AutoViaUpdater/config.yml`
- Velocity: `plugins/AutoViaUpdater/config.toml`

Global keys

- `Check-Interval`: minutes between checks when cron is disabled.
- `Cron-Expression`: UNIX cron string; set to "" to disable.
- `Delay`: seconds to wait after load before first check.
- `AutoRestart`: whether to restart after a successful update.
- `AutoRestart-Delay`: seconds before restart.
- `AutoRestart-Message`: broadcast message (supports `&` color codes on all platforms).

Per‑plugin keys

- `ViaVersion.enabled`, `ViaVersion.snapshot`, `ViaVersion.dev`, `ViaVersion.java8`
- `ViaBackwards.enabled`, `ViaBackwards.snapshot`, `ViaBackwards.dev`, `ViaBackwards.java8`
- `ViaRewind.enabled`, `ViaRewind.snapshot`, `ViaRewind.dev`, `ViaRewind.java8`
- `ViaRewind-Legacy.enabled`, `ViaRewind-Legacy.snapshot`, `ViaRewind-Legacy.dev` (Spigot only)

Commands & Permission

- `/updatevias` — triggers an immediate check.
- Permission: `autoviaupdater.admin` (required on Bungee/Velocity; OP on Spigot by default).

How it works

- Checks Jenkins for the latest matching build (Release vs Snapshot, Java‑8 when selected).
- Downloads `.jar` into the `plugins` folder. If a jar already exists, the new jar is placed in `plugins/update` to be
  picked up on next restart.
- Tracks downloaded build numbers in `plugins/AutoViaUpdater/versions.yml` (separate keys for Release, Dev, Java8
  variants).

Folia Support

- Periodic checks run off the Bukkit scheduler to avoid region‑thread issues.
- All Bukkit API touches (broadcast, shutdown, plugin manager reads) happen on the global region via a small adapter.

Build From Source

- Requires Maven.
- Run `mvn package` — the shaded jar can be found in `target/`.

Changelog (10.0.0)

- Folia‑compatible scheduling and safe Bukkit access.
- Unified async command behavior across all platforms.
- Color code support for broadcast messages on all platforms.
- Added HTTP timeouts and tightened error handling.
- Cron tick logic fixed to avoid off‑by‑one misses.
- Version bump and general polish for v10.

CI

- Every push builds the project and uploads the shaded JAR as a workflow artifact.
- Pushing a tag like `v10.0.0` creates a GitHub Release with the JAR attached.
