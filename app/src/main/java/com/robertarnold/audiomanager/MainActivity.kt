package com.robertarnold.audiomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

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
    var taps by remember { mutableStateOf(0) }
    var windowStart by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Audio Manager",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    val now = System.currentTimeMillis()
                    if (now - windowStart > 700L) {
                        windowStart = now
                        taps = 1
                    } else {
                        taps += 1
                    }
                    if (taps >= 3) {
                        taps = 0
                        windowStart = 0L
                    }
                })
            }
        )
        Text("Triple-tap the title to unlock Developer Tools", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { /* placeholder */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Settings (placeholder)")
        }
    }
}
