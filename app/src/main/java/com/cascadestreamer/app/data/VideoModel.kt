package com.cascadestreamer.app.data

data class Video(
    val id: String,
    val title: String,
    val url: String,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val thumbnail: String = ""
)

data class Playlist(
    val id: String,
    val name: String,
    val videos: List<Video> = emptyList()
)
