# Dash — Local Android Telemetry & Monitor

A 100% local, privacy-first Android monitoring app built with **Kotlin + Jetpack Compose + Room**, featuring a pure **Black & White (`limora-unslop` monochrome)** editorial UI.

## Features
- **Screen Time & App Usage**: Archives daily foreground time, app launch counts, and screen unlocks via `UsageStatsManager` into a local Room SQLite database so **monthly and yearly** history is preserved even after Android's 7-day buffer expires.
- **Data Usage (Wi-Fi & Mobile)**: Tracks per-app and total Wi-Fi vs Mobile data consumption via `NetworkStatsManager`, stored daily for monthly & yearly reports.
- **Location History**: Logs GPS/Network coordinates and reverse-geocoded addresses locally using `LocationManager` + a lightweight Foreground Service (`MonitorForegroundService`) and `BootReceiver`, plotted on a monochrome coordinate grid.
- **Device Telemetry & Local Vault**: Records battery level, charging state, temperature, RAM, and internal storage snapshots, with one-tap **JSON local backup export** to `Downloads/Dash/`.
- **Monochrome UI (`limora-unslop` style)**: High-contrast Black (`INK`) and White (`PAPER`) themes, bespoke Compose `Canvas` illustrations (telemetry dial, coordinate trace, split data meter, bar charts, drawn empty states), hairline borders, and tabular numerals.

## Build APK via GitHub Actions
1. Push this repository to GitHub (`main` or `master` branch).
2. Open the **Actions** tab in your GitHub repository.
3. The **Build Android APK** workflow (`.github/workflows/build-apk.yml`) runs automatically (or can be triggered manually via **Run workflow**).
4. Once complete, download `dash-debug-apk` from the workflow **Artifacts** section and install `app-debug.apk` on your Android phone.
