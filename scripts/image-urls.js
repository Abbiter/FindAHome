/**
 * Property listing photos only — rooms, apartments, houses (no people/portraits).
 * Unsplash + Pexels; used by seed.js and backfill-property-images.js.
 */
const IMAGE_URLS = [
  // Unsplash — interiors & exteriors
  'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800&q=80',
  'https://images.unsplash.com/photo-1522708323590-d24dbb521c0c?w=800&q=80',
  'https://images.unsplash.com/photo-1560448204-e4f9c3e1e631?w=800&q=80',
  'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=80',
  'https://images.unsplash.com/photo-1484154218962-a197022815936?w=800&q=80',
  'https://images.unsplash.com/photo-1560185127-872d1bc887ff?w=800&q=80',
  'https://images.unsplash.com/photo-1560185007-cde436f6a4d8?w=800&q=80',
  'https://images.unsplash.com/photo-1560448075-cbc16ba4a9d0?w=800&q=80',
  'https://images.unsplash.com/photo-1523217582562-09d0def993a6?w=800&q=80',
  'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800&q=80',
  'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=800&q=80',
  'https://images.unsplash.com/photo-1600566753190-17f0baa424a8?w=800&q=80',
  'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&q=80',
  'https://images.unsplash.com/photo-1600047509807-ba8f99d2cd7a?w=800&q=80',
  'https://images.unsplash.com/photo-1600210492493-0946911123ea?w=800&q=80',
  'https://images.unsplash.com/photo-1615874959470-174a4beca6e6?w=800&q=80',
  'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?w=800&q=80',
  'https://images.unsplash.com/photo-1631889993959-f3dbf7b29a80?w=800&q=80',
  'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800&q=80',
  'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800&q=80',
  'https://images.unsplash.com/photo-1605276374101-dee2bbed99b0?w=800&q=80',
  'https://images.unsplash.com/photo-1586023492125-27b8c751aff7?w=800&q=80',
  'https://images.unsplash.com/photo-1558036117-15ad022fc89e?w=800&q=80',
  'https://images.unsplash.com/photo-1540518610460-31147d5229aa?w=800&q=80',
  'https://images.unsplash.com/photo-1600585154526-990d9b39e447?w=800&q=80',
  'https://images.unsplash.com/photo-1505691723518-36a5ac3be353?w=800&q=80',
  'https://images.unsplash.com/photo-1505693416388-ac6ce68f61e9?w=800&q=80',
  // Pexels — houses & rooms (no lifestyle/people shots)
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
  'https://images.pexels.com/photos/259962/pexels-photo-259962.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/276029/pexels-photo-276029.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/1918294/pexels-photo-1918294.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/1571463/pexels-photo-1571463.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/276551/pexels-photo-276551.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/106399/pexels-photo-106399.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/3288102/pexels-photo-3288102.jpeg?auto=compress&cs=tinysrgb&w=800',
  'https://images.pexels.com/photos/5824482/pexels-photo-5824482.jpeg?auto=compress&cs=tinysrgb&w=800',
];

const DRAWABLE_KEYS = [
  'listing_interior',
  'listing_moving',
  'listing_lifestyle',
  'splash_collage_interior',
  'splash_collage_moving',
  'splash_collage_lifestyle',
];

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

function isRemoteUrl(ref) {
  const s = String(ref || '').trim().toLowerCase();
  return s.startsWith('http://') || s.startsWith('https://');
}

function hasUsableImages(imageUrls) {
  if (!Array.isArray(imageUrls)) return false;
  return imageUrls.some((u) => String(u || '').trim().length > 0);
}

function hasRemoteImages(imageUrls) {
  if (!Array.isArray(imageUrls)) return false;
  return imageUrls.some((u) => isRemoteUrl(u));
}

module.exports = {
  IMAGE_URLS,
  DRAWABLE_KEYS,
  randInt,
  pick,
  shuffle,
  uniqueImages,
  isRemoteUrl,
  hasUsableImages,
  hasRemoteImages,
};
