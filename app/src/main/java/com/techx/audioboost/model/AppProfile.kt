package com.techx.audioboost.model

/**
 * Data model for per-application sound profiles.
 */
data class AppProfile(
    val packageName: String,
    val appName: String,
    val presetId: String
)
