package com.example.notifybank
import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class BankNotifyService : NotificationListenerService() {
    private lateinit var textToSpeech: TextToSpeech
    private var mediaPlayer: MediaPlayer? = null
    private var activeBank: String? = null
    private var soundVolume: Float = 0.5f

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences("bank_preferences", Context.MODE_PRIVATE)
        activeBank = sharedPreferences.getString("active_bank", null)
        soundVolume = sharedPreferences.getFloat("sound_volume", 0.5f)
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale("vi", "VN"))
            }
        }

    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val prefs = getSharedPreferences("bank_preferences", Context.MODE_PRIVATE)

        // Check all enabled banks
        checkACBNotification(sbn, prefs)
        checkOCBNotification(sbn, prefs)
        checkTechcombankNotification(sbn, prefs)
        checkVietinbankNotification(sbn, prefs)
    }

    private fun checkACBNotification(sbn: StatusBarNotification?, prefs: SharedPreferences) {
        if (!prefs.getBoolean("ACB_enabled", true)) return

        val notification = sbn?.notification
        val extras = notification?.extras
        val title = extras?.getString(Notification.EXTRA_TITLE)
        val text = extras?.getString(Notification.EXTRA_TEXT)

        if (title?.contains("Thong bao thay doi so du tai khoan") == true) {
            val amount = extractAmountACB(text ?: "")
            if (amount != "Amount not found") {
                playSound()
                readAmountACB(amount.replace(",", ""))
            }
        }
    }

    private fun checkOCBNotification(sbn: StatusBarNotification?, prefs: SharedPreferences) {
        if (!prefs.getBoolean("OCB_enabled", true)) return

        val notification = sbn?.notification
        val extras = notification?.extras
        val title = extras?.getString(Notification.EXTRA_TITLE)
        val text = extras?.getString(Notification.EXTRA_TEXT)

        if (title?.contains("Thông báo biến động số dư") == true) {
            val amount = extractAmountOCB(text ?: "")
            if (amount != "Amount not found") {
                playSound()
                readAmountOCB(amount.replace("+", "").replace(",", ""))
            }
        }
    }

    private fun checkTechcombankNotification(sbn: StatusBarNotification?, prefs: SharedPreferences) {
        if (!prefs.getBoolean("Techcombank_enabled", true)) return

        val notification = sbn?.notification
        val extras = notification?.extras
        val title = extras?.getString(Notification.EXTRA_TITLE)
        val regex = Regex("\\+ VND (\\d{1,3}(?:,\\d{3})*)")
        val match = regex.find(title?: "")
        val amount = (match?.groups?.get(1)?.value) ?: "Amount not found"

        if (amount != "Amount not found") {
            playSound()
            readAmountTech(amount.replace("+", "").replace(",", ""))
        }
    }

    private fun checkVietinbankNotification(sbn: StatusBarNotification?, prefs: SharedPreferences) {
        if (!prefs.getBoolean("Vietinbank_enabled", true)) return

        val notification = sbn?.notification
        val extras = notification?.extras
        val title = extras?.getString(Notification.EXTRA_TITLE)
        val text = extras?.getString(Notification.EXTRA_TEXT)

        if (title?.contains("Biến động số dư") == true) {
            val amount = extractAmountViettin(text ?: "")
            if (amount != "Amount not found") {
                playSound()
                readAmountVietin(amount.replace("+", "").replace(",", ""))
            }
        }
    }

    private fun extractAmountACB(text: String): String {
        val regex = Regex("TK \\d+\\(VND\\) \\+ ([\\d,]+)")
        val match = regex.find(text)
        return (match?.groups?.get(1)?.value) ?: "Amount not found"
    }

    private fun extractAmountOCB(text: String): String {
        val regex = Regex("Số tiền: ([+]\\d{1,3}(?:,\\d{3})*)")
        val match = regex.find(text)
        return (match?.groups?.get(1)?.value) ?: "Amount not found"
    }

    private fun extractAmountViettin(text: String): String {
        val regex = Regex("Giao dịch: ([+]\\d{1,3}(?:,\\d{3})*)")
        val match = regex.find(text)
        return (match?.groups?.get(1)?.value) ?: "Amount not found"
    }

    private fun readAmountACB(amount: String) {
        val text = "Số tiền $amount đồng đã được chuyển vào tài khoản ngân hàng ACB"
        val params = Bundle()
        val adjustedVolume = soundVolume * 0.7f
        mediaPlayer?.setVolume(adjustedVolume, adjustedVolume)
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, soundVolume)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
    }

    private fun readAmountOCB(amount: String) {
        val text = "Số tiền $amount đồng đã được chuyển vào tài khoản ngân hàng OCB"
        val params = Bundle()
        val adjustedVolume = soundVolume * 0.7f
        mediaPlayer?.setVolume(adjustedVolume, adjustedVolume)
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, soundVolume)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
    }

    private fun readAmountTech(amount: String) {
        val text = "Số tiền $amount đã được chuyển vào tài khoản ngân hàng Techcombank"
        val params = Bundle()
        val adjustedVolume = soundVolume * 0.7f
        mediaPlayer?.setVolume(adjustedVolume, adjustedVolume)
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, soundVolume)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
    }

    private fun readAmountVietin(amount: String) {
        val text = "Số tiền $amount đồng đã được chuyển vào tài khoản ngân hàng Vietinbank"
        val params = Bundle()
        val adjustedVolume = soundVolume * 0.7f
        mediaPlayer?.setVolume(adjustedVolume, adjustedVolume)
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, soundVolume)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
    }

    private fun playSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.ting)
            mediaPlayer?.setVolume(soundVolume, soundVolume)
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }

            mediaPlayer?.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()

        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
}