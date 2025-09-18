# ------------------------------
# Core: keep generics/annotations (needed for Retrofit/Gson)
# ------------------------------
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations, MethodParameters

# ------------------------------
# Firebase
# ------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ------------------------------
# Retrofit / OkHttp / Gson / Moshi / Okio
# ------------------------------
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.squareup.moshi.** { *; }
-dontwarn okhttp3.**

# Keep Retrofit annotations on interfaces (so Retrofit can reflect endpoints)
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep your Retrofit API interfaces
-keep interface com.capstone.safehito.api.** { *; }

# ------------------------------
# Models used by Gson
# ------------------------------
-keep class com.capstone.safehito.model.** { *; }
-keepclassmembers class com.capstone.safehito.model.** {
    <fields>;
    <init>(...);
}

# If you use @SerializedName anywhere, keep those fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ------------------------------
# Jetpack Compose
# ------------------------------
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ------------------------------
# Room (if used)
# ------------------------------
-keep class androidx.room.** { *; }

# ------------------------------
# Coil / Lottie / CameraX
# ------------------------------
-keep class coil.** { *; }
-keep class com.airbnb.lottie.** { *; }
-keep class androidx.camera.** { *; }

# ------------------------------
# General app
# ------------------------------
-keep class com.capstone.safehito.** { *; }

# Ignore annotation processing / javax model (compile-time only)
-dontwarn javax.lang.model.**
-dontwarn javax.annotation.**
-dontwarn javax.tools.**
-dontwarn com.squareup.javapoet.**
-dontwarn com.squareup.kotlinpoet.**
-dontwarn com.google.auto.common.**

# Ignore JDBC (not needed on Android runtime)
-dontwarn java.sql.**

# Keep generic type signatures (critical for Retrofit + Gson)
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep Retrofit API interfaces exactly
-keep interface com.capstone.safehito.api.** { *; }

# Keep all model classes used by Gson (your ForecastResponse, ForecastItem, etc.)
-keep class com.capstone.safehito.model.** { *; }
-keepclassmembers class com.capstone.safehito.model.** {
    <fields>;
    <init>(...);
}

# ---- Kotlin coroutines ----
-keep class kotlin.coroutines.** { *; }
-dontwarn kotlin.coroutines.**

# Keep suspend function signatures (Continuation parameter)
-keepclassmembers class * {
    @kotlin.coroutines.jvm.internal.DebugMetadata *;
}

# Keep runtime invisible parameter annotations & method parameters (already partly in your file)
-keepattributes MethodParameters, RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations

