# PACE Running for Android

This Android Studio project wraps the current PACE web app in a native Android WebView. The app loads `https://vz7m60.github.io/pace-running/`, so an internet connection is required. Run history, goal settings, photos, and manually entered Samsung Health values stay in the Android app's WebView storage and are separate from Chrome.

For APK download, Galaxy installation steps, and the security warning explanation, see the [Korean Android installation guide](INSTALL_KO.md).

## Android integrations

- Requests Android location permission for browser GPS tracking.
- Offers the camera and image picker for run photos.
- Uses the Android share sheet for run summaries.
- Keeps the screen awake while the run is active.
- Provides Android back handling for map full screen and the ready screen.

## Build

Open `android-app` in Android Studio with JDK 17 and Android SDK 35 installed, then build the `app` debug variant. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow also builds the debug APK on pushes that change this project. Download the `PACE-Android-debug` artifact from the workflow run. The local workspace did not have a working JDK or Android SDK when this project was created, so local APK compilation could not be run here.

The debug APK is for testing and cannot update a release-signed installation. For direct distribution, create the signing key and repository secrets described in the [Korean installation and release guide](INSTALL_KO.md), then run **Android Release APK** from GitHub Actions. It creates a signed APK and a draft GitHub Release for review before publication.
