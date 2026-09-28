# QA

## Automated

`./gradlew testDebugUnitTest lintDebug assembleDebug bundleRelease assembleRelease`

JVM tests cover readiness priority, thermal reporting without a battery sensor, battery boundary, power saving, unavailable metrics, and session duration behavior. Android lint checks platform compatibility and resources. GitHub validates release signatures.

## Manual device acceptance

- Fresh install: introductory disclosure appears; acceptance persists after restart.
- Dashboard: real values refresh while visible; polling stops in background. Missing sensors are shown as unavailable. Verify high temperature/low battery warnings against actual device states.
- Library: auto-detected games, manual selection/removal, search, no apps, and uninstalled games.
- Profile: save each profile, restart and confirm persistence; recommendations do not claim to apply game settings.
- Sessions: launch an app, return, finish; confirm manual history. Prevent another session while one is active. Check process death and reboot with active timer.
- Settings: manufacturer-specific pages either open or show an explanatory error. Delete history confirmation and cancellation work.
- Accessibility: TalkBack, 200% font scale, landscape, tablet layout, gesture navigation and three-button navigation.
- Release: install signed APK; upload AAB to Play internal testing and review pre-launch report.

Physical-device and Play Console acceptance must be performed before commercial publication. No physical device was attached during initial setup.
