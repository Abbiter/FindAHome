# Firestore seed guide (assignment mode)

## If the app shows "No listings found"

1. **Verify data exists** — `cd scripts && node verify.js` should report **properties: 350**. If zero, run the seeder (step 2).
2. **Publish Firestore rules** — Copy `firestore.rules` from the project root into [Firebase Console](https://console.firebase.google.com/project/findahome-50b4d/firestore/rules) → Publish. Conversations need the **latest** rules (`resource.data.participants`, not `get()` on the same doc).
3. **Run the Node seeder** (project `findahome-50b4d`):

```bash
cd scripts
npm install
# serviceAccountKey.json from Firebase → Project settings → Service accounts
node seed.js --reset
# Optional sample inbox for your Auth UID:
node seed.js --reset --link-student=YOUR_FIREBASE_AUTH_UID
```

4. **Sign in** as a **student**. Listings load from `properties`, not `listings`.

---

Use **drawable keys** in `imageUrls` / `imageUrl`, not Firebase Storage URLs.

## Demo accounts (create in Firebase Authentication)

| Role | Email (example) | Notes |
|------|-----------------|--------|
| Provider | `provider1@findahome.demo` | Set `users/{uid}.role` = `PROVIDER`, `isVerified` = true |
| Student | `student@findahome.demo` | Set `role` = `STUDENT`, `isVerified` = true |

## `users/{uid}` (merge)

```json
{
  "uid": "<firebase_auth_uid>",
  "email": "provider1@findahome.demo",
  "role": "PROVIDER",
  "isVerified": true,
  "fullName": "Campus Homes Ltd",
  "phone": "+267 700 0001",
  "verificationStatus": "VERIFIED",
  "providerBusinessName": "Campus Homes Ltd",
  "providerContactAddress": "Gaborone"
}
```

## `properties/{autoId}`

```json
{
  "ownerId": "<provider_uid>",
  "title": "Modern Studio Near UB",
  "description": "Furnished studio with Wi-Fi, close to campus.",
  "location": "Gaborone",
  "priceBwp": 1200,
  "roomCount": 1,
  "availabilityStatus": "AVAILABLE",
  "availabilityDate": "2026-06-01",
  "imageUrls": ["listing_interior"],
  "createdAt": 1710000000000,
  "updatedAt": 1710000000000
}
```

Add more documents with `listing_moving`, `listing_lifestyle` keys.

## Chat

`propertyImageUrl` on conversations can be a drawable key, e.g. `listing_interior`.

## Legacy `listings` collection

Optional. If used, set `imageUrl` to a drawable key instead of a URL.
