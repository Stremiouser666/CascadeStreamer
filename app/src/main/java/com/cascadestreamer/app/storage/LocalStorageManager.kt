package com.cascadestreamer.app.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import java.io.File

class LocalStorageManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("cascadestreamer_prefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    private val documentsDir = File(context.filesDir, "playlists")
    
    init {
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }
    }
    
    // Audio Preferences
    fun saveAudioPreferences(audioPreferences: AudioPreferences) {
        val prefs = audioPreferences.preferences.value
        sharedPreferences.edit().apply {
            putBoolean("drc_enabled", prefs.drcEnabled)
            putString("compression_level", prefs.compressionLevel.name)
            putBoolean("volume_norm_enabled", prefs.volumeNormalization)
            apply()
        }
    }
    
    fun loadAudioPreferences(): AudioPreferencesData {
        val drcEnabled = sharedPreferences.getBoolean("drc_enabled", false)
        val compressionLevelStr = sharedPreferences.getString("compression_level", "OFF")
        val volumeNormEnabled = sharedPreferences.getBoolean("volume_norm_enabled", false)
        
        val compressionLevel = try {
            CompressionLevel.valueOf(compressionLevelStr ?: "OFF")
        } catch (e: Exception) {
            CompressionLevel.OFF
        }
        
        return AudioPreferencesData(
            drcEnabled = drcEnabled,
            compressionLevel = compressionLevel,
            volumeNormalization = volumeNormEnabled
        )
    }
    
    // Video Preferences
    fun saveVideoSettings(videoSettings: VideoSettings) {
        sharedPreferences.edit().apply {
            putFloat("brightness", videoSettings.brightness)
            putFloat("contrast", videoSettings.contrast)
            putFloat("saturation", videoSettings.saturation)
            putString("aspect_ratio", videoSettings.aspectRatio.name)
            putFloat("custom_width", videoSettings.customWidth)
            putFloat("custom_height", videoSettings.customHeight)
            apply()
        }
    }
    
    fun loadVideoSettings(): VideoSettings {
        return VideoSettings(
            brightness = sharedPreferences.getFloat("brightness", 1.0f),
            contrast = sharedPreferences.getFloat("contrast", 1.0f),
            saturation = sharedPreferences.getFloat("saturation", 1.0f),
            aspectRatio = try {
                AspectRatio.valueOf(
                    sharedPreferences.getString("aspect_ratio", "FIT") ?: "FIT"
                )
            } catch (e: Exception) {
                AspectRatio.FIT
            },
            customWidth = sharedPreferences.getFloat("custom_width", 1.0f),
            customHeight = sharedPreferences.getFloat("custom_height", 1.0f)
        )
    }
    
    // Watch History
    fun saveWatchHistory(history: List<WatchHistoryItem>) {
        try {
            val json = gson.toJson(history)
            val file = File(documentsDir, "watch_history.json")
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadWatchHistory(): List<WatchHistoryItem> {
        return try {
            val file = File(documentsDir, "watch_history.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : com.google.gson.reflect.TypeToken<List<WatchHistoryItem>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // Playlists
    fun savePlaylists(playlists: List<Playlist>) {
        try {
            val json = gson.toJson(playlists)
            val file = File(documentsDir, "playlists.json")
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadPlaylists(): List<Playlist> {
        return try {
            val file = File(documentsDir, "playlists.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : com.google.gson.reflect.TypeToken<List<Playlist>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
