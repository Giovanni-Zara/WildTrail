# WildTrail — Exam Presentation & Study Guide

A deep, question-ready explanation of the methodologies and technologies behind each part of
the demo. Structure per section: **What it is → Technologies & methodology → How it works in
this app → Likely questions (with answers).**

> ⚠️ Read the **"Honest caveats"** section at the end first — it lists the few places where the
> code and the obvious answer differ (e.g. the prediction model is *not* XGBoost), so you're
> never caught off guard.

---

## 0. Architecture overview (read this first — it answers half the questions)

**Platform & language.** Native Android app written in **Kotlin**. UI is **Jetpack Compose**
(declarative, Material 3), single-`Activity` (`MainActivity`) hosting all screens as composables.

**Architectural pattern: MVVM + unidirectional data flow (UDF).**
- **View** (Compose composables) is stateless: it renders an immutable `UiState` and emits events
  (lambdas) upward.
- **ViewModel** holds state as a `StateFlow<UiState>` and exposes functions for events. State
  flows down, events flow up — no two-way binding.
- Survives configuration changes (rotation) because the ViewModel outlives the Activity.

**Layered / clean-ish architecture (3 layers):**
1. **`ui/`** — Compose screens + ViewModels (one per screen).
2. **`domain/`** — pure Kotlin models (`HikeLog`, `User`, …) and **use cases** (e.g.
   `GetReviewSummaryUseCase`, `DetectFallUseCase`). No Android/framework dependencies.
3. **`data/`** — **repositories** that are the single API the ViewModels talk to. Each repository
   coordinates a **local** source (Room) and **remote** sources (Firestore/Storage/Retrofit).

**Offline-first / single source of truth.** The UI **always reads from the local Room database**
(SQLite). Firebase **real-time listeners** push remote changes *into* Room; the UI observes Room
`Flow`s and updates automatically. So the app works offline and stays reactive. (Conflict rules
exist, e.g. `keepingLocalPicture`, `mergeLocalMedia`, so a fresh server copy doesn't wipe a local
file path.)

**Asynchrony & reactivity: Kotlin Coroutines + Flow.** `suspend` functions for one-shot async,
`Flow`/`StateFlow` for streams. `collectAsStateWithLifecycle()` bridges Flows into Compose.

**Dependency injection.** Manual DI via a **`AppContainer`** (a *service locator*) created once in
the `Application` class (`WildTrailApp`). Every repository/service is a singleton built there and
handed to ViewModels through their factories. (No Hilt/Dagger — chosen for simplicity.)

**Navigation.** **Navigation-Compose** with **nested graphs**: an `AuthGraph` (login/sign-up) and a
`MainGraph` (the 4 bottom-nav tabs + detail screens). A `NavHost` swaps between them based on auth
state.

**Backend services.**
- **Firebase** (Backend-as-a-Service): **Authentication**, **Cloud Firestore** (real-time NoSQL
  document DB), **Cloud Storage** (media files).
- A small **Python Flask backend** (`backend/weather_proxy.py`) for three things a phone shouldn't
  do directly: **weather proxy**, **ML time prediction**, and **LLM review summary**. The app
  reaches it with **Retrofit + OkHttp + Gson**.

**Tech-stack cheat sheet**

| Concern | Technology |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Pattern | MVVM + UDF, layered architecture |
| Async | Coroutines, Flow/StateFlow |
| Local DB | Room (SQLite) + TypeConverters |
| Auth | Firebase Auth (email/password + Google) |
| Cloud DB | Cloud Firestore (real-time) |
| File storage | Firebase Cloud Storage |
| Custom backend | Python + Flask; Retrofit/OkHttp/Gson on the client |
| Maps | Google Maps SDK for Android + `maps-compose` |
| Location | Fused Location Provider + foreground Service |
| Sensors | `SensorManager` (accelerometer + gyroscope) |
| On-device ML | ML Kit Image Labeling; TensorFlow Lite (BirdNet) |
| Server ML | scikit-learn `HistGradientBoostingRegressor`; LLM via OpenRouter |
| Image loading | Coil |
| DI | Manual `AppContainer` (service locator) |

