package com.example.notifybank
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifybank.ui.theme.NotifyBankTheme
import java.util.Locale


class MainActivity : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, BankNotifyService::class.java)
        startService(serviceIntent)
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale("vi", "VN"))
            }
        }
        // Check if notification access is granted
        if (!isNotificationAccessGranted()) {
            showPermissionDialog()
        }

        setContent {
            NotifyBankTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BankNotifyUI()
                }
            }
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return listeners != null && listeners.contains(packageName)
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("App này cần truy cập quyền thông báo của bạn")
            .setMessage("Hãy bật quyền thông báo")
            .setPositiveButton("Tới cài đặt") { _, _ ->
                // Open the Notification Listener Settings page
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivity(intent)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    @Composable
    fun BankNotifyUI() {
        val isOCBEnabled = remember { mutableStateOf(true) }
        val isTechEnabled = remember { mutableStateOf(true) }
        val isVietinEnabled = remember { mutableStateOf(true) }
        val isACBEnabled = remember { mutableStateOf(true) }

        val context = LocalContext.current

        val soundVolume = remember { mutableFloatStateOf(0.5f) }

        LaunchedEffect(Unit) {
            val prefs = context.getSharedPreferences("bank_preferences", Context.MODE_PRIVATE)
            isOCBEnabled.value = prefs.getBoolean("OCB_enabled", true)
            isTechEnabled.value = prefs.getBoolean("Techcombank_enabled", true)
            isVietinEnabled.value = prefs.getBoolean("Vietinbank_enabled", true)
            isACBEnabled.value = prefs.getBoolean("ACB_enabled", true)

            soundVolume.floatValue = prefs.getFloat("sound_volume", 0.5f)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1F2A44),
                            Color(0xFF2C3E50)
                        ) // Dark blue gradient to black
                    )
                )
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "DING",
                style = TextStyle(
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00B0D3),
                    shadow = Shadow(
                        color = Color.Gray.copy(alpha = 0.5f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Thông báo",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFFBDC3C7),
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(2f, 2f),
                            blurRadius = 5f
                        )
                    ),
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACB",
                        color = Color.White,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isACBEnabled.value,
                        onCheckedChange = {
                            isACBEnabled.value = it
                            saveBankState(context, "ACB", it)
                        }
                    )
                }

                // Switch for enabling/disabling notifications
                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OCB",
                        color = Color.White,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isOCBEnabled.value,
                        onCheckedChange = {
                            isOCBEnabled.value = it
                            saveBankState(context, "OCB", it)
                        }
                    )
                }

                // Techcombank Switch
                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Techcombank",
                        color = Color.White,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isTechEnabled.value,
                        onCheckedChange = {
                            isTechEnabled.value = it
                            saveBankState(context, "Techcombank", it)
                        }
                    )
                }

                // Vietinbank Switch
                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vietinbank",
                        color = Color.White,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isVietinEnabled.value,
                        onCheckedChange = {
                            isVietinEnabled.value = it
                            saveBankState(context, "Vietinbank", it)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Slider(
                    value = soundVolume.floatValue ,
                    onValueChange = { volume ->
                        soundVolume.floatValue  = volume
                        setSoundVolume(context, volume)

                    },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        playTestSound(soundVolume.floatValue)
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) { Text(text = "Test", fontSize = 18.sp) }


            }
        }
    }

    private fun setSoundVolume(context: Context, volume: Float){
        val sharedPreferences =
            context.getSharedPreferences("bank_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putFloat("sound_volume", volume).apply()
    }

    private fun playTestSound(volume: Float){
        val mediaPlayer = MediaPlayer.create(this, R.raw.ting)
        val adjustedVolume = volume * 0.7f
        mediaPlayer.setVolume(adjustedVolume, adjustedVolume)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            it.release()

            val text = "Số tiền 500 nghìn đồng đã được chuyển vào tài khoản ngân hàng"
            val params = Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
        }
    }

    private fun saveBankState(context: Context, bank: String, isEnabled: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences("bank_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("${bank}_enabled", isEnabled).apply()
    }
}