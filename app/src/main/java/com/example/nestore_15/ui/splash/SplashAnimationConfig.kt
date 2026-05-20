package com.example.nestore_15.ui.splash

/**
 * Central tuning knobs for the splash experience.
 *
 * Adjust these values to change motion without touching animation logic.
 */
object SplashAnimationConfig {
    /** Primary gradient drift cycle (ms). Higher = slower background motion. */
    const val GRADIENT_PHASE_1_MS = 14_000

    /** Secondary gradient morph cycle (ms). Use a different value than phase 1 for non-repeating feel. */
    const val GRADIENT_PHASE_2_MS = 19_000

    /** Tertiary color-shift cycle (ms). */
    const val GRADIENT_PHASE_3_MS = 11_000

    /** Logo one-shot entrance duration (ms). */
    const val LOGO_ENTRANCE_MS = 700

    /** Logo entrance start scale (1 = full size). */
    const val LOGO_ENTRANCE_START_SCALE = 0.88f

    /** Subtle vertical float amplitude (dp). */
    const val LOGO_FLOAT_AMPLITUDE_DP = 4f

    /** Logo float cycle (ms). */
    const val LOGO_FLOAT_MS = 3_200

    /** Gentle breath scale range. */
    const val LOGO_BREATH_MIN = 0.985f
    const val LOGO_BREATH_MAX = 1.015f
    const val LOGO_BREATH_MS = 2_600

    /** Soft glow pulse behind the logo. */
    const val LOGO_GLOW_MIN_ALPHA = 0.22f
    const val LOGO_GLOW_MAX_ALPHA = 0.48f
    const val LOGO_GLOW_MS = 2_000

    /** Fade-out before navigating to home/login (ms). */
    const val EXIT_FADE_MS = 450

    /** Tagline fade-in delay after logo starts (ms). */
    const val TAGLINE_DELAY_MS = 280
    const val TAGLINE_FADE_MS = 500
}
