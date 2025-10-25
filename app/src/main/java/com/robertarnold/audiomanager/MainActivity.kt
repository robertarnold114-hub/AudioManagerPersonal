package com.robertarnold.audiomanager

import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val ContextDataStore by preferencesDataStore("audio_manager_prefs")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Dashboard()
                }
            }
        }
    }
}

@Composable
fun Dashboard() {
    var selectedApp by remember { mutableStateOf<String?>(null) }

    if (selectedApp == null) {
        AppListScreen(onAppSelected = { selectedApp = it })
    } else {
        AppDetailScreen(appName = selectedApp!!) {
            selectedApp = null
        }
    }
}

@Composable
fun AppListScreen(onAppSelected: (String) -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val apps = remember {
        pm.getInstalledApplications(0)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { it.loadLabel(pm).toString() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Installed Apps",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(apps) { app ->
                val label = app.loadLabel(pm).toString()
                Text(
                    text = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppSelected(label) }
                        .padding(12.dp)
                )
                Divider()
            }
        }
    }
}

@Composable
fun AppDetailScreen(appName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val dataStore = ContextDataStore
    val scope = rememberCoroutineScope()

    val keyToggle = booleanPreferencesKey("toggle_${appName.hashCode()}")
    val keyVolume = intPreferencesKey("volume_${appName.hashCode()}")
    val keySound = stringPreferencesKey("sound_${appName.hashCode()}")

    var toggleState by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableStateOf(5f) }
    var selectedSound by remember { mutableStateOf("Default") }

    // Load saved settings
    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        toggleState = prefs[keyToggle] ?: false
        volumeLevel = prefs[keyVolume]?.toFloat() ?: 5f
        selectedSound = prefs[keySound] ?: "Default"
    }

    // Launcher for picking a custom ringtone
    val soundPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri: Uri? = result.data?.getParcelableExtra(Intent.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
            selectedSound = uri.toString()
            scope.launch {
                context.dataStore.edit { it[keySound] = uri.toString() }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(appName, style = MaterialTheme.typography.headlineSmall)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Custom Sound:")
            Switch(
                checked = toggleState,
                onCheckedChange = {
                    toggleState = it
                    scope.launch { context.dataStore.edit { prefs -> prefs[keyToggle] = it } }
                }
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Volume: ${volumeLevel.toInt()}", modifier = Modifier.align(Alignment.CenterHorizontally))
            Slider(
                value = volumeLevel,
                onValueChange = {
                    volumeLevel = it
                    scope.launch { context.dataStore.edit { prefs -> prefs[keyVolume] = it.toInt() } }
                },
                valueRange = 0f..15f
            )
        }

        Button(
            onClick = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound")
                }
                soundPicker.launch(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choose Notification Sound")
        }

        Text(
            text = "Current Sound: ${if (selectedSound == "Default") "Default System" else selectedSound}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
