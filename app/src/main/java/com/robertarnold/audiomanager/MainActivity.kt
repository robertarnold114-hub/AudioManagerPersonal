package com.robertarnold.audiomanager

import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioManagerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioManagerApp() {
    val audioManager = LocalContext.current.getSystemService(AudioManager::class.java)
    var volume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text(
                    text = "Audio Manager",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Slider(
                    value = volume.toFloat(),
                    onValueChange = { newValue ->
                        volume = newValue.toInt()
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                    },
                    valueRange = 0f..maxVolume.toFloat(),
                    steps = maxVolume,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Volume: $volume / $maxVolume",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = {
                    val ringtone = RingtoneManager.getRingtone(
                        LocalContext.current,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    )
                    ringtone.play()
                }) {
                    Text("Play Test Sound")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAudioManagerApp() {
    AudioManagerApp()
}
