package com.cascadestreamer.app.managers

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

enum class CompressionLevel {
    OFF, LOW, MEDIUM, HIGH
}

data class AudioPreferencesData(
    val drcEnabled: Boolean = false,
    val compressionLevel: CompressionLevel = CompressionLevel.OFF,
    val volumeNormalization: Boolean = false
)

class AudioPreferences {
    private val _preferences = mutableStateOf(AudioPreferencesData())
    val preferences: State<AudioPreferencesData> = _preferences
    
    fun setDrcEnabled(enabled: Boolean) {
        _preferences.value = _preferences.value.copy(drcEnabled = enabled)
    }
    
    fun setCompressionLevel(level: CompressionLevel) {
        _preferences.value = _preferences.value.copy(compressionLevel = level)
    }
    
    fun setVolumeNormalization(enabled: Boolean) {
        _preferences.value = _preferences.value.copy(volumeNormalization = enabled)
    }
    
    fun getCompressionFactor(): Float {
        return when (_preferences.value.compressionLevel) {
            CompressionLevel.OFF -> 1.0f
            CompressionLevel.LOW -> 0.7f
            CompressionLevel.MEDIUM -> 0.5f
            CompressionLevel.HIGH -> 0.3f
        }
    }
}
