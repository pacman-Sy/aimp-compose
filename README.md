# AIMP Compose — AIMP-style player with LRCLib lyrics

Kotlin + Jetpack Compose + Media3 + Hilt + Room + DataStore + Retrofit/kotlinx.serialization.
Clean Architecture + MVI, Material 3 with AIMP dark-gray/orange palette, light theme toggle.

## Architecture (text diagram)

```
PlayerScreen / LibraryScreen / SettingsScreen / EqualizerScreen  (Compose)
        |  PlayerIntent (MVI)                         ^
        v                                             | StateFlow<PlayerUiState>
PlayerViewModel  ---- LyricsRepository ---- LrcLibApi (Retrofit, custom User-Agent)
        |                    |
        |                    +-- Room (lyrics_cache) + DataStore (prefs)
        v
MediaController --> MediaSessionService --> ExoPlayer --> AudioFX Equalizer
                              |
                              +-- Media3 Notification + Android Auto
```

## LRCLib
- `GET https://lrclib.net/api/get` with `track_name`, `artist_name`, `album_name`, `duration`.
- Custom `User-Agent: LRCGET v0.2.0 (...)` header on every request.
- Synced LRC parsed to timed lines; cached in Room by track ID.
- Instrumental -> "No lyrics available (Instrumental)". 404 -> "Lyrics not found" + retry.
- One fetch per track session unless user refreshes.

## CI
`.github/workflows/build.yml` builds on push / dispatch and uploads the APK to gofile.
