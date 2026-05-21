/**
 * Backfill missing property photos and optionally remove stale legacy listings.
 *
 *   node backfill-property-images.js
 *   node backfill-property-images.js --prune-legacy
 *   node backfill-property-images.js --upgrade-drawables   (replace drawable keys with URLs)
 *   node backfill-property-images.js --force-all             (rewrite every property imageUrls)
 */
const admin = require('firebase-admin');
const {
  IMAGE_URLS,
  DRAWABLE_KEYS,
  randInt,
  uniqueImages,
  hasUsableImages,
  hasRemoteImages,
  isRemoteUrl,
} = require('./image-urls');

let serviceAccount;
try {
  serviceAccount = require('./serviceAccountKey.json');
} catch {
  console.error('Missing scripts/serviceAccountKey.json — download from Firebase Console.');
  process.exit(1);
}

admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();
const PRUNE_LEGACY = process.argv.includes('--prune-legacy');
const UPGRADE_DRAWABLES = process.argv.includes('--upgrade-drawables');
const FORCE_ALL = process.argv.includes('--force-all');
const BATCH_SIZE = 400;

function onlyDrawableKeys(imageUrls) {
  if (!hasUsableImages(imageUrls)) return false;
  return imageUrls.every((u) => DRAWABLE_KEYS.includes(String(u).trim()));
}

function hasLegacyDrawableKeys(imageUrls) {
  if (!hasUsableImages(imageUrls)) return false;
  return imageUrls.some((u) => DRAWABLE_KEYS.includes(String(u).trim()));
}

async function deleteCollection(collectionName) {
  const col = db.collection(collectionName);
  let deleted = 0;
  while (true) {
    const snap = await col.limit(500).get();
    if (snap.empty) break;
    const batch = db.batch();
    snap.docs.forEach((d) => batch.delete(d.ref));
    await batch.commit();
    deleted += snap.size;
    process.stdout.write(`Deleted ${deleted} from ${collectionName}\r`);
  }
  if (deleted > 0) console.log(`\nCleared ${collectionName}: ${deleted} documents`);
  return deleted;
}

async function backfillProperties() {
  const snap = await db.collection('properties').get();
  const writes = [];
  let skipped = 0;

  for (const doc of snap.docs) {
    const data = doc.data();
    const imageUrls = Array.isArray(data.imageUrls) ? data.imageUrls : [];
    const needsPhotos =
      FORCE_ALL ||
      !hasUsableImages(imageUrls) ||
      hasLegacyDrawableKeys(imageUrls) ||
      (UPGRADE_DRAWABLES && onlyDrawableKeys(imageUrls)) ||
      (!hasRemoteImages(imageUrls) && UPGRADE_DRAWABLES);

    if (!needsPhotos) {
      skipped += 1;
      continue;
    }

    const count = randInt(1, 3);
    const nextUrls = uniqueImages(count);
    writes.push({
      ref: doc.ref,
      data: {
        imageUrls: nextUrls,
        updatedAt: Date.now(),
      },
    });
  }

  for (let i = 0; i < writes.length; i += BATCH_SIZE) {
    const batch = db.batch();
    writes.slice(i, i + BATCH_SIZE).forEach(({ ref, data }) => batch.update(ref, data));
    await batch.commit();
    process.stdout.write(`Updated ${Math.min(i + BATCH_SIZE, writes.length)}/${writes.length} properties\r`);
  }
  if (writes.length > 0) console.log('');

  return { total: snap.size, updated: writes.length, skipped };
}

async function backfillLegacyListings() {
  const snap = await db.collection('listings').get();
  const writes = [];

  for (const doc of snap.docs) {
    const imageUrl = String(doc.get('imageUrl') || '').trim();
    if (imageUrl.length > 0 && (isRemoteUrl(imageUrl) || DRAWABLE_KEYS.includes(imageUrl))) {
      continue;
    }
    writes.push({
      ref: doc.ref,
      data: {
        imageUrl: IMAGE_URLS[randInt(0, IMAGE_URLS.length - 1)],
      },
    });
  }

  for (let i = 0; i < writes.length; i += BATCH_SIZE) {
    const batch = db.batch();
    writes.slice(i, i + BATCH_SIZE).forEach(({ ref, data }) => batch.update(ref, data));
    await batch.commit();
  }

  return { total: snap.size, updated: writes.length };
}

async function sampleProperty() {
  const snap = await db.collection('properties').limit(1).get();
  if (snap.empty) return null;
  const doc = snap.docs[0];
  return { id: doc.id, title: doc.get('title'), imageUrls: doc.get('imageUrls') };
}

async function main() {
  console.log('FindHome — backfill property images');
  console.log(`Project: ${serviceAccount.project_id}`);
  if (FORCE_ALL) console.log('Mode: replace ALL property photos with housing-only URLs');
  if (UPGRADE_DRAWABLES) console.log('Mode: upgrade drawable keys → remote URLs');
  if (PRUNE_LEGACY) console.log('Mode: delete legacy listings collection after backfill');

  const propertyStats = await backfillProperties();
  console.log(`Properties: ${propertyStats.total} total, ${propertyStats.updated} updated, ${propertyStats.skipped} already OK`);

  const legacyStats = await backfillLegacyListings();
  if (legacyStats.total > 0) {
    console.log(`Legacy listings: ${legacyStats.total} total, ${legacyStats.updated} imageUrl fixed`);
  }

  if (PRUNE_LEGACY) {
    await deleteCollection('listings');
    console.log('Legacy listings collection removed (app uses properties).');
  }

  const sample = await sampleProperty();
  if (sample) {
    console.log('\nSample property after backfill:');
    console.log(`  id: ${sample.id}`);
    console.log(`  title: ${sample.title}`);
    console.log(`  imageUrls: ${JSON.stringify(sample.imageUrls)}`);
  } else {
    console.warn('\nNo properties in Firestore. Run: node seed.js --reset');
  }

  console.log('\nDone. Rebuild/reinstall the app and refresh Home.');
  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
