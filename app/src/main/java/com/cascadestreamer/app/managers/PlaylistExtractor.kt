package com.cascadestreamer.app.managers

import java.io.File

class PlaylistExtractor {
    
    fun extractPlaylistUrls(playlistUrl: String): List<String> {
        val urls = mutableListOf<String>()
        
        try {
            val command = arrayOf(
                "yt-dlp",
                "-j",
                "--flat-playlist",
                playlistUrl
            )
            
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            
            output.split("\n").forEach { line ->
                if (line.contains("url")) {
                    val url = parseJsonUrl(line)
                    if (url.isNotEmpty()) {
                        urls.add(url)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return urls
    }
    
    fun savePlaylistToFile(filename: String, urls: List<String>, directory: File) {
        try {
            val file = File(directory, "$filename.txt")
            file.writeText(urls.joinToString("\n"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadPlaylistFromFile(file: File): List<String> {
        return try {
            file.readLines().filter { it.isNotEmpty() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun parseJsonUrl(jsonLine: String): String {
        return try {
            val startIdx = jsonLine.indexOf("\"url\":\"") + 7
            val endIdx = jsonLine.indexOf("\"", startIdx)
            if (startIdx > 6 && endIdx > startIdx) {
                jsonLine.substring(startIdx, endIdx)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
