# Model data dipakai Gson lewat reflection, jadi jangan dihapus/diobfuscate.
-keep class com.example.habisin.data.model.** { *; }
-keep class com.example.habisin.data.api.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
