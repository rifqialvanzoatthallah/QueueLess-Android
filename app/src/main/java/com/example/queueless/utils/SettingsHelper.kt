package com.example.queueless.utils

import android.content.Context

object SettingsHelper {

    private const val PREF_NAME = "queueless_settings_prefs"

    // Kunci Pengaturan Admin
    private const val KEY_ADMIN_TTS_ENABLED = "admin_tts_enabled"
    private const val KEY_ADMIN_COUNTER_NAME = "admin_counter_name"
    private const val KEY_ADMIN_TTS_RATE = "admin_tts_rate"

    // Kunci Pengaturan Mahasiswa
    private const val KEY_MHS_NOTIF_VIBRATE = "mhs_notif_vibrate"
    private const val KEY_MHS_NOTIF_SOUND = "mhs_notif_sound"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isAdminTtsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ADMIN_TTS_ENABLED, true)

    fun setAdminTtsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ADMIN_TTS_ENABLED, enabled).apply()
    }

    fun getAdminCounterName(context: Context): String =
        prefs(context).getString(KEY_ADMIN_COUNTER_NAME, "").orEmpty()

    fun setAdminCounterName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_ADMIN_COUNTER_NAME, name.trim()).apply()
    }

    fun getAdminSpeechRate(context: Context): Float =
        prefs(context).getFloat(KEY_ADMIN_TTS_RATE, 0.95f)

    fun setAdminSpeechRate(context: Context, rate: Float) {
        prefs(context).edit().putFloat(KEY_ADMIN_TTS_RATE, rate).apply()
    }

    fun isMhsNotifVibrateEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MHS_NOTIF_VIBRATE, true)

    fun setMhsNotifVibrateEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MHS_NOTIF_VIBRATE, enabled).apply()
    }

    fun isMhsNotifSoundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MHS_NOTIF_SOUND, true)

    fun setMhsNotifSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MHS_NOTIF_SOUND, enabled).apply()
    }
}