# AppPerms

AppPerms is a Jetpack Compose Android application that audits installed applications, reports requested and granted permissions, and ranks apps using a configurable risk score.

> **Status:** Active development. Permission changes are routed through Android system settings. A `PermissionModifierEngine` abstraction is included for future Shizuku/AppOps integrations.

## Features

- Enumerates installed user and system applications, including Google services and packages without requested permissions.
- Displays requested, granted, denied, and risk-weighted permission information.
- Searches by application name or package name.
- Filters by user/system/all applications and dangerous permissions.
- Sorts by risk score, granted permission count, or name.
- App detail sheet with direct access to Android application settings.
- Long-press multi-selection and batch permission review.
- DataStore-backed preferences for theme, startup filters, sort order, and risk weights.
- Material 3 themes with Light, Dark, System, and Android 12+ Dynamic Monet modes.
- About screen with build metadata and project link.

## Architecture

The project follows a pragmatic clean-architecture/MVVM structure:

```text
app/src/main/java/com/example/appperms/
├── data/       # Package Manager data source and DataStore preferences
├── domain/     # App models, risk calculator, modifier abstraction
└── ui/         # Compose screens, ViewModels, navigation, and theme
```

Package Manager calls are dispatched to `Dispatchers.IO`. ViewModels expose UI state through Kotlin `StateFlow`, and Hilt provides application dependencies.

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 37
- Android device or emulator running API 24+

`QUERY_ALL_PACKAGES` is declared because the core product requires visibility into installed applications on Android 11 and newer. Distribution through Google Play may require a permitted core use case and a Play Console declaration.

## Build

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Run JVM tests with:

```bash
./gradlew :app:testDebugUnitTest
```

## Permission behavior

Android intentionally restricts third-party applications from silently changing another app's runtime permissions. AppPerms therefore opens the target app's system management page. The `PermissionModifierEngine` interface is deliberately isolated for a future optional Shizuku/AppOps implementation.

The installed-app list represents packages visible to the app. It does not guarantee that every listed package is actively running in the background; Android restricts broad process inspection for ordinary applications.

## Privacy

AppPerms performs local device inspection. It does not require an account and does not include a network service for uploading the installed-app inventory.

## License

AppPerms is distributed under the Apache License 2.0. See [LICENSE](LICENSE).
