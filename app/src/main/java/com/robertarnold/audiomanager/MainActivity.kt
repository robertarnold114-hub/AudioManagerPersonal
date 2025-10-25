package com.robertarnold.audiomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
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
            .sortedBy { it.loadLabel(pm).toString() }
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null } // only user apps
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
    val key = booleanPreferencesKey("toggle_${appName.hashCode()}")
    val dataStore = ContextDataStore

    var toggleState by remember {
        mutableStateOf(
            runBlocking {
                context.dataStore.data.first()[key] ?: false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = appName, style = MaterialTheme.typography.headlineSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Use Custom Sound:")
            Switch(
                checked = toggleState,
                onCheckedChange = {
                    toggleState = it
                    runBlocking {
                        context.dataStore.edit { prefs ->
                            prefs[key] = it
                        }
                    }
                }
            )
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
