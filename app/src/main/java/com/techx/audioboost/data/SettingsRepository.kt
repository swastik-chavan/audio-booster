package com.techx.audioboost.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.techx.audioboost.model.AppProfile
import com.techx.audioboost.model.AudioPreset
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "audio_boost_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val BOOST_PERCENT = intPreferencesKey("boost_percent")
        val IS_ULTRA_ENABLED = booleanPreferencesKey("is_ultra_enabled")
        val ULTRA_BOOST_PERCENT = intPreferencesKey("ultra_boost_percent")
        val HAS_SHOWN_ULTRA_WARNING = booleanPreferencesKey("has_shown_ultra_warning")

        val IS_EQ_ENABLED = booleanPreferencesKey("is_eq_enabled")
        val EQ_BAND_LEVELS = stringPreferencesKey("eq_band_levels")

        val IS_BASS_BOOST_ENABLED = booleanPreferencesKey("is_bass_boost_enabled")
        val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")

        val IS_VIRTUALIZER_ENABLED = booleanPreferencesKey("is_virtualizer_enabled")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")

        val IS_LIMITER_ENABLED = booleanPreferencesKey("is_limiter_enabled")
        val SAFETY_LIMIT_ENABLED = booleanPreferencesKey("safety_limit_enabled")

        val ACTIVE_PRESET_ID = stringPreferencesKey("active_preset_id")
        val CUSTOM_PRESETS_JSON = stringPreferencesKey("custom_presets_json")

        val APP_THEME = stringPreferencesKey("app_theme")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val AUTO_OFF_MINUTES = intPreferencesKey("auto_off_minutes")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")

        val APP_PROFILES_ENABLED = booleanPreferencesKey("app_profiles_enabled")
        val APP_PROFILES_JSON = stringPreferencesKey("app_profiles_json")
    }

    private val dataStore = context.dataStore

    private val safePreferences: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val boostPercent: Flow<Int> = safePreferences.map { it[PreferencesKeys.BOOST_PERCENT] ?: 0 }
    val isUltraEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.IS_ULTRA_ENABLED] ?: false }
    val ultraBoostPercent: Flow<Int> = safePreferences.map { it[PreferencesKeys.ULTRA_BOOST_PERCENT] ?: 0 }
    val hasShownUltraWarning: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.HAS_SHOWN_ULTRA_WARNING] ?: false }

    val isEqEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.IS_EQ_ENABLED] ?: true }
    val eqBandLevels: Flow<List<Int>> = safePreferences.map { prefs ->
        val serialized = prefs[PreferencesKeys.EQ_BAND_LEVELS] ?: "0,0,0,0,0"
        deserializeBandLevels(serialized)
    }

    val isBassBoostEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.IS_BASS_BOOST_ENABLED] ?: false }
    val bassBoostStrength: Flow<Int> = safePreferences.map { it[PreferencesKeys.BASS_BOOST_STRENGTH] ?: 0 }

    val isVirtualizerEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.IS_VIRTUALIZER_ENABLED] ?: false }
    val virtualizerStrength: Flow<Int> = safePreferences.map { it[PreferencesKeys.VIRTUALIZER_STRENGTH] ?: 0 }

    val isLimiterEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.IS_LIMITER_ENABLED] ?: true }
    val safetyLimitEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.SAFETY_LIMIT_ENABLED] ?: true }

    val activePresetId: Flow<String> = safePreferences.map { it[PreferencesKeys.ACTIVE_PRESET_ID] ?: AudioPreset.FLAT.id }
    val customPresets: Flow<List<AudioPreset>> = safePreferences.map { prefs ->
        val json = prefs[PreferencesKeys.CUSTOM_PRESETS_JSON] ?: "[]"
        deserializeCustomPresets(json)
    }

    val appTheme: Flow<String> = safePreferences.map { it[PreferencesKeys.APP_THEME] ?: "audio_booster" }
    val hapticsEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.HAPTICS_ENABLED] ?: true }
    val autoOffMinutes: Flow<Int> = safePreferences.map { it[PreferencesKeys.AUTO_OFF_MINUTES] ?: 0 }
    val notificationEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.NOTIFICATION_ENABLED] ?: true }

    val appProfilesEnabled: Flow<Boolean> = safePreferences.map { it[PreferencesKeys.APP_PROFILES_ENABLED] ?: false }
    val appProfiles: Flow<List<AppProfile>> = safePreferences.map { prefs ->
        val json = prefs[PreferencesKeys.APP_PROFILES_JSON] ?: "[]"
        deserializeAppProfiles(json)
    }

    suspend fun saveBoost(boost: Int, isUltra: Boolean, ultraPercent: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.BOOST_PERCENT] = boost
            prefs[PreferencesKeys.IS_ULTRA_ENABLED] = isUltra
            prefs[PreferencesKeys.ULTRA_BOOST_PERCENT] = ultraPercent
        }
    }

    suspend fun setHasShownUltraWarning(shown: Boolean) {
        dataStore.edit { it[PreferencesKeys.HAS_SHOWN_ULTRA_WARNING] = shown }
    }

    suspend fun saveEqualizer(enabled: Boolean, bands: List<Int>) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_EQ_ENABLED] = enabled
            prefs[PreferencesKeys.EQ_BAND_LEVELS] = serializeBandLevels(bands)
        }
    }

    suspend fun saveBassBoost(enabled: Boolean, strength: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_BASS_BOOST_ENABLED] = enabled
            prefs[PreferencesKeys.BASS_BOOST_STRENGTH] = strength
        }
    }

    suspend fun saveVirtualizer(enabled: Boolean, strength: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_VIRTUALIZER_ENABLED] = enabled
            prefs[PreferencesKeys.VIRTUALIZER_STRENGTH] = strength
        }
    }

    suspend fun saveLimiterSettings(limiterEnabled: Boolean, safetyLimitEnabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_LIMITER_ENABLED] = limiterEnabled
            prefs[PreferencesKeys.SAFETY_LIMIT_ENABLED] = safetyLimitEnabled
        }
    }

    suspend fun setActivePresetId(presetId: String) {
        dataStore.edit { it[PreferencesKeys.ACTIVE_PRESET_ID] = presetId }
    }

    suspend fun saveCustomPreset(preset: AudioPreset, currentPresets: List<AudioPreset>) {
        val updated = currentPresets.filter { it.id != preset.id } + preset
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_PRESETS_JSON] = serializeCustomPresets(updated)
            prefs[PreferencesKeys.ACTIVE_PRESET_ID] = preset.id
        }
    }

    suspend fun deleteCustomPreset(presetId: String, currentPresets: List<AudioPreset>) {
        val updated = currentPresets.filter { it.id != presetId }
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_PRESETS_JSON] = serializeCustomPresets(updated)
            if (prefs[PreferencesKeys.ACTIVE_PRESET_ID] == presetId) {
                prefs[PreferencesKeys.ACTIVE_PRESET_ID] = AudioPreset.FLAT.id
            }
        }
    }

    suspend fun setAppTheme(theme: String) {
        dataStore.edit { it[PreferencesKeys.APP_THEME] = theme }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setAutoOffMinutes(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.AUTO_OFF_MINUTES] = minutes }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setAppProfilesEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.APP_PROFILES_ENABLED] = enabled }
    }

    suspend fun saveAppProfile(profile: AppProfile, currentProfiles: List<AppProfile>) {
        val updated = currentProfiles.filter { it.packageName != profile.packageName } + profile
        dataStore.edit { it[PreferencesKeys.APP_PROFILES_JSON] = serializeAppProfiles(updated) }
    }

    suspend fun deleteAppProfile(packageName: String, currentProfiles: List<AppProfile>) {
        val updated = currentProfiles.filter { it.packageName != packageName }
        dataStore.edit { it[PreferencesKeys.APP_PROFILES_JSON] = serializeAppProfiles(updated) }
    }

    suspend fun resetAllSettings() {
        dataStore.edit { it.clear() }
    }

    private fun serializeBandLevels(levels: List<Int>): String {
        return levels.joinToString(",")
    }

    private fun deserializeBandLevels(serialized: String): List<Int> {
        return try {
            serialized.split(",").map { it.trim().toInt() }
        } catch (t: Throwable) {
            listOf(0, 0, 0, 0, 0)
        }
    }

    private fun serializeCustomPresets(presets: List<AudioPreset>): String {
        val array = JSONArray()
        for (p in presets) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("boostPercent", p.boostPercent)
                put("isUltraEnabled", p.isUltraEnabled)
                put("ultraBoostPercent", p.ultraBoostPercent)
                put("bandLevels", JSONArray(p.bandLevels))
                put("bassBoostEnabled", p.bassBoostEnabled)
                put("bassBoostStrength", p.bassBoostStrength)
                put("virtualizerEnabled", p.virtualizerEnabled)
                put("virtualizerStrength", p.virtualizerStrength)
                put("limiterEnabled", p.limiterEnabled)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeCustomPresets(json: String): List<AudioPreset> {
        val list = mutableListOf<AudioPreset>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val bandArray = obj.optJSONArray("bandLevels")
                val bands = mutableListOf<Int>()
                if (bandArray != null) {
                    for (b in 0 until bandArray.length()) {
                        bands.add(bandArray.getInt(b))
                    }
                }
                list.add(
                    AudioPreset(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        isCustom = true,
                        boostPercent = obj.optInt("boostPercent", 0),
                        isUltraEnabled = obj.optBoolean("isUltraEnabled", false),
                        ultraBoostPercent = obj.optInt("ultraBoostPercent", 0),
                        bandLevels = if (bands.isNotEmpty()) bands else listOf(0, 0, 0, 0, 0),
                        bassBoostEnabled = obj.optBoolean("bassBoostEnabled", false),
                        bassBoostStrength = obj.optInt("bassBoostStrength", 0),
                        virtualizerEnabled = obj.optBoolean("virtualizerEnabled", false),
                        virtualizerStrength = obj.optInt("virtualizerStrength", 0),
                        limiterEnabled = obj.optBoolean("limiterEnabled", true)
                    )
                )
            }
        } catch (t: Throwable) {
        }
        return list
    }

    private fun serializeAppProfiles(profiles: List<AppProfile>): String {
        val array = JSONArray()
        for (p in profiles) {
            val obj = JSONObject().apply {
                put("packageName", p.packageName)
                put("appName", p.appName)
                put("presetId", p.presetId)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeAppProfiles(json: String): List<AppProfile> {
        val list = mutableListOf<AppProfile>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AppProfile(
                        packageName = obj.getString("packageName"),
                        appName = obj.getString("appName"),
                        presetId = obj.getString("presetId")
                    )
                )
            }
        } catch (t: Throwable) {
        }
        return list
    }
}
