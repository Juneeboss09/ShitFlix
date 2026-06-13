# ShitFlix (Android)

A Netflix-style streaming client for Android, written in Kotlin + Jetpack Compose. It uses a CloudStream-inspired provider/extension model so streaming providers can be plugged in at runtime.

> ⚠️ This project is a scaffold. It compiles and runs, ships a sample "Demo" provider (public-domain trailers / open content from Internet Archive), and exposes the extension/provider interfaces so real CloudStream-compatible providers can be added. **Do not bundle pirated providers** — the user is responsible for what they install.

## Features

- 🎬 Home: trending / categories / featured hero (Netflix-style)
- 🔍 Search across enabled providers
- 📄 Movie details (synopsis, poster, play button, episodes if series)
- 🧩 Extension installer + provider selector
- ▶️ ExoPlayer (Media3) player with subtitle tracks + quality (track) selector
- 🌑 Dark Netflix-clone theme, Compose Material3

## Build

```bash
cd android
# First time only — generate the Gradle wrapper:
gradle wrapper --gradle-version 8.7
# Then:
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and Android SDK with `compileSdk = 34`.

If you don't have a system `gradle`, open the `android/` folder in Android Studio (Hedgehog or newer) and let it generate the wrapper, then `Build → Build APK(s)`.

## Project structure

```
app/src/main/java/com/shitflix/app/
  MainActivity.kt              # Single-activity, Compose NavHost
  ui/theme/                    # Netflix-clone dark theme
  ui/screens/                  # Home, Details, Search, Extensions, Player
  ui/components/               # MovieCard, Row, Hero
  data/model/                  # Movie, Episode, StreamLink, Subtitle
  data/provider/               # ProviderApi interface + DemoProvider
  data/repo/                   # ProviderRegistry (in-memory store)
  extensions/                  # ExtensionInstaller (URL-based, stubbed)
  player/                      # ExoPlayer wrapper
```

## Adding providers

`ProviderApi` is the contract every provider implements:

```kotlin
interface ProviderApi {
  val name: String
  suspend fun home(): List<HomeRow>
  suspend fun search(query: String): List<Movie>
  suspend fun details(id: String): MovieDetails
  suspend fun load(id: String): List<StreamLink>
}
```

Drop a new implementation into `data/provider/` and register it in `ProviderRegistry`. A real CloudStream extension loader (DEX-based, hot-loadable) is a much bigger project — the included `ExtensionInstaller` is a stub showing where it would plug in.
