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

    // Function to play the selected sound at set volume
    fun playTestNotification() {
        val audioManager = context.getSystemService(AudioManager::class.java)
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        val newVolume = (maxVolume * (volumeLevel / 15f)).toInt().coerceIn(1, maxVolume)

        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newVolume, 0)

        val soundUri: Uri = if (selectedSound != "Default") {
            Uri.parse(selectedSound)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val ringtone = RingtoneManager.getRingtone(context, soundUri)
        ringtone?.play()

        // Restore volume after short delay
        scope.launch {
            kotlinx.coroutines.delay(2000)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalVolume, 0)
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

        // ✅ NEW: Test Notification button
        Button(
            onClick = { playTestNotification() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Notification Sound")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
