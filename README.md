# FindAHome

Android app for **student housing in Gaborone**. Students browse listings, save favorites, reserve rooms, chat with providers, and get local notifications when new matches appear. Property owners (providers) manage listings and reply to inquiries.

**Repository:** [https://github.com/Abbiter/FindAHome](https://github.com/Abbiter/FindAHome)

## Download the APK

Pre-built debug APK (install on a device or emulator):

| File | Location |
|------|----------|
| **FindAHome v1.0 (debug)** | [`releases/FindAHome-v1.0-debug.apk`](releases/FindAHome-v1.0-debug.apk) |

1. Download the APK from the link above (or clone this repo and open the `releases` folder).
2. On your phone: enable **Install unknown apps** for your browser/files app, then open the APK.
3. Requires **Android 7.0+** (API 24). Internet permission is required for listing photos and Firebase.

To rebuild locally:

```powershell
.\gradlew.bat assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Tech stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (student flows), XML layouts (some provider screens) |
| Architecture | MVVM (`ViewModel` + `StateFlow` / LiveData) |
| Backend | **Firebase Authentication** + **Cloud Firestore** (no Room/SQLite) |
| Local prefs | DataStore (saved listings, notification dedup, filters) |
| Images | Coil (Compose), Glide (XML); remote URLs + vector placeholders |
| Notifications | Android NotificationManager (local, in-app listing alerts) |

**Firebase project:** `findahome-50b4d` (see `app/google-services.json`).

## Features

### Student
- Register / login / profile onboarding
- **Explore** — browse `properties` with price & location filters; rented listings hidden
- **Saved** — favorite listings; **Reserved** tab for your bookings
- Listing details, simulated card **payment / reserve** (updates Firestore to `RENTED`)
- **Messages** — chat with providers per listing (`conversations` / `messages`)
- **Notifications** — new listing matches (deduplicated, not flooded on restart)

### Provider
- Provider home, add/edit/manage listings
- Inquiries and listing detail views
- Verification flow (`users.isVerified`)

### Extension (course requirement)
- **In-app chat** between student and provider (not a campus map; map FAB shows “coming soon”)

## Project structure

```
app/src/main/java/com/example/nestore_15/
  ui/              Activities & Compose screens
  viewmodel/       MVVM ViewModels
  data/            Repositories, Firestore models
  notifications/   Listing match notifier
scripts/           Node.js Firestore seeder & image backfill
firestore.rules    Security rules (publish in Firebase Console)
releases/          Pre-built APK for submission
docs/              Additional setup notes
```

## Prerequisites

- **Android Studio** Ladybug or newer (AGP 8.5+, compile SDK 35)
- **JDK 17**
- A **Firebase** project with Auth (Email/Password) and Firestore enabled
- For seeding data: **Node.js 18+** and a Firebase **service account** key (never commit this file)

## Run from source

1. Clone the repo and open the root folder in Android Studio.
2. Confirm `app/google-services.json` matches your Firebase project (or replace with your own).
3. **Publish Firestore rules:** copy `firestore.rules` to [Firebase Console → Firestore → Rules](https://console.firebase.google.com/project/findahome-50b4d/firestore/rules) and click **Publish**.
4. Sync Gradle and run on an emulator or device (`app` module, debug).

## Seed Firestore (recommended for demos)

Without seeded data, Explore may show **no listings**.

```powershell
cd scripts
npm install
# Download service account JSON → save as scripts/serviceAccountKey.json
# (Firebase Console → Project settings → Service accounts → Generate new private key)
node seed.js --reset
```

Optional — link sample conversations to **your** student Auth UID:

```powershell
node seed.js --reset --link-student=YOUR_FIREBASE_AUTH_UID
```

Fix or refresh listing photos (remote housing URLs):

```powershell
npm run backfill-images:housing
npm run verify
```

Expect **~350** documents in `properties`. See [`scripts/README.md`](scripts/README.md) and [`docs/FIRESTORE_SEED.md`](docs/FIRESTORE_SEED.md).

### Demo accounts

Create these in **Firebase Authentication** (Email/Password) and ensure matching `users/{uid}` documents exist with the correct `role` and `isVerified: true`. The seeder uses fixed UIDs when you run `seed.js --reset` with accounts that match `scripts/seed.js`.

| Role | Example email |
|------|----------------|
| Provider | `provider1@findahome.demo`, `provider2@findahome.demo`, `provider3@findahome.demo` |
| Student | `student1@findahome.demo`, `student2@findahome.demo` |

Use the passwords you set in Firebase Auth when creating these users.

## Firestore collections (main)

| Collection | Purpose |
|------------|---------|
| `users` | Profile, role (`STUDENT` / `PROVIDER`), verification |
| `properties` | Listings (title, price BWP, location, images, availability) |
| `conversations` | Chat threads (participants, property metadata) |
| `messages` | Chat messages per conversation |
| `notifications` | In-app notification documents (where used) |

Reservation flow sets `availabilityStatus: "RENTED"`, `reservedBy`, and `reservationRef` on the property document.

## Testing checklist

- [ ] Splash → login as student → Explore shows listings (after seed)
- [ ] Filter by price / location
- [ ] Save listing → appears under **Saved**
- [ ] Reserve / pay on a listing → moves to **Reserved**, listing hidden from Explore
- [ ] Open Messages from a listing or Conversations screen
- [ ] Provider login → add/edit listing
- [ ] Notifications screen opens without duplicate flood on app restart

## Known limitations

- **No offline database (Room)** — all listing data is loaded from Firestore.
- **Payment is simulated** — no real payment gateway.
- **Map view** is a placeholder (extension focus is chat).
- Listing photos require **network** after backfill (Unsplash/Pexels URLs).
- `serviceAccountKey.json` must stay local and must **not** be pushed to GitHub.

## Course / presentation notes (CSE201-style)

- **Purpose:** Help students find and book student accommodation in Gaborone.
- **Design:** MVVM, role-based navigation (student vs provider), Firestore security rules for chat and reservations.
- **Data:** 350 seeded properties, 3 providers + 2 students in seed script.
- **Tests:** Manual UI checklist above; instrumented tests under `app/src/androidTest` if present.

## License

Academic / project submission — see course requirements. Third-party images may be subject to Unsplash/Pexels terms when using backfill scripts.

## Authors

**Larona Olefile Mothame**  
Botswana School of Business Sciences (BSBS)

*FindAHome* — student housing mobile application project.
