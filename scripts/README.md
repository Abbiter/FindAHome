# Find A Home — Firestore seed

## 1. Service account

Download `serviceAccountKey.json` from [Firebase Console](https://console.firebase.google.com/project/findahome-50b4d/settings/serviceaccounts/adminsdk) → **Generate new private key** → save as `scripts/serviceAccountKey.json`.

Must be project **`findahome-50b4d`** (same as `app/google-services.json`).

## 2. Install and seed

```powershell
cd scripts
npm install
node seed.js --reset
```

Optional — sample inbox for **your** signed-in student account (copy UID from Firebase Authentication):

```powershell
node seed.js --reset --link-student=YOUR_FIREBASE_AUTH_UID
```

## 3. Publish Firestore rules

Copy `firestore.rules` from the repo root into [Firestore Rules](https://console.firebase.google.com/project/findahome-50b4d/firestore/rules) and **Publish**.

Re-publish after every rules change (required for Messages / conversations queries).

## 4. Fix listing photos (without wiping data)

If Home shows blank cards or only a few photos:

```powershell
npm run backfill-images:full
```

This will:

- Add Unsplash/Pexels `imageUrls` to any property missing photos
- Replace drawable-only keys with real photo URLs (optional upgrade)
- Remove the old `listings` collection so it does not duplicate `properties` on Home

Lighter touch (only empty `imageUrls`):

```powershell
npm run backfill-images
```

## 5. Verify

```powershell
npm run verify
```

Expect **properties: 350** (or similar) and **missing photos: 0**.

## 6. App

- Sign in as a **student** (any Firebase Auth account).
- Home reads the `properties` collection — not `listings`.
- Rebuild/reinstall the app after script changes (needs `INTERNET` for remote photos).
- Messages only appear for conversations where your UID is in `participants` (use `--link-student` or inquire on a listing).