---

## 1. App overview

**What it is.** A social hiking tracker — *"Track. Discover. Share."* You record a hike via GPS,
the app computes stats (distance, elevation, speed, calories, XP), you attach photos and voice
notes, then save it publicly or privately. Other users discover hikes in a feed, like them, leave
reviews (with ratings + photos) and comments, follow each other, and earn achievements. Extra
"smart" features: weather, on-device bird/photo recognition, an ML time estimate, and an LLM that
summarizes reviews.

**One-paragraph pitch:** *"WildTrail is a native Android app, built in Kotlin with Jetpack Compose
and an MVVM, offline-first architecture. Firebase handles authentication, the real-time database
and media storage, while a small Flask backend serves a machine-learning duration predictor and an
LLM review summarizer. It also runs two ML models fully on-device — bird-call recognition and image
labeling — and tracks GPS in the background through a foreground service."*

---

## 2. Registration & login (email / password)

**Technologies & methodology.** **Firebase Authentication** (email/password provider). Auth state
is exposed reactively so the whole app reacts to login/logout.

**How it works here.**
- `LoginScreen` is a single composable that toggles between **Log in** and **Sign up** modes
  (`isSignUp`). `AuthViewModel` holds the form state and validates (email present, password ≥ 6
  chars, and on sign-up: username/sex/date-of-birth/country required).
- `AuthRepository.signUp(...)` → `FirebaseAuthService.signUp` calls
  `auth.createUserWithEmailAndPassword(...)`. Firebase creates the credential; the app then creates
  the **user profile document** in Firestore and saves it to Room. A profile picture (optional) is
  copied locally first (instant display) and uploaded to Cloud Storage in the background.
- `signIn(...)` calls `auth.signInWithEmailAndPassword(...)`, then loads the profile (remote → local
  fallback).
- **Reactive auth state:** `FirebaseAuthService.authStateFlow()` wraps Firebase's
  `AuthStateListener` in a `callbackFlow`. `AuthRepository.authState` maps it to a sealed
  `AuthState { Loading, SignedOut, SignedIn(user) }`. `WildTrailRoot` observes this and swaps
  between `AuthGraph` and `MainGraph` (with `popUpTo(...)` so you can't "back" into a logged-out
  state).

**Methodology notes.** Passwords are **never stored by the app** — Firebase handles hashing/storage
server-side. The local form state lives in the ViewModel; the credential lives with Firebase.

**Likely questions**
- *Where are passwords stored?* Never in the app or our DB — Firebase Auth stores them salted/hashed
  server-side; the client only ever sends them over TLS to Firebase.
- *How does the app know it's logged in?* A reactive `Flow` from Firebase's `AuthStateListener`;
  the root composable switches navigation graphs on `SignedIn`/`SignedOut`.
- *What if there's no network at launch?* Firebase persists the session token locally, so the user
  stays signed in; cached profile comes from Room.
- *Validation?* Client-side in the ViewModel before calling Firebase; Firebase enforces uniqueness
  of email and password policy server-side.

---

## 3. Log in with Google

**Technologies & methodology.** The **modern Credential Manager API** (`androidx.credentials`) with
**Google Identity Services** (`GetGoogleIdOption` / `GoogleIdTokenCredential`) — this is Google's
current recommended flow that **replaced the deprecated `GoogleSignInClient`**. It's based on
**OpenID Connect**: Google returns a signed **ID token (a JWT)** that Firebase verifies.

**How it works here (the exact handshake).**
1. `GoogleSignInHelper.requestIdToken(serverClientId)` builds a `GetGoogleIdOption` with the
   **server (Web) OAuth client ID** and calls `CredentialManager.getCredential(...)`. Android shows
   the account picker.
