const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

const PROVIDER_UIDS = [
  'yBFoS19T0eehc8ETeb6VHkv3mrR2',
  'LJHrsFFuwIRjBAFYeA5FpkJ50vm2',
  'qX3SOV1ckoMGO4HfaJuGXbjxk232',
];

const STUDENT_UIDS = [
  'fH2kLm9NpQrStUvWxYzA1bCdEfGh01',
  'gJ3mNo0OqRsTuVwXyZaB2cDeFgHiJk02',
];

const PROVIDERS = [
  { uid: PROVIDER_UIDS[0], email: 'provider1@findahome.demo', fullName: 'Campus Homes Botswana' },
  { uid: PROVIDER_UIDS[1], email: 'provider2@findahome.demo', fullName: 'Gaborone Student Lets' },
  { uid: PROVIDER_UIDS[2], email: 'provider3@findahome.demo', fullName: 'Phakalane Properties' },
];

const STUDENTS = [
  { uid: STUDENT_UIDS[0], email: 'student1@findahome.demo', fullName: 'Thabo Molefe' },
  { uid: STUDENT_UIDS[1], email: 'student2@findahome.demo', fullName: 'Amantle Kgosana' },
];

const LOCATIONS = [
  'Gaborone',
  'Block 6',
  'Block 8',
  'Block 9',
  'Phakalane',
  'Mogoditshane',
  'Village',
  'Gaborone, Block 6',
  'Gaborone, Block 8',
  'Gaborone, Block 9',
  'Phakalane, Gaborone',
  'Mogoditshane, Near UB',
  'Village, Gaborone',
];

const TITLE_PREFIXES = [
  'Modern Studio',
  'Cozy Student Room',
  'Spacious Apartment',
  'Furnished Bachelor Flat',
  'Campus Close Room',
  'Bright Single Room',
  'Shared Student House Room',
  'En-suite Student Studio',
  'Affordable Bedsitter',
  'Two-Bedroom Flat',
  'Garden Cottage Room',
  'Secure Complex Unit',
  'Newly Renovated Room',
  'Quiet Study Room',
  'Walk-to-Campus Studio',
];

const TITLE_SUFFIXES = [
  'Near UB',
  'With Wi-Fi',
  'All Bills Included',
  'Furnished',
  'Available Now',
  'Ideal for Students',
  'Parking Included',
  '24hr Security',
  'Self-Catering',
  'Ground Floor',
];

const IMAGE_URLS = [
  'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800&q=80',
  'https://images.unsplash.com/photo-1522708323590-d24dbb521c0c?w=800&q=80',
  'https://images.unsplash.com/photo-1560448204-e4f9c3e1e631?w=800&q=80',
  'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=80',
  'https://images.unsplash.com/photo-1505693416388-ac6ce68f61e9?w=800&q=80',
  'https://images.unsplash.com/photo-1484154218962-a197022815936?w=800&q=80',
  'https://images.unsplash.com/photo-1560185127-872d1bc887ff?w=800&q=80',
  'https://images.unsplash.com/photo-1570129477492-45d8953fbbab?w=800&q=80',
  'https://images.unsplash.com/photo-1560185007-cde436f6a4d8?w=800&q=80',
  'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800&q=80',
  'https://images.unsplash.com/photo-1501183638710-841dd1904471?w=800&q=80',
  'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=80',
  'https://images.unsplash.com/photo-1567767292270-a7f3ad04f01a?w=800&q=80',
  'https://images.unsplash.com/photo-1505691723518-36a5ac3be353?w=800&q=80',
  'https://images.unsplash.com/photo-1560448075-cbc16ba4a9d0?w=800&q=80',
  'https://images.unsplash.com/photo-1523217582562-09d0def993a6?w=800&q=80',
  'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800&q=80',
  'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=800&q=80',
  'https://images.unsplash.com/photo-1600566753190-17f0baa424a8?w=800&q=80',
  'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&q=80',
  'https://images.pexels.com/photos/259588/pexels-photo-259588.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/1396122/pexels-photo-1396122.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/164338/pexels-photo-164338.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/1571460/pexels-photo-1571460.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/1457842/pexels-photo-1457842.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/323780/pexels-photo-323780.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/323705/pexels-photo-323705.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/276724/pexels-photo-276724.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/439391/pexels-photo-439391.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/1080721/pexels-photo-1080721.jpeg?auto=compress&cs=tinysrgb&w=800',
];

const PROPERTY_COUNT = 350;
const BATCH_SIZE = 450;
const RESET = process.argv.includes('--reset');

function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pick(arr) {
  return arr[randInt(0, arr.length - 1)];
}

function shuffle(arr) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = randInt(0, i);
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

