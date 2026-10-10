# Tactile Deck

A refined industrial cyberdeck control surface for Android, built with Kotlin and Jetpack Compose.

## Build

Requires JDK 17 and Android SDK 35. Run `./gradlew assembleDebug`. The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions builds the debug APK on pushes and pull requests and uploads it as an artifact. On version tags (`v*`), it also publishes a GitHub Release with the APK attached.