2. It extracts the **Google ID token** (a JWT containing the user's identity, signed by Google).
3. `AuthRepository.signInWithGoogleIdToken(idToken)` → `FirebaseAuthService`:
   `GoogleAuthProvider.getCredential(idToken, null)` → `auth.signInWithCredential(credential)`.
   Firebase **verifies the token's signature** and creates/links a Firebase user.
4. On first Google login the app bootstraps a Firestore profile from the Google
   `displayName`/`email`/`photoUrl`.

**Config requirements (good to mention).** Needs the **Web client ID** (`GOOGLE_WEB_CLIENT_ID`, kept
in `local.properties`, not in git) and the app's **SHA-1 fingerprint** registered in the Firebase
project so Google trusts the app.

**Likely questions**
- *Why send Google's ID token to Firebase instead of logging in directly?* So all users — email and
  Google — become one **Firebase identity** with one `uid`; the rest of the app (Firestore rules,
  ownership) only deals with Firebase uids.
- *What is the ID token?* A JWT issued and signed by Google asserting the user's identity; Firebase
  validates the signature and audience server-side — the client can't forge it.
- *Why Credential Manager and not the old API?* The legacy `GoogleSignInClient` is deprecated;
  Credential Manager is the unified Android API for Google/passkeys/password credentials.

---

## 4. Home page

**What it is.** The landing tab after login: a welcome header with the user's headline stats
(hikes, distance, XP, level) and a **"Recently from you"** list of the logged-in user's own hikes.

**Technologies & methodology.** Same MVVM/Flow pattern. `HomeViewModel` combines the user profile
flow and the user's hikes flow (from Room, synced by Firestore listeners) into a single `UiState`.
Stats like **level** come from `LevelMath.levelForXp(xp)` (`level = ⌊(1 + √(1 + 0.08·xp)) / 2⌋`),
so leveling gets progressively harder.

**How it works here.** Bottom navigation (`WildTrailBottomBar`) switches between Home/Explore/
Track/Profile. Each hike is a `HikeCard` showing a mini route map preview, distance/time/elevation,
trail-characteristic chips, and a like button. Tapping a card navigates to the Hike Detail screen
(passing the `hikeId` as a navigation argument).

**Likely questions**
- *Where does the data come from?* Room (local). A Firestore listener for "my hikes" keeps Room in
  sync; the UI just observes Room — that's the offline-first design.
- *How is level computed?* Closed-form inverse of an XP-per-level curve (`xpForLevel = 50·L·(L−1)`),
  so each level needs more XP than the last.

---

## 5. Tap on another user's profile

**What it is.** Tapping a hike's author (or a reviewer/commenter) opens **that** user's public
profile: avatar, level/XP, stats, achievements, and their public hikes.

**Technologies & methodology.** **Screen reuse via a navigation argument.** The Profile screen is a
single composable; it shows *your* profile when opened from the bottom bar (no `uid` argument) and
*another* user's profile when opened with a `uid` (`profile/{uid}` route). The same `ProfileViewModel`
takes an optional `targetUid`.

**How it works here.** `openUser(uid)` navigates to `Destination.UserProfile.create(uid)`.
`ProfileViewModel` observes `userRepository.observeUser(uid)` + that user's hikes + achievements.
`isMe` (computed by comparing `targetUid` to the logged-in uid) controls whether edit/settings/
logout actions are shown — so a foreign profile is read-only.

**Likely questions**
- *Is it a different screen?* No — same composable, parameterised by `uid`. This avoids duplicated
  UI and keeps behavior consistent.
- *How do you fetch a user you've never met?* Firestore `users/{uid}` document; cached into Room so
  it renders instantly next time.

---

## 6. Explore page

**What it is.** The discovery tab: a **"Featured this week"** public feed plus a search bar, sort
chips, and a filter menu. When you search or apply a filter it switches to a results list.

**Technologies & methodology.** Reactive composition of multiple `Flow`s in `ExploreViewModel`. The
public feed comes from `hikeLogRepository.observePublicFeed(...)` (Firestore → Room → Flow). Search
and filtering are delegated to a **Room SQL query**; sorting is done **in memory**.

**How it works here.** `ExploreUiState` holds `query`, `sort`, the draft/applied filters, the
`featured` feed and `results`. A key design point: results are shown **only when the query is
non-blank OR a filter is active** (`showingResults`); otherwise the featured feed is displayed.

**Likely questions**
- *Is the feed live?* Yes — a Firestore snapshot listener updates Room; the feed re-emits
  automatically (e.g. like counts change in real time).
- *Why separate "featured" from "results"?* So browsing (no query) shows a curated recent feed,
  while searching/filtering switches to a query-driven list.

---

## 7. Sorting button

**What it is.** Chips to sort the list: **Recent / Liked / Top rated / Longest**.

**Technologies & methodology.** A `SortOption` enum; sorting is a **pure in-memory transform** in
the ViewModel (`sortHikes`), applied to whatever list is showing (featured or results).

**How it works here.**
- `RECENT` → by `endedAt` descending.
- `MOST_LIKED` → by `likesCount` descending.
- `TOP_RATED` → by `averageRating`, tie-broken by `reviewCount`.
- `LONGEST` → by `lengthKm` descending.

Because it's a plain list sort applied to the current `Flow` emission, changing the sort is instant
and re-applies live as the underlying data updates.

**Likely questions**
- *Server-side or client-side sort?* Client-side, in memory, on the already-synced list — cheap for
  the feed sizes here and works offline. (A production app with huge feeds would sort/paginate in
  Firestore queries.)

---

## 8. Filtering menu

**What it is.** A panel to filter by **distance range, elevation range, difficulty (1–5), and
surface type** (Mountain/Forest/Coastal/Urban/Desert/Other).

**Technologies & methodology.** A `HikeFilter` immutable data class with a **draft vs applied**
pattern: you edit a `draftFilter` (sliders/chips) and only commit it on **Apply** (`appliedFilter`),
which avoids re-querying on every slider tick. `HikeFilter.isActive()` tells whether any constraint
deviates from defaults.

**How it works here.** Editing calls `onDistanceChange / onElevationChange / onDifficultyToggle /
onSurfaceTypeToggle` (updating the draft). **Apply** copies draft → applied and closes the menu;
that triggers a re-query.

**Likely questions**
- *Why draft vs applied?* So the expensive query runs once on Apply, not on every interaction —
  better UX and fewer DB hits.

---

## 9. Filtering + search bar (how they actually run)

**Technologies & methodology.** Search + filter are executed as a **single parameterised SQL query
on the local Room database** (`HikeLogDao.filter(...)`), not in Kotlin. This is fast and works
offline because Firestore has already synced public hikes into Room.

**How it works here.** In `ExploreViewModel`, `combine(query, appliedFilter)` + `flatMapLatest`
re-subscribes to `hikeLogRepository.filter(q, filter)` whenever the query or applied filter changes.
The DAO query does a `LIKE` match on title/description for the text search and `BETWEEN` / `IN`
clauses for distance, elevation, difficulty and surface. The result `Flow` stays live (e.g. like
counts update), and the ViewModel then applies the chosen **sort** in memory.

**Likely questions**
- *Full-text search?* It's a SQL `LIKE '%query%'` on title/description — substring matching, not a
  full-text index. Fine for the scale; a bigger app would use FTS or a search service.
- *Why query Room and not Firestore directly?* Offline support + speed + avoiding per-keystroke
  network calls; Firestore is the sync source, Room is the query surface.
- *Does it re-run on every keystroke?* The query Flow re-subscribes on query change
  (`flatMapLatest` cancels the previous query), but the heavy filter only changes on **Apply**.

---

## 10. Tap on a hike (Hike Detail)

**What it is.** Full hike page: route map, elevation chart, stats, trail characteristics, AI time
prediction, captured media (photos + voice notes with bird detection), community reviews (+ AI
summary) and comments.

**Technologies & methodology.** `HikeDetailViewModel` `combine`s several Flows — the hike, reviews,
comments, like state, current user — into one `UiState`. The screen is a single `LazyColumn`.
Ownership (`isMyHike`) gates owner-only actions (delete). Navigation passes the `hikeId` argument;
the VM loads everything for that id.

**Likely questions**
- *How does it know if it's my hike?* Compares the hike's `creatorFirebaseUid` to the logged-in uid.
- *Are reviews/comments live?* Yes — Firestore listeners per `hikeId`, synced to Room, observed as
  Flows.

---

## 11. Map and green trail path

**What it is.** A Google Map showing the recorded GPS track drawn as a **green polyline**.

**Technologies & methodology.** **Google Maps SDK for Android** via the **`maps-compose`** library
(declarative `GoogleMap` composable). The route is a `Polyline`; the camera auto-frames the track.

**How it works here (`RouteMap.kt`).**
- The route is `List<GeoPoint>` → mapped to `List<LatLng>`.
- A `Polyline(points = latLngs, color = Color(0xFF2E5D3A) /* green */, width = 10f)` draws the path.
- Camera logic: while **recording** it *follows* the latest point; when **viewing** a finished hike
  it builds a `LatLngBounds` over all points and animates the camera to fit the whole track.
- The map key is injected from `local.properties` (`MAPS_API_KEY`) into the manifest at build time
  — never hard-coded.

**Likely questions**
- *Where do the points come from?* The GPS samples captured during tracking, stored in the hike's
  `routeCoordinates` (lat/lng/altitude/timestamp).
- *Why a polyline and not a route from a directions API?* It's the *actual recorded* path (breadcrumb
  of GPS fixes), not a computed route between two points.
