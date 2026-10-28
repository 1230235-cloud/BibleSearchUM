# BibleSearchUM

Android application developed with **Jetpack Compose**, **Retrofit 2**, **Kotlin Coroutines**, and **MVVM Architecture** (`ViewModel` + `StateFlow`) to search and read verses from the **Bolls Bible REST API (RVR1960)**.

## Features
- **REST API Integration**: Consumes `https://bolls.life/` endpoints (`get-books/RVR1960/` and `get-verse/RVR1960/{bookId}/{chapter}/{verse}/`) using Retrofit 2 and Gson.
- **MVVM Reactive State**: Immutable `BibleUiState` managed via `StateFlow` in `BibleViewModel`.
- **Cascading Material 3 Dropdowns**: Reusable `DropdownSelector` built on `ExposedDropdownMenuBox` for hierarchical Book, Chapter, and Verse selection.
- **Explicit State Handling**: Clean UI rendering for Loading (`CircularProgressIndicator`), Error (`errorContainer` Card), and Verse Display states.

## Project Structure
- `model/BibleModels.kt`: `BibleBook` and `BibleVerse` data classes.
- `network/BibleApiService.kt`: Retrofit service interface and `RetrofitInstance` singleton.
- `ui/BibleUiState.kt`: Immutable UI state container.
- `ui/BibleViewModel.kt`: Business logic and coroutine-based network requests.
- `ui/components/DropdownSelector.kt`: Generic Material 3 `ExposedDropdownMenuBox` component.
- `ui/BibleSearchScreen.kt`: Main composable screen.
- `MainActivity.kt`: Application entry point.
