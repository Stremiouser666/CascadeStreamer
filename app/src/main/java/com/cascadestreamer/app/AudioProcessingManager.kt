package com.cascadestreamer.app

import androidx.media3.exoplayer.ExoPlayer

class AudioProcessingManager(
    private val audioPreferences: AudioPreferences
) {
    
    fun applyAudioProcessing(player: ExoPlayer) {
        val prefs = audioPreferences.preferences.value
        
        if (prefs.drcEnabled) {
            applyDynamicRangeCompression(player, prefs.compressionLevel)
        }
        
        if (prefs.volumeNormalization) {
            applyVolumeNormalization(player)
        }
    }
    
    private fun applyDynamicRangeCompression(
        player: ExoPlayer,
        level: CompressionLevel
    ) {
        val compressionFactor = when (level) {
            CompressionLevel.OFF -> 1.0f
            CompressionLevel.LOW -> 0.7f
            CompressionLevel.MEDIUM -> 0.5f
            CompressionLevel.HIGH -> 0.3f
        }
        
        player.volume = player.volume * compressionFactor
    }
    
    private fun applyVolumeNormalization(player: ExoPlayer) {
        player.volume = 1.0f
    }
}
