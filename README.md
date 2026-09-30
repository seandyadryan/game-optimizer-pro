# Game Optimizer PRO

Native Android gaming companion built with Kotlin and Jetpack Compose. 24 selectable interface languages, dark premium theme, lime accents, Android 8+ support.

**Application ID:** `com.deploydulupulangnanti.gameoptimizerpro`  
**Version:** 1.1.0 (2) · **Target SDK:** 36 · **Minimum SDK:** 26

## Screenshots

Actual Android 16 emulator captures; device readings are emulator values.

<img src="docs/screenshots/01-dashboard.png" width="240" alt="Device dashboard" /> <img src="docs/screenshots/02-library.png" width="240" alt="Game library" /> <img src="docs/screenshots/03-activity.png" width="240" alt="Session history" />

<img src="docs/screenshots/06-arabic-rtl.png" width="240" alt="Arabic right-to-left settings" /> <img src="docs/screenshots/07-japanese.png" width="240" alt="Japanese settings" /> <img src="docs/screenshots/05-installed-app-icons.png" width="240" alt="Original installed app icon in the picker" />

## Features

- Live device dashboard: available system RAM, battery level/temperature, free storage, connectivity, power saving and thermal warnings.
- Game library: Android-classified games plus user-selected launcher apps; search and launch.
- Original installed app icons in the library, picker, profiles, active sessions, and new history entries; safe fallback for removed apps and older history.
- Settings → Language: choose one of 24 languages or follow the device. Selection persists, Arabic/Persian/Urdu support RTL, and all translations are bundled for offline switching.
- Per-game Balanced, Competitive, and Endurance recommendation profiles.
- Persistent manual session timer and up to 100 completed sessions, with deletion controls.
- Shortcuts to Wi-Fi, Do Not Disturb, display, and battery settings.
- Offline-first: no account, ads, analytics, server, or Internet permission.

Android does not allow ordinary apps to force another game's frame rate, overclock hardware, or free other apps' RAM reliably. This app offers real device data and preparation tools, not synthetic boosts. Profiles are guidance and do not modify games. Battery temperature is not CPU/GPU temperature. Session time includes all elapsed time until manually ended.

## Build

Open this folder in Android Studio with Android SDK 36 installed. Use JDK 17 or 21. The committed Gradle wrapper downloads Gradle 8.13 with a SHA-256 check.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
.\gradlew.bat bundleRelease assembleRelease
```

Set your SDK path in ignored `local.properties`. Release signing uses environment variables or ignored `keystore.properties`:

```properties
KEYSTORE_PATH=D:/KEYSTORE/game_optimizer_pro.jks
KEYSTORE_PASSWORD=<private>
KEY_ALIAS=game_optimizer_pro
KEY_PASSWORD=<private>
```

Keep a secure offline backup of the key and credentials. They are not committed. Without signing inputs, local release output is unsigned; the GitHub release job explicitly requires all four secrets.

## GitHub Actions

The `Android build` workflow uses ephemeral GitHub-hosted `ubuntu-latest` runners. Pull requests run JVM tests, Android lint and debug APK builds. Pushes to `main`, `v*` tags, and manual dispatches additionally produce a signed APK and Play Store AAB after verification passes.

Android 26 and 36 emulator tests exercise navigation, privacy, persistence, session migration, installed icons, language switching and RTL, and capture actual UI screenshots. Release output requires verification and both emulator jobs to pass.

Repository Actions secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Download outputs from the workflow run's Artifacts section. The runner's temporary key is removed even if the build fails. No Play Console deployment occurs automatically.

## Localization

Included: English, Indonesian, Malay, Spanish, Brazilian Portuguese, French, German, Italian, Dutch, Polish, Russian, Ukrainian, Turkish, Arabic, Persian, Hindi, Bengali, Urdu, Simplified Chinese, Traditional Chinese, Japanese, Korean, Thai, and Vietnamese. Unsupported device languages fall back to English. Game names and logos are supplied by installed apps.

Edit the 104 translated messages per language in `localization/*.json`. Run `pwsh ./scripts/Generate-Locales.ps1` to update Android resources; `pwsh ./scripts/Generate-Locales.ps1 -Check` checks coverage, format placeholders, and generated files in CI. Add languages through `localization/languages.json` and a matching catalog. Have native speakers review store copy and translations for each launch market.

## Release preparation

See [Play Store checklist](docs/PLAY_STORE.md), [privacy policy in HTML](privacy-policy.html), [privacy policy source](docs/PRIVACY_POLICY.md), and [QA checklist](docs/QA.md). Source code is proprietary; no third-party redistribution license is granted. Dependency licenses remain applicable.