- *How is the map key protected?* It's a build-time placeholder from `local.properties` and
  restricted in Google Cloud (Android package + SHA-1 + Maps SDK only).

---

## 12. Elevation gain

**What it is.** Total meters climbed, shown as a stat and as an elevation-vs-distance chart.

**Technologies & methodology.** Computed from the **altitude** field of each GPS sample. **Only
positive deltas are summed** (cumulative ascent) — descents don't subtract. This is the standard
definition of "elevation gain."

**How it works here (`HikeMath.elevationGainMeters`).** For consecutive points,
`gain += max(0, altitude[i] − altitude[i−1])`. Distance uses the **Haversine formula** between
consecutive lat/lng points (`HikeMath.totalDistanceKm`). Altitude comes from the GPS fix
(`Location.getAltitude()`).

**Likely questions**
- *Why only positive deltas?* Elevation *gain* = total ascent; a round trip back to the start still
  has gain > 0. Net elevation would be ≈ 0.
- *How accurate is GPS altitude?* Vertical GPS accuracy is poorer than horizontal, so raw altitude
  is noisy — a production app would smooth it or use a barometer. (Good to acknowledge.)
- *What's Haversine?* Great-circle distance between two lat/lng points on a sphere — the standard
  way to turn coordinate deltas into meters.

