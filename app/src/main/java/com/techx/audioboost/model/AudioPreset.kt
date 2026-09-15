package com.techx.audioboost.model

/**
 * Sound preset configuration.
 * Configures boost level, EQ bands, Bass Boost, Virtualizer, and Limiter settings.
 */
data class AudioPreset(
    val id: String,
    val name: String,
    val isCustom: Boolean = false,
    val boostPercent: Int = 0,
    val isUltraEnabled: Boolean = false,
    val ultraBoostPercent: Int = 0,
    val bandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // millibels (-1500 to +1500)
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0..1000
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0, // 0..1000
    val limiterEnabled: Boolean = true
) {
    companion object {
        val FLAT = AudioPreset(
            id = "preset_flat",
            name = "Flat",
            isCustom = false,
            boostPercent = 0,
            bandLevels = listOf(0, 0, 0, 0, 0),
            bassBoostEnabled = false,
            bassBoostStrength = 0,
            virtualizerEnabled = false,
            virtualizerStrength = 0,
            limiterEnabled = true
        )

        val MUSIC = AudioPreset(
            id = "preset_music",
            name = "Music",
            isCustom = false,
            boostPercent = 25,
            bandLevels = listOf(300, 150, 0, 200, 300),
            bassBoostEnabled = true,
            bassBoostStrength = 250,
            virtualizerEnabled = true,
            virtualizerStrength = 200,
            limiterEnabled = true
        )

        val MOVIES = AudioPreset(
            id = "preset_movies",
            name = "Movies",
            isCustom = false,
            boostPercent = 30,
            bandLevels = listOf(400, 200, 100, 250, 300),
            bassBoostEnabled = true,
            bassBoostStrength = 350,
            virtualizerEnabled = true,
            virtualizerStrength = 450,
            limiterEnabled = true
        )

        val GAMING = AudioPreset(
            id = "preset_gaming",
            name = "Gaming",
            isCustom = false,
            boostPercent = 35,
            bandLevels = listOf(500, 0, 300, 400, 200),
            bassBoostEnabled = true,
            bassBoostStrength = 300,
            virtualizerEnabled = true,
            virtualizerStrength = 400,
            limiterEnabled = true
        )

        val VOCAL_CLARITY = AudioPreset(
            id = "preset_vocal_clarity",
            name = "Vocal Clarity",
            isCustom = false,
            boostPercent = 20,
            bandLevels = listOf(-200, 0, 500, 400, 200),
            bassBoostEnabled = false,
            bassBoostStrength = 0,
            virtualizerEnabled = false,
            virtualizerStrength = 0,
            limiterEnabled = true
        )

        val BASS = AudioPreset(
            id = "preset_bass",
            name = "Bass",
            isCustom = false,
            boostPercent = 40,
            bandLevels = listOf(800, 500, -100, 100, 200),
            bassBoostEnabled = true,
            bassBoostStrength = 700,
            virtualizerEnabled = false,
            virtualizerStrength = 0,
            limiterEnabled = true
        )

        val PODCAST = AudioPreset(
            id = "preset_podcast",
            name = "Podcast",
            isCustom = false,
            boostPercent = 20,
            bandLevels = listOf(-300, 200, 500, 300, 0),
            bassBoostEnabled = false,
            bassBoostStrength = 0,
            virtualizerEnabled = false,
            virtualizerStrength = 0,
            limiterEnabled = true
        )

        val BUILT_IN_PRESETS = listOf(
            FLAT,
            MUSIC,
            MOVIES,
            GAMING,
            VOCAL_CLARITY,
            BASS,
            PODCAST
        )
    }
}
