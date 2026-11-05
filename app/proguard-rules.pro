# ProGuard/R8 rules for Vital.
#
# Debug builds run without minification, so these rules only matter for release.
# R8 already handles the AGP defaults and the consumer rules shipped by AndroidX,
# Compose, Hilt, Room, OkHttp, Coil and ML Kit. The rules below cover the places
# where the app itself is reached by reflection or by generated serializers.

# Keep annotations and metadata that reflective libraries (Gson, kotlinx.serialization)
# read at runtime.
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# --- Gson -------------------------------------------------------------------
# These data classes are populated from JSON by field name via Gson reflection,
# so their names and fields must survive shrinking and obfuscation.
-keep class com.vital.ai.GeminiClient$FoodAnalysisResult { *; }
-keep class com.vital.ai.GeminiClient$CoachResponse { *; }
-keep class com.vital.ai.OpenRouterClient$FoodAnalysisResult { *; }
-keep class com.vital.data.network.OpenFoodFactsClient$** { *; }

# Gson internals that use reflection; quiet the usual R8 notes.
-dontwarn sun.misc.**
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# --- WorkManager ------------------------------------------------------------
# WorkManager restores previously scheduled work by class name, so worker classes
# and their (Context, WorkerParameters) constructors must be kept.
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- kotlinx.serialization --------------------------------------------------
# Generated serializers are referenced by name; keep them and the Companion
# members that expose them. Mirrors the documented kotlinx.serialization rules.
-keep,includedescriptorclasses class com.vital.**$$serializer { *; }
-keepclassmembers class com.vital.** {
    *** Companion;
}
-keepclasseswithmembers class com.vital.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Kotlin metadata (safety net for any remaining reflection) ---------------
-keep class kotlin.Metadata { *; }
