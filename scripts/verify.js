const admin = require('firebase-admin');

try {
  const serviceAccount = require('./serviceAccountKey.json');
  admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
} catch (e) {
  console.error('Missing scripts/serviceAccountKey.json — download from Firebase Console.');
  process.exit(1);
}

const db = admin.firestore();

async function main() {
  const projectId = admin.app().options.credential?.projectId
    || require('./serviceAccountKey.json').project_id;
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
    console.log('\nNo properties found. Run: node seed.js --reset');
    process.exit(1);
  }

  const sample = await db.collection('properties').limit(1).get();
  const doc = sample.docs[0];
  console.log('\nSample property document:');
  console.log(`  id: ${doc.id}`);
  console.log(`  title: ${doc.get('title')}`);
  console.log(`  ownerId: ${doc.get('ownerId')}`);
  console.log(`  priceBwp: ${doc.get('priceBwp')}`);
  console.log('\nDatabase looks seeded.');
  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
