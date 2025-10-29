# NOISE Android App

NOISE is a Jetpack Compose Android application that helps school bus drivers, parents, and students stay synchronized on daily pickup routes. It combines real-time mapping with streamlined communication flows so that everyone can see where the bus is, confirm attendance, and keep track of upcoming stops.

## Key Features

- **Role-based navigation:** A main menu directs users to tailored experiences for parents/students and drivers.
- **Interactive Google Maps views:** Both parents/students and drivers see live maps powered by the [Maps Compose](https://github.com/googlemaps/android-maps-compose) library.
- **Bottom sheet dashboards:** Contextual information such as attendance confirmations and student manifests is presented in Material 3 bottom sheets.
- **Attendance workflow:** Parents and students can confirm whether they plan to ride, making it easier for drivers to plan their route.
- **Driver manifest:** Drivers get a consolidated list of riders with quick navigation shortcuts.

## Architecture Overview

The project is written in Kotlin and uses a single-activity architecture that hosts multiple composable screens. Navigation between screens is handled by `NavHost` from `androidx.navigation.compose`.

```
MainActivity
└── AppNavigation (NavHost)
    ├── MainMenuScreen
    ├── UserMainScreen (parent/student view)
    └── DriverScreen (driver view)
```

Each screen uses composable functions to declare UI elements. Shared data such as the `Student` model is stored in simple Kotlin data classes. Bottom sheet interactions rely on `BottomSheetScaffold` to present dynamic content without leaving the map view.

> **Note:** Location data and attendance states are mocked. Integrate your own data sources (for example, a backend service or real-time location provider) before releasing to production.

## Requirements

- Android Studio Hedgehog (or newer) with the latest Android Gradle Plugin
- Android SDK 36 with build tools 36.x
- Kotlin 1.9+
- A Google Maps API key configured in your `local.properties` or manifest for map rendering

## Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd NOISE
   ```
2. **Open in Android Studio**
   - Select *Open an Existing Project* and choose this folder.
   - Let Gradle synchronize dependencies.
3. **Configure the Google Maps API key**
   - Follow the [Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk) documentation to generate an API key.
   - Add your key to the `local.properties` file:
     ```
     MAPS_API_KEY=your_api_key_here
     ```
   - Reference the key from `AndroidManifest.xml` or a secure configuration file before building.
4. **Run the application**
   - Choose an emulator or physical device running Android 7.0 (API 24) or newer.
   - Click the *Run* button in Android Studio or execute `./gradlew installDebug` from the command line.

## Testing

Run unit tests and instrumented tests with Gradle:

```bash
./gradlew test          # JVM unit tests
./gradlew connectedAndroidTest  # Instrumented UI tests (requires emulator or device)
```

## Project Structure

```
app/
├── build.gradle.kts         # Module Gradle configuration
├── src/main/java/com/example/noise/
│   ├── MainActivity.kt      # Entry point hosting the NavHost
│   ├── AppNavigation.kt     # Navigation graph definitions
│   ├── MainMenu.kt          # Role selection screen
│   ├── UserMainScreen.kt    # Parent/student map + attendance UI
│   ├── DriverScreen.kt      # Driver map + rider manifest
│   └── Student.kt           # Data model for riders
└── src/main/res/            # UI resources
```

## Contributing

1. Fork the repository and create a feature branch.
2. Make your changes and ensure all tests pass.
3. Submit a pull request with a clear description of the updates.

## License

See the [`COPYRIGHT`](COPYRIGHT) file for ownership details. If you plan to publish this app, ensure compliance with third-party licenses (Google Maps SDK, Android Jetpack libraries, etc.).
