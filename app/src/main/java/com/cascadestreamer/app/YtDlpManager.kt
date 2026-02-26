package com.cascadestreamer.app

data class StreamInfo(
    val url: String,
    val quality: String,
    val format: String
)

class YtDlpManager {
    
    fun extractStreamUrl(videoUrl: String): StreamInfo {
        return StreamInfo(
            url = videoUrl,
            quality = "720p",
            format = "mp4"
        )
    }
    
    fun getAvailableQualities(videoUrl: String): List<String> {
        return listOf("1080p", "720p", "480p", "360p")
    }
    
    fun getStreamUrlForQuality(videoUrl: String, quality: String): String {
        return videoUrl
    }
}
