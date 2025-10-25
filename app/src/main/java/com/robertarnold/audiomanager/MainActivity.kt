package com.robertarnold.audiomanager

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 🔹 Create DataStore instance (at top level)
private val Context.dataStore by preferencesDataStore("audio_manager_prefs")
private val SELECTED_RINGTONE_KEY = stringPreferencesKey("selected_ringtone")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AudioManagerApp()
        }
    }

    @Composable
    fun AudioManagerApp() {
        val context = this@MainActivity
        val scope = rememberCoroutineScope()
        var selectedSound by remember { mutableStateOf("Default notification sound") }

        // Load saved ringtone when app starts
        LaunchedEffect(Unit) {
            val prefs = context.dataStore.data.first()
            selectedSound = prefs[SELECTED_RINGTONE_KEY] ?: "Default notification sound"
        }

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
                    onClick = {
                        // Launch ringtone picker
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
                        startActivityForResult(intent, 101)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose Notification Sound")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            // Save the currently selected ringtone to DataStore
                            context.dataStore.edit { prefs ->
                                prefs[SELECTED_RINGTONE_KEY] = selectedSound
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Selection")
                }
            }
        }
    }

    // Handle ringtone picker result
    @Deprecated("Deprecated in Android 13+, use registerForActivityResult if updating later")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && data != null) {
            val uri: Uri? = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                val ringtone = RingtoneManager.getRingtone(this, uri)
                val name = ringtone.getTitle(this)
                // Save directly when selected
                lifecycleScope.launch {
                    dataStore.edit { prefs ->
                        prefs[SELECTED_RINGTONE_KEY] = name
                    }
                }
            }
        }
    }
}
