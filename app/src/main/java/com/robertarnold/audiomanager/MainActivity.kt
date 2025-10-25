package com.robertarnold.audiomanager

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AudioManagerApp()
        }
    }

    @Composable
    fun AudioManagerApp() {
        var selectedSound by remember { mutableStateOf("Default notification sound") }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Notification Sound Manager",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Selected: $selectedSound",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { openRingtonePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose Notification Sound")
                }
            }
        }
    }

    private fun openRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_NOTIFICATION
            )
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                true
            )
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                true
            )
        }
        startActivity(intent)
    }
}
