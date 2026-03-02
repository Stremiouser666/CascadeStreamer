package com.cascadestreamer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Let Compose handle it first, then system
                if (!isEnabled) {
                    isEnabled = false
                    onBackPressed()
                }
            }
        })
        
        setContent {
            MaterialTheme {
                CascadeStreamerApp()
            }
        }
    }
}