---

## 13. Audio (and photos) recorded while hiking

> **Important wording:** the app records **photos + audio voice-notes**, *not video*. Say "photos
> and audio notes" so a code peek doesn't contradict you.

**What it is.** During an active recording you can snap **photos** and record **audio voice notes**;
each is geotagged to where you were and attached to the hike.

**Technologies & methodology.**
- **Audio:** Android `MediaRecorder` (AAC in an MPEG-4 `.m4a` container) → `AudioRecorder`.
- **Photo:** the camera capture returns a `Bitmap`, saved as JPEG by `HikeMediaStore`.
- Each capture becomes a `HikeMediaItem { id, type (PHOTO/AUDIO), filePath, lat, lng, timestamp }`
  located at the current GPS position.
- **Permissions** are requested at runtime (camera, microphone).

**How it works here (sync to other users).** When the hike is saved, `HikeLogRepository` **uploads
each media file to Firebase Cloud Storage** and stores the resulting **download URL** in the media
item (carried in the Firestore document). So other users load the same photos and stream the same
audio. Playback uses `MediaPlayer` (streams remote URLs); the bird detector downloads-and-caches a
remote clip before decoding.

**Likely questions**
- *Is there video?* No — photos and audio notes only.
- *How can another user see my media?* Files go to Cloud Storage; the hike document carries their
  URLs, so any viewer loads them (the app handles both local-file and http URLs transparently).
