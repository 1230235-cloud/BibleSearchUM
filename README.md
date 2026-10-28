# BibleSearchUM

Android application developed with **Jetpack Compose**, **Retrofit 2**, **Kotlin Coroutines**, **Preferences DataStore**, and **MVVM Architecture** (`ViewModel` + `StateFlow`) to search, read, save, and share verses from the **Bolls Bible REST API**.

## Features & Extra Challenges Implemented
- **REST API Integration**: Consumes `https://bolls.life/` endpoints using Retrofit 2 and Gson, including an OkHttp interceptor that transparently maps `RVR1960` (from the lab guide) to `RV1960` (Bolls Bible's live identifier).
- **MVVM Reactive State**: Immutable `BibleUiState` managed via `StateFlow` in `BibleViewModel`.
- **Cascading Material 3 Dropdowns**: Reusable `DropdownSelector` built on `ExposedDropdownMenuBox` for hierarchical Translation, Book, Chapter, and Verse selection.
- **Explicit State Handling**: Clean UI rendering for Loading (`CircularProgressIndicator`), Error (`errorContainer` Card), and Verse Display states.
- **Extra Challenge 1 (Share Verse)**: Shares the formatted verse reference and quote via implicit `Intent.ACTION_SEND` chooser.
- **Extra Challenge 2 (Local Persistence)**: Persists favorite verses across sessions using **Jetpack Preferences DataStore** (`FavoritesRepository`) and displays them in a dedicated **Favoritos** tab.
- **Extra Challenge 3 (Translation Selector)**: Dynamic switching between `RVR1960`, `NVI`, and `LBLA` translations.

## How to Test & Run
### 1. Run Unit & Live API Integration Tests (CLI)
```bash
./gradlew testDebugUnitTest
```
Runs `BibleSearchUnitTest`, which verifies:
1. Cascading selection (`Book -> Chapter -> Verse`), translation switching, and favorite toggling on `BibleViewModel`.
2. Live HTTP requests against `https://bolls.life/` fetching the 66 books and John 3:16 (`Juan 3:16`).

### 2. Build Debug APK (CLI)
```bash
./gradlew assembleDebug
```
Generates the installable APK at `app/build/outputs/apk/debug/app-debug.apk`.
