# MVPoisk TV Android project

A minimal Android TV WebView shell for the MVPoisk TV web mode.

Production URL opened by the app:
`https://mvcomplexsite.github.io/mvpoisk/?tv=1`

## Build
1. Open this folder in Android Studio.
2. Let Gradle sync and install Android SDK 35 if prompted.
3. Build > Build APK(s).
4. Install `app-debug.apk` on Android TV with ADB or a file manager.

The app declares `LEANBACK_LAUNCHER`, requires Android TV/Google TV, does not require a touchscreen, supports D-pad navigation through the website TV mode, and supports WebView fullscreen custom views for embedded video players.

## Build without Android Studio (GitHub Actions)
Push this project to a GitHub repository. The included workflow `.github/workflows/build-apk.yml` builds a debug APK on every push to `main` and also supports manual `workflow_dispatch`. Download the `mvpoisk-tv-debug-apk` artifact from the Actions run.
