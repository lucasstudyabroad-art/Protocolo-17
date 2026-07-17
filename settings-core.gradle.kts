// Sandbox-only settings file: builds just the pure-Kotlin/JVM modules that don't require
// the Android SDK/AGP (which cannot be resolved in network-restricted CI/dev sandboxes,
// since the AGP plugin is only published on Google's Maven repo).
//
// Usage: gradle -c settings-core.gradle.kts :protocol-core:test
//
// Android Studio / a full dev machine should open the project normally (settings.gradle.kts),
// which includes :app as well.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Protocolo-17"

include(":protocol-core")
include(":pos-simulator")