function uniqueImages(count) {
  const pool = shuffle(IMAGE_URLS);
  const urls = [];
  for (let i = 0; i < count; i++) {
    urls.push(pool[i % pool.length]);
  }
  return [...new Set(urls)];
}

function randomTimestampWithinDays(days) {
  const now = Date.now();
  const offset = randInt(0, days * 24 * 60 * 60 * 1000);
  return now - offset;
}

async function deleteCollection(collectionName) {
  const col = db.collection(collectionName);
  let deleted = 0;
  while (true) {
    const snap = await col.limit(500).get();
    if (snap.empty) break;
    const batch = db.batch();
    snap.docs.forEach((doc) => batch.delete(doc.ref));
    await batch.commit();
    deleted += snap.size;
    process.stdout.write(`Deleted ${deleted} from ${collectionName}\r`);
  }
  if (deleted > 0) console.log(`\nCleared ${collectionName}: ${deleted} documents`);
}

async function commitBatches(writes) {
  for (let i = 0; i < writes.length; i += BATCH_SIZE) {
    const batch = db.batch();
    const chunk = writes.slice(i, i + BATCH_SIZE);
    chunk.forEach(({ ref, data }) => batch.set(ref, data));
    await batch.commit();
    process.stdout.write(`Committed ${Math.min(i + BATCH_SIZE, writes.length)}/${writes.length}\r`);
  }
  console.log('');
}

function buildUsers() {
  const writes = [];
  PROVIDERS.forEach((p) => {
    writes.push({
      ref: db.collection('users').doc(p.uid),
      data: {
        uid: p.uid,
        email: p.email,
        fullName: p.fullName,
        role: 'PROVIDER',
        isVerified: true,
        phone: `+267 7${randInt(1000000, 9999999)}`,
        verificationStatus: 'VERIFIED',
        providerBusinessName: p.fullName,
        providerContactAddress: pick(LOCATIONS),
        providerPropertyCount: Math.floor(PROPERTY_COUNT / PROVIDER_UIDS.length),
      },
    });
  });
  STUDENTS.forEach((s) => {
    writes.push({
      ref: db.collection('users').doc(s.uid),
      data: {
        uid: s.uid,
        email: s.email,
        fullName: s.fullName,
        role: 'STUDENT',
        isVerified: true,
        phone: `+267 7${randInt(1000000, 9999999)}`,
        verificationStatus: 'VERIFIED',
        studentInstitution: 'University of Botswana',
        studentPreferredLocation: pick(LOCATIONS),
        studentBudgetMax: randInt(800, 2500),
      },
    });
  });
  return writes;
}

function buildProperties() {
  const writes = [];
  const perProvider = Math.floor(PROPERTY_COUNT / PROVIDER_UIDS.length);
  let remainder = PROPERTY_COUNT % PROVIDER_UIDS.length;
  const assignments = [];
  PROVIDER_UIDS.forEach((uid) => {
    let count = perProvider;
    if (remainder > 0) {
      count += 1;
      remainder -= 1;
    }
    for (let i = 0; i < count; i++) assignments.push(uid);
  });
  const shuffledOwners = shuffle(assignments);
  for (let i = 0; i < PROPERTY_COUNT; i++) {
    const ownerId = shuffledOwners[i];
    const createdAt = randomTimestampWithinDays(30);
    const updatedAt = createdAt + randInt(0, 5 * 24 * 60 * 60 * 1000);
    const isRented = Math.random() < 0.2;
    const imageCount = randInt(1, 3);
    const title = `${pick(TITLE_PREFIXES)} — ${pick(TITLE_SUFFIXES)}`;
    const location = pick(LOCATIONS);
    const priceBwp = randInt(800, 3500);
    const roomCount = randInt(1, 4);
    writes.push({
      ref: db.collection('properties').doc(),
      data: {
        ownerId,
        title,
        location,
        priceBwp,
        roomCount,
        availabilityStatus: isRented ? 'RENTED' : 'AVAILABLE',
        availabilityDate: new Date(createdAt).toISOString().slice(0, 10),
        imageUrls: uniqueImages(imageCount),
        description: `${title} in ${location}. ${roomCount} room(s), ideal for students. Contact provider for viewing.`,
        createdAt,
        updatedAt,
      },
    });
  }
  return writes;
}

async function main() {
  console.log('FindHome Firestore seed');
  if (RESET) {
    console.log('Reset mode: deleting users and properties...');
    await deleteCollection('properties');
    await deleteCollection('users');
  }
  const userWrites = buildUsers();
  const propertyWrites = buildProperties();
  console.log(`Seeding ${userWrites.length} users...`);
  await commitBatches(userWrites);
  console.log(`Seeding ${propertyWrites.length} properties...`);
  await commitBatches(propertyWrites);
  console.log('Done.');
  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
