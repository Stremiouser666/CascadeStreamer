# Retrofit
-keep interface com.squareup.retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# GSON
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.gson.** { *; }
-keepclassmembers enum * { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# ExoPlayer
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }

# Hilt
-keepclasseswithmembernames class * {
    @com.google.dagger.hilt.* <methods>;
}

# Room
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }

# Keep View constructors for Fragments
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
