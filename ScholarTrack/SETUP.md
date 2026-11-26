# ScholarTrack setup (Android Studio, Kotlin + Jetpack Compose)

1. New Project > "Empty Activity" (Compose). Name: ScholarTrack, package: com.scholartrack.app, Min SDK 26.
2. Replace/merge `app/build.gradle.kts` pieces with `build.gradle.kts.snippet.txt`.
   The KSP plugin version must match your Kotlin version (e.g. Kotlin 2.0.21 -> ksp 2.0.21-1.0.28).
3. Merge `AndroidManifest.snippet.xml` into your manifest (permissions + android:name=".ScholarTrackApp").
4. Copy the `com/scholartrack/app/...` folders into app/src/main/java/ (delete the template MainActivity).
5. Create a GitHub repo, upload `scholarships.json` to it, then set BASE_URL in data/AppContainer.kt:
   https://raw.githubusercontent.com/<user>/<repo>/main/
   (must end with "/"). Until you do, the app shows the "could not refresh" error state, which is also good for testing.
6. Sync Gradle and run.
