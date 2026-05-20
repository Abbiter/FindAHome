package com.example.nestore_15.ui.splash

/**
 * Central tuning knobs for the splash experience.
 *
 * Aurora background: each blob drifts on its own X/Y timeline (no rotation).
 * Increase duration values for slower, calmer motion.
 */
object SplashAnimationConfig {

    // --- Aurora ambient background ---

    /** Scales all blob opacities (0.5 = softer, 1.5 = stronger). */
    const val AURORA_INTENSITY = 1f

    /** Base vertical gradient subtle shift cycle (ms). */
    const val AURORA_BASE_MORPH_MS = 36_000

    /** Blob 1 — primary navy cloud (upper area). */
    const val BLOB_1_X_MS = 32_000
    const val BLOB_1_Y_MS = 41_000
    const val BLOB_1_ALPHA_MS = 22_000
    const val BLOB_1_ALPHA_MIN = 0.18f
    const val BLOB_1_ALPHA_MAX = 0.28f
    const val BLOB_1_RADIUS_FRACTION = 0.95f

    /** Blob 2 — indigo / primary-container (mid-right). */
    const val BLOB_2_X_MS = 38_000
    const val BLOB_2_Y_MS = 29_000
    const val BLOB_2_ALPHA_MS = 26_000
    const val BLOB_2_ALPHA_MIN = 0.14f
    const val BLOB_2_ALPHA_MAX = 0.24f
    const val BLOB_2_RADIUS_FRACTION = 0.88f

    /** Blob 3 — green accent (lower-left parallax). */
    const val BLOB_3_X_MS = 45_000
    const val BLOB_3_Y_MS = 33_000
    const val BLOB_3_ALPHA_MS = 30_000
    const val BLOB_3_ALPHA_MIN = 0.10f
    const val BLOB_3_ALPHA_MAX = 0.20f
    const val BLOB_3_RADIUS_FRACTION = 0.92f

    /** Blob 4 — orange accent (lower-right, slowest). */
    const val BLOB_4_X_MS = 52_000
    const val BLOB_4_Y_MS = 47_000
    const val BLOB_4_ALPHA_MS = 34_000
    const val BLOB_4_ALPHA_MIN = 0.08f
    const val BLOB_4_ALPHA_MAX = 0.16f
    const val BLOB_4_RADIUS_FRACTION = 0.85f

    // --- Logo ---

    const val LOGO_ENTRANCE_MS = 700
    const val LOGO_ENTRANCE_START_SCALE = 0.88f
    const val LOGO_FLOAT_AMPLITUDE_DP = 3f
    const val LOGO_FLOAT_MS = 3_600
    const val LOGO_BREATH_MIN = 0.99f
    const val LOGO_BREATH_MAX = 1.01f
    const val LOGO_BREATH_MS = 3_200
    const val LOGO_GLOW_MIN_ALPHA = 0.16f
    const val LOGO_GLOW_MAX_ALPHA = 0.32f
    const val LOGO_GLOW_MS = 2_400

    const val EXIT_FADE_MS = 450
    const val TAGLINE_DELAY_MS = 280
    const val TAGLINE_FADE_MS = 500
}
