# NOISE Android App — Science & Engineering Fair 2025 Presentation

Welcome to the public reference repository for **NOISE (Neighborhood Oriented Intelligent School-bus Experience)**. This document is tailored for 2025 Science & Engineering Fair judges, educators, and technologists who want to understand the project at both a conceptual and technical level. The app is authored in Kotlin using Jetpack Compose to modernize daily school transportation with real-time visibility and streamlined communication.

---

## Executive Summary
- **Mission:** Deliver safer, more predictable school bus rides by sharing live bus locations, attendance confirmations, and rider manifests with families and drivers.
- **Audience:** Parents/guardians, students, and bus drivers in K-12 districts.
- **Innovation Highlight:** Integrates mapping, attendance workflows, and role-specific dashboards in a single Compose-based Android app.
- **Project Status (May 2025):** Prototype validated in controlled tests with mocked data sources; ready for integration with production-grade APIs.

## Problem Statement & Impact
School transportation coordinators frequently rely on manual phone calls or paper manifests. These methods introduce uncertainty when routes change or students miss the bus. NOISE reduces that uncertainty by:
1. Showing everyone the same live location map.
2. Allowing parents/students to confirm or decline ridership before pickup.
3. Giving drivers an automatically updated manifest to optimize their route.

Early testing suggests potential reductions in missed pickups and improved driver preparedness. Future pilots will measure these outcomes with live telemetry and attendance data.

## Demonstration Checklist for Judges
| Step | Action | Purpose |
| --- | --- | --- |
| 1 | Launch the app in Android Studio or on a device (see [Getting Started](#getting-started)) | Verify build reliability |
| 2 | Sign in as a parent/student via the main menu | Explore the rider-focused dashboard |
| 3 | Toggle attendance responses in the bottom sheet | Observe how confirmations update driver data |
| 4 | Switch to the driver experience | Inspect the manifest and navigation shortcuts |
| 5 | Review the mocked data adapters | Understand how real services would integrate |

## Technical Highlights
- **Jetpack Compose UI:** Declarative layouts provide responsive screen states for both riders and drivers without multiple activities.
- **Navigation Architecture Component:** `NavHost` coordinates all composable destinations, enabling clean separation of parent/student and driver flows.
- **Google Maps Compose:** Embeds map tiles and camera interactions inside Compose, simplifying live-location rendering.
- **Material 3 Design System:** Bottom sheets, buttons, and typography follow modern Android guidelines for accessibility and consistency.
- **Mock Data Layer:** Kotlin data classes such as `Student` simulate backend responses, making it easy to swap in real network or database sources later.

## System Architecture (High Level)
```
MainActivity
└── AppNavigation (NavHost)
    ├── MainMenuScreen
    ├── UserMainScreen (parent/student view)
    │   ├── MapsCompose surface
    │   └── AttendanceBottomSheet
    └── DriverScreen (driver view)
        ├── MapsCompose surface
        └── ManifestBottomSheet
```
Each composable is self-contained, receives state via parameters, and emits user intents (e.g., attendance confirmations) back to view-model-style controllers. In this prototype those controllers are simple functions; production integration can introduce `ViewModel` classes and repository layers.

## Code Walkthrough
- `app/src/main/java/com/example/noise/MainActivity.kt` — Single-activity entry point that configures the Compose content tree.
- `app/src/main/java/com/example/noise/AppNavigation.kt` — Central navigation graph defining available routes and their transitions.
- `app/src/main/java/com/example/noise/MainMenu.kt` — Landing screen where users choose the parent/student or driver experience.
- `app/src/main/java/com/example/noise/UserMainScreen.kt` — Displays the map, bottom sheet, and attendance workflow for families.
- `app/src/main/java/com/example/noise/DriverScreen.kt` — Provides drivers with a manifest, route summary, and quick navigation options.
- `app/src/main/java/com/example/noise/Student.kt` — Data model representing riders and their attendance status.

## Requirements (2025 Toolchain)
- Android Studio Ladybug (2024.2.1) or newer with the corresponding Android Gradle Plugin.
- Android SDK 36 with build tools 36.x.
- Kotlin 1.9 or newer.
- A Google Maps API key (see below) for production map tiles.

## Getting Started
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd NOISE
   ```
2. **Open in Android Studio**
   - Select *Open an Existing Project* and choose this folder.
   - Allow Gradle synchronization to finish.
3. **Configure the Google Maps API key**
   - Follow the [Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk) guide to create a key.
   - Add the key to `local.properties` (not committed to source control):
     ```
     MAPS_API_KEY=your_api_key_here
     ```
   - Reference the key in `AndroidManifest.xml` (inside the `app` module) or a secure configuration provider before releasing.
4. **Run the application**
   - Choose an emulator or physical device running Android 7.0 (API 24) or newer.
   - Click *Run* in Android Studio or execute `./gradlew installDebug` from the command line.

## Testing Strategy
Use Gradle tasks to validate the codebase:
```bash
./gradlew test                 # JVM unit tests
./gradlew connectedAndroidTest # Instrumented UI tests (requires emulator or device)
```
For science fair demonstrations, we recommend running the unit test suite before showcasing the app to confirm build stability.

## Evaluation Rubric Notes
Judges may consider these aspects when scoring the project:
- **Innovation:** Fusion of attendance workflows with live transit mapping in a student-centric context.
- **Technical Complexity:** Compose-based UI, state management patterns, and Google Maps integration.
- **Scalability Plan:** Modular architecture supports future authentication, push notifications, and analytics.
- **Societal Impact:** Improves safety and communication for families and transportation staff.

## Roadmap Beyond the Prototype
1. Replace mocked data with secure REST or GraphQL services.
2. Add driver-initiated notifications for delays or route changes.
3. Implement student RFID or QR check-ins for precise boarding records.
4. Introduce admin dashboards for district-level analytics.
5. Conduct pilot deployments with real buses and gather feedback for iteration.

## Ethical & Privacy Considerations
- Handle location data according to district privacy policies and COPPA/FERPA guidelines.
- Offer transparent opt-in controls for guardians.
- Encrypt network traffic and enforce secure key management when integrating real services.

## References & Credits
- Google Maps Platform: Maps Compose & Maps SDK for Android.
- Android Jetpack libraries: Navigation, Lifecycle, Material 3 components.
- Prototype contributors: NOISE core development team, 2025 Science & Engineering Fair mentors.

## Licensing & Usage
See the [`COPYRIGHT`](COPYRIGHT) file for ownership details. Redistribution or modification requires prior written consent from NOISE, while third-party dependencies retain their respective licenses.