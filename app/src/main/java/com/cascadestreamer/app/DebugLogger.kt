package com.cascadestreamer.app

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DebugLogger(context: Context) {
    private val debugFile = File(context.cacheDir, "cascade_debug.log")
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    
    fun log(tag: String, message: String, exception: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val logLine = "[$timestamp] $tag: $message"
        
        try {
            debugFile.appendText("$logLine\n")
            
            if (exception != null) {
                debugFile.appendText("${exception.stackTraceToString()}\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getLogPath(): String = debugFile.absolutePath
    
    fun clearLog() {
        debugFile.delete()
    }
}
