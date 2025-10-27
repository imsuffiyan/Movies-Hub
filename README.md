# Movies-Hub

Movies-Hub is an Android client for browsing The Movie Database (TMDB) catalogue. It showcases now playing, popular, and top-rated movies, supports keyword search, and lets you maintain a list of favourites for quick access.

## Features
- Home screen with curated sections and horizontal carousels
- Infinite scroll list with Paging 3 for category detail screens
- Full-text search with loading state feedback
- Local favourites stored on device

## Prerequisites
- [Android Studio](https://developer.android.com/studio) or the Android command-line build tools
- A TMDB API key (create one at [TMDB](https://www.themoviedb.org/))

## Configuration
1. Copy your TMDB API key into either `local.properties` or an environment variable before building:
   - **Option A – local.properties**
     ```properties
     TMDB_API_KEY=your_real_api_key_here
     ```
   - **Option B – environment variable**
     ```bash
     export TMDB_API_KEY=your_real_api_key_here
     ```
2. Sync the Gradle project in Android Studio or run a build so the generated `BuildConfig` contains the key.

If the key is missing, the app surfaces a descriptive error when network calls are attempted.

## Build & Run
- From Android Studio: open the project and click **Run** ▶️.
- From the command line:
  ```bash
  ./gradlew assembleDebug
  ```
  Install the resulting APK on an emulator or device.

## Testing
Run unit tests with:
```bash
./gradlew test
```

## Tech Stack
- Kotlin & Jetpack
- Retrofit, OkHttp, and Gson
- Paging 3
- Hilt for dependency injection
- Glide for image loading

## Troubleshooting
- **Missing data on the home screen or search**: verify that the TMDB API key is configured as described above.
- **Empty favourites list after adding movies**: favourites refresh automatically when returning to the favourites screen; if issues persist, clear the app storage and try again.