- *What audio format and why?* AAC/`.m4a` — well-compressed, universally supported by Android's
  `MediaCodec`, which the bird model's decoder also uses.

---

## 14. Map geotags

**What it is.** Colored pins on the hike map marking **where** each photo (azure) and voice note
(orange) was captured.

**Technologies & methodology.** Each `HikeMediaItem` stores the `lat/lng` of the capture moment.
`RouteMap` renders a `Marker` per media item, colored by type via
`BitmapDescriptorFactory.defaultMarker(hue)` and labeled "Photo N" / "Voice N" (ordered by
timestamp).

**Likely questions**
- *How is a geotag obtained?* From the current GPS fix at capture time (the same fused-location
  stream that records the route) — not from photo EXIF.
- *Why color-code?* To distinguish photo vs audio markers at a glance.

---

## 15. ML for images & bird identification (both **on-device**)

Two independent on-device models — they run **offline, no server, nothing leaves the phone**.

### 15a. Image labeling (photos)
**Technology:** **Google ML Kit — on-device Image Labeling** (the bundled default model, a
MobileNet-based classifier, ~400+ everyday labels). **Methodology:** a CNN image classifier
returning labels + confidences.

**How it works (`PhotoDescriber`).** Opens a hike photo full-screen → decode to `Bitmap` → wrap in
ML Kit `InputImage` → `labeler.process(...)` → keep labels with **confidence ≥ 0.55**, take the
**top 3**, show *"Looks like: …"*. ML Kit internally resizes/normalizes to the model's input
(~224×224) — the app passes the bitmap as-is.

**Likely questions**
- *On-device or cloud?* On-device — the model ships inside the APK; works offline; the image never
  leaves the phone.
- *Generic or trained for nature?* Generic everyday labels (Tree, Sky, Dog…), not species/plant
  level — a fair limitation to acknowledge.

### 15b. Bird identification (audio) — **BirdNet**
**Technology:** **TensorFlow Lite** running a **BirdNET** model (`assets/birdnet/model.tflite` +
`labels.txt`). **Methodology:** a CNN trained on spectrogram-like audio windows that outputs a
probability per bird species.

**How it works (`BirdNetClassifier` + `AudioPcmDecoder`).**
1. **Decode + resample** the recording to **48 kHz mono PCM float** using `MediaExtractor` +
   `MediaCodec` (handles the m4a/AAC, or downloads a remote clip first).
2. **Window** the signal into the model's input length and run the TFLite **`Interpreter`** on each
   window.
3. Apply a **sigmoid** to logits, keep the **max confidence per class** across windows, threshold at
   **0.10**, return the **top-5** species (common + scientific name).
- The `Interpreter` is serialized behind a `Mutex` (TFLite interpreters aren't thread-safe).

**Likely questions**
- *What's BirdNET?* A well-known open model for acoustic bird identification (Cornell Lab /
  TU Chemnitz); here it's exported to TFLite and embedded.
- *Why 48 kHz mono?* That's the model's expected input; we resample whatever the mic recorded.
- *Why on-device for both?* Privacy, offline use, zero per-call cost, low latency.
- *Difference vs the image model?* Image = generic ML Kit labeler; audio = a specialized
  domain model (birds) run through TensorFlow Lite.

---

## 16. Time prediction (server-side ML)

**What it is.** A **"Predict my time"** button estimating how long *this* user would take on *this*
hike, in minutes.

**Technology & methodology.** A **supervised regression** model — **scikit-learn's
`HistGradientBoostingRegressor`** (histogram-based gradient-boosted decision trees, conceptually in
the same family as XGBoost/LightGBM). Served by the **Flask** backend (`/predict`); the app calls it
with **Retrofit**.

> ⚠️ **Naming gotcha:** the training file is `train_xgboost_model.py`, but the code actually trains
> and deploys a **`HistGradientBoostingRegressor`** (see the `from sklearn.ensemble import …`
> import). Call it *"a gradient-boosted-trees regressor (scikit-learn HistGradientBoostingRegressor)"*
> — don't say "XGBoost," because the code would contradict you.

