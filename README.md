# ECR17 POS Client (minimal version)

Native Android app that talks to a PAX POS terminal over raw TCP/IP using the ECR17 /
Protocol 17 standard (see `docs/PAX_ECR17_Protocol_Specification.pdf`). The phone runs as
the **ECR** (cash register) and connects as a **TCP client** to the POS terminal on the
same Wi-Fi/LAN.

## Current scope

This is the minimum working version:

- One screen (**Buy**): enter the POS's IP/port and the terminal's Terminal ID / Cash
  Register ID, enter an amount, tap **BUY**.
- The app checks POS status, then sends a Basic Payment (command `P`) and shows the
  parsed result (approved with masked card/auth code/STAN, or declined with the reason).
- The four connection fields are **remembered** across app restarts (saved via
  Preferences DataStore) — you only type them once.
- **Not included yet**: transaction history, the other 18 protocol operations (refund,
  reversal, totals, pre-authorization, etc.), and a Configuration screen. The full
  protocol engine for all of that already exists and is tested in `:protocol-core` — see
  `docs/ecr17-coverage-matrix.md` — it's just not wired into the UI yet.

## Project layout

- `:protocol-core` — pure Kotlin/JVM module: LRC, frame codec, TCP stream parser, all 19
  ECR17 message builders/parsers, the transport layer, and the ACK/NAK/retry transmission
  session. Fully unit-tested (137 tests), no Android dependency.
- `:app` — the Android application (Compose UI, currently just the Buy screen).
- `:pos-simulator` — scaffolded module, not yet implemented.

## Building the APK

**This repository was built in a sandbox with no Android SDK and no network access to
Google's Maven repository, so the `:app` module has not been compiled or run here.** The
Kotlin/Compose/Room/DataStore code follows standard, current APIs, but you should treat it
as "ready to build," not "verified to build," until you do the following yourself:

1. Install [Android Studio](https://developer.android.com/studio) (any recent stable
   version — this project targets `compileSdk 35`, `minSdk 26`, Kotlin 2.0.21, AGP 8.6.0).
2. Open the repository root (`Protocolo-17/`) in Android Studio. It will detect the
   Gradle project (`settings.gradle.kts` includes `:app`, `:protocol-core`,
   `:pos-simulator`) and sync automatically — this requires normal internet access
   (Google's Maven + Maven Central), unlike the sandbox this was built in.
3. Once sync finishes, either:
   - Click **Run ▶** with a device/emulator selected, or
   - Build → Build Bundle(s)/APK(s) → Build APK(s) to produce
     `app/build/outputs/apk/debug/app-debug.apk`.
4. To install on a physical phone: enable Developer Options → USB debugging, connect via
   USB, and either use Android Studio's Run button or:
   ```
   ./gradlew installDebug
   ```

### Command-line build (if you have the Android SDK installed locally)

```
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Running just the protocol tests (no Android SDK required)

The protocol engine can be built and tested independently of Android tooling:

```
./gradlew -c settings-core.gradle.kts :protocol-core:test
```

(`settings-core.gradle.kts` is a sandbox-only settings file that excludes `:app` so this
works even without the Android SDK. Opening the project normally in Android Studio uses
`settings.gradle.kts`, which includes everything.)

## Configuring the app against a real terminal

Before this app can talk to your PAX terminal, confirm on the terminal / PAXTools side:

1. **PAXTools is configured for ECR communication over TCP/IP** (not RS232/USB) — the
   protocol spec doesn't define a universal TCP port, so you must read the actual port
   PAXTools is listening on from its own configuration screen.
2. **The terminal's IP address** on your Wi-Fi/LAN.
3. **Terminal ID** and **Cash Register ID** as configured in PAXTools — both are 8-digit
   numeric strings (leading zeros matter, e.g. `00000001`).
4. **The phone and the terminal are on the same network**, and that network doesn't have
   client isolation enabled (a common Wi-Fi router setting that blocks device-to-device
   traffic and would silently break this even though Wi-Fi itself is connected).

Enter the IP, port, Terminal ID, and Cash Register ID once in the app — they're saved
automatically. Enter an amount and tap BUY to test.

## Known limitations (see `docs/` for detail)

- `docs/lrc-implementation-note.md`: the LRC checksum algorithm is implemented per the
  most plausible reading of the spec, but has **not** been certified against an official
  Fabrick/PAX test vector or real terminal traffic. Validate this before any production use.
- `docs/protocol-assumptions.md`: every place the PDF was internally ambiguous or
  inconsistent, and the interpretation implemented.
- `docs/production-checklist.md`: not yet written (deferred along with the rest of the
  full-scope build).
