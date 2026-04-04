# TMDb API Key Setup

The app can use a TMDb API key in either of these ways:

1. Put the key in `local.properties`
2. Set the `TMDB_API_KEY` environment variable

## Option 1: local.properties

Open `local.properties` and add:

```properties
tmdbApiKey=YOUR_TMDB_API_KEY
```

## Option 2: environment variable

In PowerShell:

```powershell
$env:TMDB_API_KEY="YOUR_TMDB_API_KEY"
```

Then run the app from the project root:

```powershell
cd C:\Users\USER\Documents\codex\moviedb-app
$env:GRADLE_USER_HOME='C:\Users\USER\Documents\codex\moviedb-app\.gradle-home'
.\gradlew.bat assembleDebug
```

If the key is not set, the app falls back to sample movie data.