**Model details (defensible specifics).**
- **Features (8 → 11 after one-hot):** user → `xp_points`, `eta` (age), `past_hikes`, `avg_speed`;
  hike → `lunghezza` (length km), `elevation_gain`, `difficulty`, `surface_type` one-hot
  (`forest/mountain/mixed/road`).
- **Target:** `duration_min`. Trained on **`log1p(minutes)`** (log transform stabilizes the wide
  range of durations) and inverted at inference with **`expm1`**.
- **Training:** 80/20 split **stratified** by length-bucket × difficulty; hyper-params
  `max_iter=600, max_depth=6, learning_rate=0.05, l2_regularization=0.1`.
- **Reported metrics (held-out test):** **MAE ≈ 13.5 min, MAPE ≈ 6.6%, R² ≈ 0.99**, on **~1.28 M**
  rows.

**Client flow (`PredictRepository`).** Computes the user's age from date-of-birth and a heuristic
`avg_speed` from XP/age, maps `SurfaceType` → the API's surface string, POSTs `{user, hike}` to
`/predict`, gets `{duration_min}`. The backend rebuilds the exact feature vector (zero-filled +
one-hot), predicts in log-space, inverts, clamps ≥ 0.

**Likely questions**
- *Which model and why gradient boosting?* Gradient-boosted trees handle tabular, non-linear,
  mixed-scale features well without heavy preprocessing, and are strong baselines for regression.
- *Why predict in log space?* Durations span minutes→hours; `log1p` compresses the range so large
  hikes don't dominate the loss, improving relative error (MAPE).
- *Is R² = 0.99 realistic?* **It's on synthetic data** (see caveats) — it shows the model learned the
  data-generating function well; on real hikes you'd expect lower. Be honest about this.
- *Why server-side, not on-device?* The model + sklearn runtime are heavy and easy to retrain/update
  centrally; keeping it server-side avoids shipping/updating it in the app.

---

## 17. Review submission

**What it is.** On someone else's hike you can leave **one** review: a 1–5 star rating, per-trait
ratings (difficulty, mud, path clarity, fatigue, animal risk), water availability, free text, and
optional **photos**.

**Technologies & methodology.** `SubmitReviewViewModel` builds a `TrailReview`;
`SocialRepository.submitReview(...)` writes it to Room + Firestore and **uploads review photos to
Cloud Storage** (local paths first for instant display, then swapped for download URLs). After a
review changes, the hike's **denormalized aggregate** (`averageRating`, `reviewCount`) is
recomputed (`refreshAggregateRating`).

**How it works here.** One review per user per hike (the UI shows "Your review" separately and hides
"Add review" if you already reviewed or it's your own hike). Reviews are owner-deletable (and the
hike owner can delete them too, enforced by Firestore Security Rules).

**Likely questions**
- *How is the average rating kept correct?* Denormalized onto the hike document and recomputed from
  the reviews collection whenever a review is added/removed (a common NoSQL pattern — store the
  aggregate to avoid reading all reviews every time).
- *Can I review my own hike / twice?* No — gated in the UI and by data (one review doc per
  user+hike).
- *Where do review photos live?* Cloud Storage; the review document stores their URLs.

---

## 18. LLM review summarizer

**What it is.** An **"AI review summary"** button that condenses all written reviews of a hike into a
short, neutral paragraph.

**Technology & methodology.** A **Large Language Model** accessed via **OpenRouter** (an
OpenAI-API-compatible gateway). Model: **`openai/gpt-oss-120b:free`** (an open-weights GPT model,
free tier). Called **server-side** from Flask (`/summarize-reviews`) so the **API key never ships in
the app**. It's a classic **prompt-engineering / text-summarization** task with a fixed **system
prompt** instructing: read the reviews, produce a brief informative summary, no personal opinions,
answer in English.

**How it works (end-to-end).**
- `HikeDetailViewModel.summarizeReviews()` collects the non-empty review texts →
  `GetReviewSummaryUseCase` (filters empties, fails fast if none) → `ReviewSummaryRepository` →
  Retrofit POST `/summarize-reviews` `{reviews: [...]}`.
