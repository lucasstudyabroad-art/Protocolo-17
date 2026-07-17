# Implementation Plan

## Environment constraint (read first)

This build environment has **no Android SDK** installed (no `ANDROID_HOME`, no `compileSdk` platform jars, no emulator) and no network path to install one within this session. Consequences:

- The `:app` Android module (Compose UI, Room, DataStore, AndroidManifest) is written to compile under a normal Android Studio / AGP setup, but **could not be built or unit-tested in this sandbox**.
- To still satisfy "run tests after every major protocol module" and "do not mark an operation implemented until its builder, parser and tests exist," the entire protocol codec/message layer and the transport layer are implemented in a **plain Kotlin/JVM Gradle module** (`:protocol-core`) that has zero Android dependencies (uses only `java.net.Socket`, `kotlinx.coroutines`, `kotlin.text`). This module **is** compiled and its JUnit/MockK/Turbine tests **are** run in this sandbox with the system-installed Gradle/JDK.
- `:pos-simulator` is a second plain Kotlin/JVM module (a CLI TCP server) that also builds and runs in this sandbox.
- `:app` depends on `:protocol-core` as a regular Gradle module dependency, so none of the protocol logic is duplicated or reimplemented inside the Android module.

This is a deliberate deviation from the "suggested packages" section of the task prompt, which shows everything under `app/src/main/java/.../protocol/`. Functionally the package names and structure are preserved (`protocol.core`, `protocol.frame`, `protocol.parser`, `protocol.transport`, `protocol.service`, `protocol.messages.*`); only the Gradle module boundary changed, specifically so the protocol layer is independently testable without an Android toolchain. This is documented here and in the coverage matrix.

## Phases

1. **Docs** — inspect repo, read full PDF, write `docs/ecr17-coverage-matrix.md` (this doc's sibling) and this plan. ✅
2. **Gradle scaffold** — root settings, `:protocol-core`, `:pos-simulator`, `:app` module skeletons, version catalog, lint/compiler strictness. ✅
3. **Protocol core, batch 1** — `Ecr17Constants`, field utilities (`Ecr17Field.kt`), `MoneyParser`, `Ecr17LrcCalculator`, `Ecr17FrameCodec`, `Ecr17Packet`, `Ecr17StreamParser`, `Ecr17Error`, `Ecr17OperationResult`. Unit tests for all. ✅
4. **Protocol core, batch 2 — messages** — one builder + one parser + one unit test file per operation in the coverage matrix (19 operations). ✅
5. **Protocol core, batch 3 — transport & session** — `PosSocketTransport` interface, `TcpClientTransport` (raw `java.net.Socket`), `Ecr17TransmissionSession` (ACK/NAK/retry/timeout state machine), `Ecr17ResponseValidator` (Terminal ID check), `Ecr17Service` facade, `OperationStateMachine`. Unit + fake-transport integration tests. ✅
6. **pos-simulator** — standalone TCP server CLI exercising the configurable scenarios listed in the prompt, built on `:protocol-core` codecs. ✅
7. **Android app — data layer** — Room entities/DAOs (`AppConfigEntity`/DataStore, `OperationEntity`, `ProtocolEventEntity`), Preferences DataStore config repository, mappers. ✅ (written, not compiled — see constraint above)
8. **Android app — domain/service glue** — `Ecr17OperationCoordinator` wiring `:protocol-core` `Ecr17Service` to Room persistence, PAN masking, duplicate-prevention `Mutex`, uncertain-state handling. ✅ (written, not compiled)
9. **Android app — UI** — navigation shell (Buy/History/Configuration bottom bar), Buy screen + Other Operations bottom sheets for all 19 operations, History list/detail/export, Configuration screens. ✅ (written, not compiled)
10. **README, production checklist, LRC note** — ✅

## Test execution log

Run from repo root with the system Gradle (`/opt/gradle/bin/gradle`, wraps to project's Gradle 8.x + Kotlin 2.0):

```
gradle :protocol-core:test
gradle :pos-simulator:build
```

Results and any fixes applied are recorded at the bottom of this file after each phase, per the "compile the affected module; run relevant tests; fix failures; update the coverage matrix; report" instruction.

<!-- Phase results appended below as work proceeds -->
