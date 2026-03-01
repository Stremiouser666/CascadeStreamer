package com.cascadestreamer.app.managers

data class StreamInfo(
    val url: String,
    val quality: String,
    val format: String
)

class YtDlpManager {
    
    fun extractStreamUrl(videoUrl: String): StreamInfo {
        val formatString = "best[acodec!=none][vcodec!=none]/bestvideo+bestaudio"
        
        return StreamInfo(
            url = videoUrl,
            quality = "auto",
            format = formatString
        )
    }
    
    fun getAvailableQualities(videoUrl: String): List<String> {
        return listOf("1080p", "720p", "480p", "360p", "240p")
    }
    
    fun getStreamUrlForQuality(videoUrl: String, quality: String): String {
        val formatString = "best[acodec!=none][vcodec!=none]/bestvideo+bestaudio"
        return videoUrl
    }
}