- Flask (`weather_proxy.py`) builds a chat completion: `system` = the summary instructions, `user` =
  the concatenated reviews → OpenRouter → returns `{summary, review_count}`.

**Likely questions**
- *Which LLM?* An open-weights GPT model (`gpt-oss-120b`) via OpenRouter; swappable by changing one
  env var (`LLM_MODEL`).
- *Why through a backend?* To keep the API key secret (never in the APK), centralize the prompt, and
  swap models without shipping an app update.
- *How do you control the output?* A constrained system prompt (length, neutrality, language); the
  user content is just the reviews. (You could add few-shot examples or temperature control.)
- *Hallucination risk?* The model only summarizes provided reviews, which limits it, but LLM output
  is best-effort — acceptable for a non-critical "summary" feature.

---

## Cross-cutting topics the professor may probe

**Background GPS / foreground service.** Recording continues with the screen off because a
**foreground `Service`** (`LocationService`, type `location`) owns the Fused Location subscription
and shows an ongoing notification; Android only delivers continuous background location to apps with
such a service. The service publishes fixes to a shared `Flow` the tracking ViewModel observes.

**Fall detection / emergency.** During a hike, `DetectFallUseCase` reads the **accelerometer +
gyroscope** (`SensorManager`) to detect an impact/tumble signature; on detection it raises an SOS
overlay with a countdown that would call the user's emergency contact.

**Weather.** Shown on the Track screen; fetched through the Flask **weather proxy** so the OpenWeather
API key stays server-side, with caching in Room.

**Security model.** **Firestore Security Rules** enforce that any signed-in user can *read* social
content but can only *create/update/delete their own* documents (owner = `creatorFirebaseUid` /
`reviewerUid` / `authorUid` / `userUid`), with a small exception allowing like/rating counters to be
updated and allowing a hike owner to cascade-delete its reviews/comments/likes. Cloud Storage rules
require auth for writes; media reads use tokenized download URLs.

**Testing.** Unit tests (JUnit) for repositories/use cases with fakes; instrumented tests (Room
in-memory DB, Compose UI tests) under `androidTest`.

---

## Honest caveats — be ready to defend these (don't get caught)

1. **Prediction model ≠ XGBoost.** Despite the filename `train_xgboost_model.py`, the deployed model
   is scikit-learn **`HistGradientBoostingRegressor`**. Say "gradient-boosted trees
   (HistGradientBoostingRegressor)."
2. **Prediction is trained on synthetic data** (~1.28 M generated rows). The near-perfect R² ≈ 0.99
   reflects learning the synthetic generator, **not** real-world hiking accuracy. Framing: *"proof of
   concept; the full ML pipeline (feature engineering, log-target, stratified split, evaluation by
   segment) is real and would be retrained on real telemetry."*
3. **"Video" isn't recorded** — only photos + audio notes. Use the right words.
4. **Image labeling is generic**, not nature-specialized (ML Kit base model). Bird ID *is*
   specialized (BirdNET).
5. **GPS altitude is noisy**, so elevation gain is approximate (no barometer/smoothing).
6. **Search is `LIKE` substring matching**, not a full-text index.
7. **The LLM is a free-tier hosted model** via OpenRouter; quality/latency depend on that service.
8. **Manual DI (service locator)** instead of Hilt — a deliberate simplicity choice; mention you know
   the trade-off (less compile-time safety, fine at this scale).

---

## 30-second architecture answer (memorize this)

> *"It's a native Kotlin/Jetpack-Compose app following MVVM with unidirectional data flow and an
> offline-first, layered architecture: ViewModels expose immutable state as StateFlow, repositories
> are the single API over a local Room database and remote Firebase services, and the UI always reads
> from Room while Firestore real-time listeners keep it in sync. Firebase covers auth, the real-time
> database and storage; a Flask backend serves an ML duration predictor and an LLM review summarizer;
> and two models — image labeling and BirdNET bird recognition — run fully on-device."*
