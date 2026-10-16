# Keep Line Numbers & Source Files for Clean Production Crash Reports
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep ALL com.gigverse.app Package Classes & Members
-keep class com.gigverse.app.** { *; }
-keepclassmembers class com.gigverse.app.** { *; }

# Keep ViewModels & Activity Constructors for Reflection
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
    <init>(android.app.Application);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
    <init>(android.app.Application);
}
-keep class * extends androidx.activity.ComponentActivity {
    <init>(...);
}

# Room Database & Entities
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * implements androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class com.gigverse.app.data.local.** { *; }
-keep class com.gigverse.app.data.model.** { *; }

# Moshi & JSON Data Models
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.**
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Retrofit & OkHttp
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin Coroutines & Flow
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Jetpack Compose UI
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Gemini AI Engine & Models
-keep class com.gigverse.app.ai.** { *; }
-keep class com.gigverse.app.config.** { *; }

# Firebase Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# MongoDB Driver & BSON Serialization
-dontwarn com.mongodb.**
-dontwarn org.bson.**
-dontwarn javax.naming.**
-dontwarn java.lang.management.**
-dontwarn io.netty.**
-dontwarn org.xerial.snappy.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.slf4j.**
-keep class com.mongodb.** { *; }
-keep class org.bson.** { *; }


