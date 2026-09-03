# Proguard / R8 rules for Study Plan
#
# NOTE: Currently isMinifyEnabled = false in app/build.gradle.kts.
# When ready to enable R8 code shrinking and obfuscation:
#   1. In app/build.gradle.kts inside release { ... }:
#        isMinifyEnabled = true
#        isShrinkResources = true
#   2. Verify with: ./gradlew assembleRelease
#   3. Ensure Room, WorkManager, and Compose classes are preserved as defined below.

# General Kotlin & Reflection attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn java.time.**

# AndroidX Room
# Retain entities, DAOs, and database classes to ensure reflection/code generation works at runtime
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void clearAllTables();
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# AndroidX WorkManager
# Prevent obfuscation of worker constructors called via reflection by WorkerFactory
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# AndroidX Lifecycle & ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Native JSON Serialization / Backup Models
-keepclassmembers class com.umairshahab.etea.studyplan.data.local.** { *; }
-keepclassmembers class com.umairshahab.etea.studyplan.domain.** { *; }

