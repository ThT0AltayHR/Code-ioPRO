# ── Attributes for stack traces ───────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── AndroidX Lifecycle (prevent ReportFragment crash) ─────────────────────────
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# ── AndroidX Biometric ────────────────────────────────────────────────────────
-keep class androidx.biometric.** { *; }
-keep interface androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ── AndroidX Startup (InitializationProvider) ────────────────────────────────
-keep class androidx.startup.** { *; }
-keep interface androidx.startup.** { *; }
-dontwarn androidx.startup.**

# ── AndroidX Fragment / AppCompat ─────────────────────────────────────────────
-keep class androidx.fragment.app.** { *; }
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.fragment.**
-dontwarn androidx.appcompat.**

# ── WebView JavaScript Interface ──────────────────────────────────────────────
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# Keep all JS bridge classes used in the app
-keep class com.codeioPRO.app.ChatFragment$** { *; }
-keep class com.codeioPRO.app.SecretsFragment$** { *; }
-keep class com.codeioPRO.app.EditorActivity$** { *; }
-keep class com.codeioPRO.app.FilesFragment$** { *; }
-keep class com.codeioPRO.app.ShellFragment$** { *; }
-keep class com.codeioPRO.app.SubAgentFragment$** { *; }
-keep class com.codeioPRO.app.AiMarketFragment$** { *; }
-keep class com.codeioPRO.app.SettingsBridge { *; }

# ── Android Keystore / Crypto (SecretsManager) ────────────────────────────────
-keep class javax.crypto.** { *; }
-dontwarn javax.crypto.**

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# ── RecyclerView / ConstraintLayout ───────────────────────────────────────────
-dontwarn androidx.recyclerview.**
-dontwarn androidx.constraintlayout.**

# ── Material Components ───────────────────────────────────────────────────────
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ── App classes — keep all public entry points ────────────────────────────────
-keep class com.codeioPRO.app.** { *; }

# ── General Android ───────────────────────────────────────────────────────────
-dontwarn android.**
-dontwarn com.android.**
