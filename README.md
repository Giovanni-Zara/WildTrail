# WildTrail — A Strava-Style Hiking App

Kotlin · Jetpack Compose · Firebase Auth + Firestore · Room (offline-first) · MVVM · Navigation Compose · Material 3

This repository is the source code for **WildTrail**, an Android hiking app. It demonstrates the **Modern Android Development (MAD)** stack end-to-end: a single-Activity Compose UI, MVVM with `StateFlow`, repository-pattern data layer with offline-first caching to Room, and a remote Firestore + Firebase Auth backend wired through a manual dependency-injection container.

---

## Table of contents

1. [What's included (feature checklist)](#1-whats-included)
2. [Prerequisites](#2-prerequisites)
3. [Importing the project into Android Studio](#3-importing-the-project)
4. [One-time setup: Firebase, Maps API key, Java](#4-one-time-setup)
5. [Project architecture](#5-project-architecture)
6. [Package structure (where every file lives)](#6-package-structure)
7. [File-by-file reference](#7-file-by-file-reference)
8. [Running the app](#8-running-the-app)
9. [Tests — what they cover and how to run them](#9-tests)
10. [Troubleshooting & FAQ](#10-troubleshooting)
11. [What's intentionally left out (extension ideas)](#11-extension-ideas)

---

## 1. What's included

| Area | Status | Notes |
|---|---|---|
| Auth (email + password) | ✅ | Firebase Auth via `FirebaseAuthService` |
| Local cache (offline-first) | ✅ | Room database `WildTrailDatabase`, 9 entities, 8 DAOs |
| Cloud sync | ✅ | Firestore via `FirestoreService` with snapshot listeners |
| MVVM + StateFlow | ✅ | One `*UiState` data class per screen |
| Stateless Composables (UDF) | ✅ | `*Route` (stateful) → `*Content` (stateless) split |
| Material 3 + dynamic colour | ✅ | Light/dark + Material You wallpaper-based palette on Android 12+ |
| Bottom navigation | ✅ | 4 destinations + nested detail screen |
| GPS hike tracking | ✅ | `LocationTracker` + `TrackingViewModel` |
| Reviews, comments, follows | ✅ | All wired through `SocialRepository` |
| Achievements catalogue | ✅ | Definitions + earned join, ready to be filled in |
| Emergency contacts | ✅ | Schema + repo + DAO; UI editor is a recommended extension |
| Tests | ✅ | Unit + instrumented + Compose UI tests |
| AR / camera AI nature ID | ⛔️ | Out of scope for the assignment — see §11 |
| Fall detection | ⛔️ | Schema is ready; sensor wiring is an extension |

---

## 2. Prerequisites

Install these once on your machine:

| Tool | Version | Why |
|---|---|---|
| **Android Studio** | Ladybug (2024.2.1) or newer | Required for Compose 1.7 + AGP 8.7 |
| **JDK** | 17 | The Gradle toolchain expects JVM 17 |
| **Android SDK** | API 35 (compileSdk) + API 26 (minSdk) | Set in `app/build.gradle.kts` |
| **A Google account** | — | Needed for Firebase + Maps API console |
| **A physical Android phone or emulator** | API 26+ | Emulator works, but GPS testing is easier on a real device |

Android Studio handles JDK + SDK installation automatically once you open the project; you only have to install Android Studio yourself.

---

## 3. Importing the project

1. Unzip / clone this folder anywhere (Desktop, your repos directory, …).
2. Open Android Studio → **File → Open** → select the `Android-Mobile-App` folder (the one containing `settings.gradle.kts`).
3. Android Studio will say *"Trust and Open the Gradle Project?"* → **Trust Project**.
4. Wait for Gradle to sync. The first sync downloads the Android Gradle Plugin 8.7, Compose BOM, Firebase, Room compilers, etc. — depending on bandwidth this can take 5–10 minutes.
5. Android Studio may prompt to install missing platforms (API 35) — accept.
6. **Important — see §4 before running the app**, because two manual files are required.

---

## 4. One-time setup

The repo deliberately omits two files that are usually project-specific (and would be a security risk if committed):

### 4.1 Firebase (`app/google-services.json`)

The Firebase plugin reads this file at build time. Without it the build will fail with `File google-services.json is missing`.

1. Open the [Firebase Console](https://console.firebase.google.com/) and click **Add project**.
2. Name the project (e.g. `WildTrail-dev`) → continue → disable Analytics if you want a faster setup.
3. Inside your project, click **Add app → Android**.
4. Use the package name **`com.wildtrail.app`** *exactly*. The SHA-1 fingerprint is optional for email/password auth, you can skip it.
5. Click *Download `google-services.json`*. Move that file to **`app/google-services.json`** of this project (next to `app/build.gradle.kts`).
6. In the Firebase Console:
   - **Build → Authentication → Sign-in method → Email/Password → Enable**.
   - **Build → Cloud Firestore → Create database** → Start in *test mode* (you can tighten the rules later) → choose any region.

A reference template (`app/google-services.json.template`) is included so you can see the expected shape — never commit your real file.

### 4.2 Google Maps API key (`local.properties`)

The hike map uses the Google Maps SDK. You need an API key.

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create (or select) a project — you can reuse the Firebase project; Firebase actually creates a Cloud project for you.
3. **APIs & Services → Library** → enable **Maps SDK for Android**.
4. **APIs & Services → Credentials → Create credentials → API key**. Copy the key.
5. In the project root, open `local.properties` (Android Studio creates this file automatically; if not, create it). Add a line:
   ```properties
   MAPS_API_KEY=PASTE_YOUR_KEY_HERE
   ```
6. The key is forwarded into `AndroidManifest.xml` via the `manifestPlaceholders["MAPS_API_KEY"]` configuration in `app/build.gradle.kts`. You don't have to edit anything else.

### 4.3 (Optional) Restrict the keys

For a real app you'd restrict the Maps key to your package + SHA-1 and tighten the Firestore rules. For the assignment the defaults are fine.

---

## 5. Project architecture

### 5.1 The 30-second tour

```
┌──────────────────────────────────────────────────────────────────────┐
│                  Composable UI  (Stateless)                         │
│  HomeContent · LoginContent · TrackingScreen · ProfileContent · …   │
└─────────────────────────▲────────────────┬───────────────────────────┘
                state ▲   │  events ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       ViewModel  (StateFlow<UiState>)               │
│   HomeViewModel · AuthViewModel · TrackingViewModel · …             │
└─────────────────────────▲───────────────────────────────────────────┘
                          │ calls
┌─────────────────────────────────────────────────────────────────────┐
│                       Repository  (single source of truth)         │
│   AuthRepository · HikeLogRepository · SocialRepository · …         │
└──────────┬──────────────────────────────────┬────────────────────────┘
           │                                  │
   ┌───────▼────────┐                ┌────────▼─────────┐
   │   Room (local) │  ← always      │ Firestore + Auth │ ← network
   │  WildTrailDB   │     queried    │   FirebaseService│
   └────────────────┘     first      └──────────────────┘
```

### 5.2 Architectural choices

| Concern | Choice | Rationale |
|---|---|---|
| **UI toolkit** | Jetpack Compose | Declarative, idiomatic for Kotlin, the modern Android default |
| **Architecture** | MVVM + Repository | Separates concerns; ViewModels survive config changes via `viewModelScope` + `SavedStateHandle` |
| **State holding** | `StateFlow<UiState>` | Cold flow upgraded to hot via `stateIn`, lifecycle-aware via `collectAsStateWithLifecycle` |
| **Unidirectional Data Flow** | State flows down (props), events flow up (`onSomething: () -> Unit`) | Stateless Composables are previewable + testable |
| **Local cache** | Room | Coroutines-friendly, JSON converters keep route GPS points compact |
| **Remote** | Firebase Auth + Firestore | Free tier, snapshot-listener API maps cleanly to `Flow` |
| **Sync strategy** | Offline-first | Always read Room; collect Firestore in the background and write through |
| **Navigation** | Navigation Compose | Type-safe enough with string routes; supports nested sub-graphs |
| **DI** | Manual `AppContainer` | Zero compile-time overhead; trivial to swap out for Hilt later |

### 5.3 Offline-first behaviour

1. The first read for any screen **always** comes from Room (`*Dao.observe…`).
2. In parallel, the repository starts a Firestore snapshot listener and writes new values back into Room.
3. Because Room's `Flow` re-emits on every write, the screen automatically updates the moment Firestore data arrives.
4. Writes go to Room first (so the UI is updated instantly), then to Firestore. Firestore failures are surfaced as `errorMessage` on the relevant `UiState`.

---

## 6. Package structure

Every file is rooted at `app/src/main/java/com/wildtrail/app/`.

```
com/wildtrail/app/
├── WildTrailApp.kt            ← Application class — owns the AppContainer
├── MainActivity.kt            ← Single Activity hosting Compose
│
├── di/                        ← Manual dependency-injection container
│   └── AppContainer.kt
│
├── domain/                    ← Pure-Kotlin domain models (no Android deps)
│   └── model/
│       ├── User.kt
│       ├── HikeLog.kt          (HikeLog + GeoPoint + SurfaceType)
│       ├── TrailReview.kt
│       ├── Social.kt           (UserFollow, FollowedTrail, HikeComment)
│       ├── Achievement.kt      (AchievementDefinition + UserAchievement + AchievementCategory)
│       └── EmergencyContact.kt
│
├── data/                      ← Data layer
│   ├── local/                 ← Room (offline cache)
│   │   ├── WildTrailDatabase.kt
│   │   ├── converter/
│   │   │   └── Converters.kt
│   │   ├── entity/
│   │   │   ├── UserEntity.kt
│   │   │   ├── HikeLogEntity.kt
│   │   │   ├── TrailReviewEntity.kt
│   │   │   ├── SocialEntities.kt   (UserFollow + FollowedTrail + HikeComment)
│   │   │   ├── AchievementEntities.kt
│   │   │   └── EmergencyContactEntity.kt
│   │   └── dao/
│   │       ├── UserDao.kt
│   │       ├── HikeLogDao.kt
│   │       ├── TrailReviewDao.kt
│   │       ├── SocialDao.kt        (UserFollowDao, FollowedTrailDao, HikeCommentDao)
│   │       ├── AchievementDao.kt
│   │       └── EmergencyContactDao.kt
│   │
│   ├── remote/                ← Firebase wrappers + DTOs
│   │   ├── FirebaseAuthService.kt
│   │   ├── FirestoreService.kt
│   │   └── dto/
│   │       ├── UserDto.kt
│   │       ├── HikeLogDto.kt
│   │       └── OtherDtos.kt        (reviews, follows, comments, achievements, contacts)
│   │
│   └── repository/            ← Single source of truth (combines local + remote)
│       ├── AuthRepository.kt
│       ├── UserRepository.kt
│       ├── HikeLogRepository.kt
│       ├── SocialRepository.kt
│       ├── AchievementRepository.kt
│       └── EmergencyContactRepository.kt
│
├── ui/                        ← Compose UI + ViewModels (one folder per feature)
│   ├── WildTrailRoot.kt       ← Top-level composable: scaffold + navigation
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Shape.kt
│   │   └── Theme.kt
│   ├── navigation/
│   │   ├── Destinations.kt
│   │   └── WildTrailNavGraph.kt
│   ├── components/
│   │   ├── HikeCard.kt
│   │   └── BottomNavBar.kt
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   └── LoginScreen.kt
│   ├── home/
│   │   ├── HomeViewModel.kt
│   │   └── HomeScreen.kt
│   ├── explore/
│   │   ├── ExploreViewModel.kt
│   │   └── ExploreScreen.kt
│   ├── tracking/
│   │   ├── TrackingViewModel.kt
│   │   └── TrackingScreen.kt
│   ├── profile/
│   │   ├── ProfileViewModel.kt
│   │   └── ProfileScreen.kt
│   └── hike/
│       ├── HikeDetailViewModel.kt
│       └── HikeDetailScreen.kt
│
└── util/                      ← Helpers with no Android dependencies
    ├── HikeMath.kt            ← Pure-Kotlin distance/elevation/XP math
    └── LocationTracker.kt     ← Fused-location wrapper exposing a Flow<GeoPoint>
```

Resources live at `app/src/main/res/`:

```
res/
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml             (light)
├── values-night/
│   └── themes.xml             (dark)
├── drawable/
│   ├── ic_splash.xml
│   └── ic_launcher_foreground.xml
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml        (adaptive)
│   └── ic_launcher_round.xml
└── xml/
    ├── backup_rules.xml
    └── data_extraction_rules.xml
```

Tests live at:

```
app/src/test/java/…              ← JVM unit tests (run on the local JVM)
app/src/androidTest/java/…       ← Instrumented tests (run on a device/emulator)
```

---

## 7. File-by-file reference

> **Convention used in this section:**
>
> - **What** — the responsibility of the file.
> - **Deps** — the libraries/imports that justify keeping the file.
> - **How** — short summary of the implementation approach.

### 7.1 Build & configuration

| File | What | Deps | How |
|---|---|---|---|
| `settings.gradle.kts` | Tells Gradle which sub-projects exist | Gradle KTS DSL | One `include(":app")` + the Compose-friendly repository config |
| `build.gradle.kts` (root) | Declares the plugins used across the project | AGP, Kotlin, KSP, Google Services, Kotlinx Serialization | Plugin declarations only; no module-level config |
| `gradle/libs.versions.toml` | Single source of truth for every dependency version (a Version Catalog) | Gradle 7.4+ | Each lib referenced from `app/build.gradle.kts` via `libs.…` aliases |
| `gradle.properties` | JVM / AndroidX flags | — | Allocates 2 GiB to Gradle, enables AndroidX + non-transitive R |
| `gradle/wrapper/gradle-wrapper.properties` | Pins the Gradle distribution | — | Gradle 8.10.2; matches AGP 8.7 |
| `app/build.gradle.kts` | The app module — Android, Compose, Room, Firebase, Maps, tests | All of the above | Reads `MAPS_API_KEY` from `local.properties` and pipes it into the manifest |
| `app/proguard-rules.pro` | R8 keep-rules for release builds | — | Keeps Firestore DTOs (Firestore uses reflection) + Kotlinx Serialization |
| `app/google-services.json.template` | Reference shape so you know what to download from Firebase | — | Replace with the real file from the Firebase console |

### 7.2 Manifest + resources

| File | What | How |
|---|---|---|
| `AndroidManifest.xml` | App permissions + Activity registration | Declares `INTERNET`, fine/coarse `LOCATION`, `CAMERA`, `VIBRATE`, foreground service location, post-notifications. Hosts a single `MainActivity`. |
| `res/values/strings.xml` | All visible UI strings | Used by `Theme.WildTrail` styles + Compose `stringResource` |
| `res/values/colors.xml` + `values-night/themes.xml` | Day/Night base theme colours | Just enough to give the Activity window a background until Compose paints |
| `res/drawable/ic_splash.xml` | Vector icon used by the SplashScreen library | Stylised mountain glyph |
| `res/drawable/ic_launcher_foreground.xml` + `mipmap-anydpi-v26/ic_launcher.xml` | Adaptive launcher icon | Forest-green background + white mountain glyph |
| `res/xml/backup_rules.xml` + `data_extraction_rules.xml` | Auto-backup behaviour for API 31+ | Default — no explicit include/exclude |

### 7.3 Application + Activity

| File | What | Deps | How |
|---|---|---|---|
| `WildTrailApp.kt` | Application class. Owns the `AppContainer` singleton. | `android.app.Application` | Constructs `DefaultAppContainer(this)` exactly once on `onCreate` |
| `MainActivity.kt` | Single Activity. Hosts Compose. | `ComponentActivity`, `androidx.activity.compose.setContent`, AndroidX SplashScreen | Installs the splash screen, calls `enableEdgeToEdge`, hands off to `WildTrailRoot` |

### 7.4 DI container

| File | What | How |
|---|---|---|
| `di/AppContainer.kt` | Manual DI: an interface + a `DefaultAppContainer` impl that constructs Room, the Firebase services, and every repository. | Tied to the Application lifecycle via a `SupervisorJob() + Dispatchers.Default` `appScope` — long-running flow listeners survive config changes. Tests substitute their own implementations of `AppContainer`. |

### 7.5 Domain models

Pure data classes — no Android dependencies, so they're trivially testable and reusable.

| File | What |
|---|---|
| `domain/model/User.kt` | The user as the UI sees it |
| `domain/model/HikeLog.kt` | Hike + `GeoPoint` (annotated `@Serializable` for JSON) + `SurfaceType` enum |
| `domain/model/TrailReview.kt` | One review per (user, hike) |
| `domain/model/Social.kt` | `UserFollow`, `FollowedTrail`, `HikeComment` |
| `domain/model/Achievement.kt` | Catalogue (`AchievementDefinition`) + earned record (`UserAchievement`) |
| `domain/model/EmergencyContact.kt` | Fall-detection contact list |

### 7.6 Room (offline cache)

| File | What | Deps | How |
|---|---|---|---|
| `data/local/WildTrailDatabase.kt` | The `RoomDatabase` singleton, version 1, `exportSchema = true` | `androidx.room.*` | Lists all 9 entities, exposes 8 DAOs; `getInstance(context)` is the lazy-thread-safe factory |
| `data/local/converter/Converters.kt` | Type converters for `List<GeoPoint>`, `List<String>`, `SurfaceType`, `AchievementCategory` | Kotlinx Serialization | List converters use JSON; enums use their `name`. Unknown enum values map to `OTHER`. |
| `data/local/entity/UserEntity.kt` | Room representation of `User` + `toDomain()` + `toEntity()` mappers | `@Entity`, `@PrimaryKey` | Primary key is `firebaseUid` (no separate local id) |
| `data/local/entity/HikeLogEntity.kt` | Hike entity with FK → user, indices on creator + isPrivate | `@ForeignKey(onDelete = CASCADE)` | Stores route as JSON via the converter |
| `data/local/entity/TrailReviewEntity.kt` | Review entity with **UNIQUE (reviewerUid, hikeId)** | Composite UNIQUE index | Enforces "one review per user per hike" |
| `data/local/entity/SocialEntities.kt` | `UserFollowEntity` (UNIQUE (follower, followee)), `FollowedTrailEntity`, `HikeCommentEntity` | Composite primary keys | All FKs cascade on user/hike deletion |
| `data/local/entity/AchievementEntities.kt` | `AchievementDefinitionEntity` (catalogue) + `UserAchievementEntity` (UNIQUE (user, achievement)) | — | Catalogue is read-mostly, earned records are append-only |
| `data/local/entity/EmergencyContactEntity.kt` | Contact entity with FK → user | — | `isPrimary` + `notifyOnFall` flags |
| `data/local/dao/UserDao.kt` | CRUD + leaderboard observation for users | Coroutines + `Flow` | `observeById`, `observeLeaderboard`, `searchByUsername` |
| `data/local/dao/HikeLogDao.kt` | Hike DAO | — | Public feed, by-creator feed, search |
| `data/local/dao/TrailReviewDao.kt` | Review DAO | — | Per-hike list + average difficulty |
| `data/local/dao/SocialDao.kt` | `UserFollowDao`, `FollowedTrailDao`, `HikeCommentDao` | — | Follow/unfollow + isFollowing observation |
| `data/local/dao/AchievementDao.kt` | Definitions + earned join query | — | `observeEarnedFor(uid)` joins on `achievementId` |
| `data/local/dao/EmergencyContactDao.kt` | Contact CRUD + fall-notify list | — | `getFallNotifyList(uid)` for the fall-detection feature |

### 7.7 Firestore + Auth (remote)

| File | What | Deps | How |
|---|---|---|---|
| `data/remote/FirebaseAuthService.kt` | Wraps `FirebaseAuth`, exposes `authStateFlow()` as a hot `callbackFlow` | `firebase-auth-ktx`, `kotlinx-coroutines-play-services` | Email/password sign-in/-up via `await()`; service is `open` so tests can subclass |
| `data/remote/FirestoreService.kt` | One `open suspend fun` / `Flow`-returning function per collection | `firebase-firestore-ktx` | `addSnapshotListener` adapted to Flow via `callbackFlow` |
| `data/remote/dto/UserDto.kt` | Firestore POJO for `users` | — | Default values in the constructor → Firestore reflection works; `toDomain()` / `toDto()` mappers |
| `data/remote/dto/HikeLogDto.kt` | Firestore POJO for `hikes` | — | Route stored as `List<Map<String, Any?>>` so it's queryable in the Firebase console |
| `data/remote/dto/OtherDtos.kt` | DTOs for reviews/follows/comments/achievements/contacts | — | Same default-constructor pattern + mappers |

### 7.8 Repositories

| File | What |
|---|---|
| `data/repository/AuthRepository.kt` | The single source of truth for "is the user logged in?". Combines Firebase Auth + Firestore profile + Room cache. Exposes `authState: StateFlow<AuthState>`. `open` so tests subclass. |
| `data/repository/UserRepository.kt` | Read-through Firestore → Room; write-through Room → Firestore for the user profile |
| `data/repository/HikeLogRepository.kt` | Same pattern for hikes; `observePublicFeed`, `observeMyHikes`, `saveHike`, `search` |
| `data/repository/SocialRepository.kt` | Aggregates reviews + user-follows + trail-follows + comments under one type |
| `data/repository/AchievementRepository.kt` | Pulls the static catalogue once; observes earned achievements |
| `data/repository/EmergencyContactRepository.kt` | Local + remote sync for emergency contacts |

### 7.9 ViewModels

Every ViewModel:
- Owns one `MutableStateFlow<XUiState>` (private) + a public `val uiState: StateFlow<XUiState>`.
- Exposes events as plain Kotlin functions called from the UI.
- Provides a `companion object factory()` so the `*Route` Composable can do `viewModel(factory = X.factory())` without ever touching `Application`.

| File | What |
|---|---|
| `ui/auth/AuthViewModel.kt` | Login + sign-up state. `onEmailChanged`, `onPasswordChanged`, `toggleMode`, `submit`. |
| `ui/home/HomeViewModel.kt` | Combines `authState` + public feed + the user's hikes via `combine` + `flatMapLatest` (the idiomatic way to switch streams when the logged-in user changes). |
| `ui/explore/ExploreViewModel.kt` | Search + featured-feed combination. |
| `ui/tracking/TrackingViewModel.kt` | Finite state machine (`IDLE → RECORDING → PAUSED → STOPPED`). Collects GPS, computes distance/elevation/XP via `HikeMath`. |
| `ui/profile/ProfileViewModel.kt` | Combines profile + hikes + earned achievements; pulls the achievement catalogue on first construction. |
| `ui/hike/HikeDetailViewModel.kt` | Reviews + comments + the hike itself for one specific `hikeId`. |

### 7.10 Compose UI

#### Theme (`ui/theme/`)

- `Color.kt` — Material 3 tonal palette (forest greens + moss + sky blue + accents).
- `Type.kt` — Material 3 type scale (`displayLarge` → `labelSmall`).
- `Shape.kt` — Slightly more rounded than the Material defaults.
- `Theme.kt` — `WildTrailTheme` Composable. Picks dynamic colour on Android 12+, otherwise our static palette. Switches light/dark with the system.

#### Navigation (`ui/navigation/`)

- `Destinations.kt` — Sealed class + bottom-bar item list. The string `route` is the only thing that escapes; argument names are exposed as `const val`.
- `WildTrailNavGraph.kt` — `NavHost` with two nested sub-graphs: **Auth** (login) and **Main** (home, explore, track, profile, hike-detail). Detail screen takes a single `{hikeId}` arg.

#### Components (`ui/components/`)

- `HikeCard.kt` — Reusable card displaying a hike's cover, title, distance/duration/likes. Stateless: takes a `HikeLog` + `onClick`.
- `BottomNavBar.kt` — `NavigationBar` driven by `currentBackStackEntryAsState()`. Uses the `popUpTo + saveState + restoreState` pattern so tab switches don't grow the back-stack.

#### Screens

Every screen has a `*Route` (stateful, ViewModel-aware) and a `*Content` (stateless) Composable.

- `ui/auth/LoginScreen.kt` — Email/password form with toggle between login & sign-up. Uses `OutlinedTextField`, `Button`, `CircularProgressIndicator`. Includes a `@Preview`.
- `ui/home/HomeScreen.kt` — `LazyColumn` with stats row, "your recent hikes", and "trending publicly" sections.
- `ui/explore/ExploreScreen.kt` — Search bar + featured feed switching to results when the query is non-blank.
- `ui/tracking/TrackingScreen.kt` — Permission banner, stats card, FSM control buttons (Start/Pause/Resume/Finish), save dialog. Uses Accompanist Permissions.
- `ui/profile/ProfileScreen.kt` — Header, achievements, "my hikes" list. Uses Coil's `AsyncImage` for the avatar.
- `ui/hike/HikeDetailScreen.kt` — Stats card, reviews list + form (1–5 sliders via `FilterChip`s), comments list + form.
- `ui/WildTrailRoot.kt` — Top-level composable. Owns the `NavController`. Reacts to `AuthState` changes and swaps sub-graphs (popping the back-stack so the user can't leak back into the auth screens after login).

### 7.11 Utilities

| File | What |
|---|---|
| `util/HikeMath.kt` | Pure-Kotlin haversine distance, elevation gain, average speed, calorie estimate, XP formula. Tested in `HikeMathTest`. |
| `util/LocationTracker.kt` | Wraps `FusedLocationProviderClient`. Exposes `observeLocation(intervalMs): Flow<GeoPoint>`. Permission check + `@SuppressLint("MissingPermission")` because we DO check first. |

---

## 8. Running the app

After completing §3 + §4:

1. Plug in a phone (with USB debugging) **or** start an emulator (any AVD with Google Play services).
2. In Android Studio, the Run-target dropdown should show your device → click **Run ▶**.
3. The first launch shows the splash screen → login screen.
4. Tap **Sign up** at the bottom, enter `you@example.com`, a password ≥ 6 chars, and a username → Create account.
5. You're now on the **Home** tab. Tap the **Track** tab → **Grant permission** → **Start hike**. Walk around (or shake the emulator's location simulator) and watch the distance / elevation grow.
6. Tap **Finish** → fill in title/description → **Save**. The hike appears in the **Profile** tab and (because you left "Public" on) in **Explore** for everyone.

---

## 9. Tests

Three test sources are included.

### 9.1 Unit tests — `app/src/test/java`

Run all of them at once:

```bash
./gradlew :app:testDebugUnitTest
```

Or in Android Studio: right-click the `test/` folder → **Run 'Tests in 'wildtrail.app'**.

| File | What it covers | Expected outcome |
|---|---|---|
| `util/HikeMathTest` | Distance / elevation / speed / calorie / XP formulas. | All 9 tests green. The Milan↔Rome test asserts the haversine distance is between 470 and 485 km — the true value is ≈ 477 km. |
| `data/local/converter/ConvertersTest` | Round-trip for `List<GeoPoint>`, `List<String>`, both enums. | All 5 tests green. Decoding a value just encoded yields the exact same data. |
| `ui/auth/AuthViewModelTest` | The auth ViewModel state machine + repository wiring. Uses a `FakeAuthRepository` that extends the real one. | All 6 tests green. Validation errors fire without touching the repo, success cases call `signIn`/`signUp` exactly once, failures populate `errorMessage`. |
| `testing/TestFakes.kt` | Shared fakes: `FakeUserDao`, `FakeAuthRepository`, mock-stubbed `FirebaseAuthService`/`FirestoreService`. | Not a test file itself — supports the others. |

### 9.2 Instrumented tests — `app/src/androidTest/java`

These need a connected device or running emulator (they're real Android processes). Run with:

```bash
./gradlew :app:connectedDebugAndroidTest
```

| File | What it covers | Expected outcome |
|---|---|---|
| `data/local/WildTrailDatabaseTest` | Room: insert/read user; persist+round-trip a hike route through the JSON converter; FK CASCADE; `Flow` emissions. | All 4 tests green. After deleting the parent user the child hike is gone (cascade). The route list survives JSON round-trip byte-for-byte. |
| `data/repository/HikeLogRepositoryTest` | The `HikeLogRepository` against an in-memory Room + a fake Firestore. | Both tests green. After `saveHike(h)` the local feed contains exactly one entry, and the fake Firestore recorded one `upsertHike` call. |
| `ui/auth/LoginScreenTest` | Compose UI test of the **stateless** `LoginContent`. | All 3 tests green. The button label is "Log in" by default and "Create account" when `isSignUp = true`; clicking the button invokes `onSubmit`. |

### 9.3 Why you can run unit tests without a Firebase project

`AuthViewModelTest` never touches the network. The test fakes pre-stub `FirebaseAuthService.authStateFlow()` to emit `null` exactly once, so the real `AuthRepository`'s `stateIn(...)` initializer terminates immediately and the ViewModel sees `AuthState.SignedOut`. The test then drives the override `signIn`/`signUp` methods on `FakeAuthRepository` to make assertions.

---

## 10. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Build fails with `File google-services.json is missing` | You haven't done §4.1 | Download the file from Firebase and drop it into `app/` |
| The map is grey / "For development purposes only" watermark | Maps API key missing or wrong | Re-check `MAPS_API_KEY` in `local.properties` (no quotes, no trailing space) and that **Maps SDK for Android** is enabled on the Cloud project |
| `IllegalStateException: Default FirebaseApp is not initialized` | The `google-services` plugin didn't run | `Build → Clean Project`, then `Build → Rebuild Project` |
| `Location permission required` keeps showing | You denied the runtime permission and the system stopped re-asking | App settings → Permissions → Location → Allow |
| KSP / Room compile errors after editing entities | Schema changed without a migration | The DB uses `fallbackToDestructiveMigration()` (see `WildTrailDatabase`); uninstall + reinstall the app |
| `IncompatibleClassChangeError` in tests | Robolectric jar conflict | `./gradlew --refresh-dependencies` |

---

## 11. Extension ideas (intentionally not implemented)

These features are mentioned in the project brief but were too large to ship inside the assignment scope. The architecture is ready for them — every entry below explains exactly where to plug code in.

1. **AI nature discovery** — call ML Kit (or Gemini Nano via `aicore`) from the Track screen's camera FAB to identify plants/animals. Store findings in a new `Sighting` entity + repository.
2. **AR navigation** — open `Track` in a CameraX preview overlaying compass bearings (`SensorManager.TYPE_ROTATION_VECTOR`). Already-covered permissions: `CAMERA` + sensors.
3. **Fall detection** — listen to `TYPE_LINEAR_ACCELERATION` from a foreground service while a hike is recording; if a 2-second freefall + sudden stop is detected, dial the primary `EmergencyContact` via `Intent.ACTION_DIAL`.
4. **Steps** — add `ActivityRecognitionClient` and store the count in `HikeLog.stepCount`.
5. **Image uploads** — wire `coverPhotoUrl` and comment `photoUrls` to Firebase Storage. The dependency is already declared (`firebase.storage`).
6. **Achievement evaluation** — after each saved hike, evaluate the catalogue against the user's stats and call `AchievementRepository.award(...)` for any newly-met thresholds.
7. **Leaderboards** — `UserDao.observeLeaderboard(limit)` already exists; just add a screen that consumes it.
8. **Notifications** — `notifyOnNewReview` / `notifyOnNewHike` flags exist; wire them up via Firebase Cloud Messaging (FCM).

---

Have fun on the trails. 🌲
