package com.example.queueless.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class VoiceAnnouncer(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val indonesianLocale = Locale.Builder()
                .setLanguage("id")
                .setRegion("ID")
                .build()

            val result = tts?.setLanguage(indonesianLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                isReady = false
                Toast.makeText(
                    appContext,
                    "Data suara Bahasa Indonesia belum terpasang di pengaturan TTS HP.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                tts?.setSpeechRate(0.92f)
                tts?.setPitch(1.0f)
                isReady = true
            }
        } else {
            isReady = false
        }
    }

    fun announceQueue(nomorAntrean: String) {
        if (!isReady || nomorAntrean.isBlank()) return

        val spokenNumber = formatQueueNumberForSpeech(nomorAntrean)
        val announcement = "Nomor antrean, $spokenNumber, silakan menuju loket pelayanan."

        tts?.speak(
            announcement,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "QUEUE_CALL_$nomorAntrean"
        )
    }

    private fun formatQueueNumberForSpeech(rawNumber: String): String {
        // Mengubah format "A-001" menjadi "A, 0 0 1" agar pelafalan setiap digit jelas
        return rawNumber
            .replace("-", ", ")
            .map { char ->
                if (char.isDigit()) "$char " else char.toString()
            }
            .joinToString("")
            .trim()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}