# Firestore seed guide (assignment mode)

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
