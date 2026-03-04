package com.cascadestreamer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.cascadestreamer.app.states.AppState
import com.cascadestreamer.app.storage.LocalStorageManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val storageManager = LocalStorageManager(this)
        
        setContent {
            MaterialTheme {
                CascadeStreamerApp(
                    appState = AppState(storageManager = storageManager),
                    onExitApp = { finish() }
                )
            }
        }
    }
}
