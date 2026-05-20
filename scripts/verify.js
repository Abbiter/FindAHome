const admin = require('firebase-admin');
const { hasUsableImages, hasRemoteImages } = require('./image-urls');

try {
  const serviceAccount = require('./serviceAccountKey.json');
  admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
} catch (e) {
  console.error('Missing scripts/serviceAccountKey.json — download from Firebase Console.');
  process.exit(1);
}

const db = admin.firestore();

async function main() {
  const projectId = require('./serviceAccountKey.json').project_id;
  console.log(`Project: ${projectId}`);
  console.log('(App must use the same project: findahome-50b4d)\n');

  const [users, properties, conversations, listings] = await Promise.all([
    db.collection('users').count().get(),
    db.collection('properties').count().get(),
    db.collection('conversations').count().get(),
    db.collection('listings').count().get(),
  ]);

  const counts = {
    users: users.data().count,
    properties: properties.data().count,
    conversations: conversations.data().count,
    listings: listings.data().count,
  };

  console.log('Collection counts:');
  Object.entries(counts).forEach(([name, count]) => {
    console.log(`  ${name.padEnd(14)} ${count}`);
  });

  if (counts.properties < 1) {
    console.log('\nNo properties found. Run: npm run seed:reset');
    process.exit(1);
  }

  const propsSnap = await db.collection('properties').limit(200).get();
  let missingPhotos = 0;
  let drawableOnly = 0;
  let withRemote = 0;
  propsSnap.docs.forEach((doc) => {
    const urls = doc.get('imageUrls') || [];
    if (!hasUsableImages(urls)) missingPhotos += 1;
    else if (hasRemoteImages(urls)) withRemote += 1;
    else drawableOnly += 1;
  });

  const sample = await db.collection('properties').limit(1).get();
  const doc = sample.docs[0];
  console.log('\nSample property (first doc):');
  console.log(`  id: ${doc.id}`);
  console.log(`  title: ${doc.get('title')}`);
  console.log(`  imageUrls: ${JSON.stringify(doc.get('imageUrls'))}`);

  console.log(`\nPhoto health (sample of ${propsSnap.size} properties):`);
  console.log(`  with remote URLs:  ${withRemote}`);
  console.log(`  drawable keys only: ${drawableOnly}`);
  console.log(`  missing photos:     ${missingPhotos}`);

  if (missingPhotos > 0) {
    console.log('\nFix: npm run backfill-images');
    process.exit(1);
  }
  if (counts.listings > 0 && counts.properties > 0) {
    console.log('\nTip: legacy `listings` can duplicate Home feed. Run:');
    console.log('  npm run backfill-images:full');
  }

  console.log('\nDatabase looks ready for the app.');
  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
