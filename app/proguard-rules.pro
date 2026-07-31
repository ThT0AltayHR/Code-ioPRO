# ════════════════════════════════════════════════════════════════════════════
#  Code-ioPRO — Kapsamlı ProGuard / R8 Kuralları  (Build 15)
#  NoClassDefFoundError çöküşünü önleyen kapsamlı kurallar
# ════════════════════════════════════════════════════════════════════════════

# Yığın izlerinde satır numaraları görünsün
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Genel – tüm annotation'lar, signature, exception'lar korunsun
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ── AndroidX Startup — KRİTİK (crash başlangıç noktası) ─────────────────
# InitializationProvider.onCreate() burada başlar;
# bu paketteki her sınıf korunmalıdır.
-keep class androidx.startup.** { *; }
-keep interface androidx.startup.** { *; }
-keepclassmembers class * implements androidx.startup.Initializer {
    <init>(...);
    public java.util.List dependencies();
    public java.lang.Object create(android.content.Context);
}

# ── AndroidX Lifecycle — KRİTİK ──────────────────────────────────────────
# ProcessLifecycleOwner Kotlin lambda'ları ($initializationListener$1 vb.)
# R8 tarafından silinebilir; $ içeren iç sınıflar açıkça korunmalıdır.
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }
-keep class androidx.lifecycle.ProcessLifecycleOwner { *; }
-keep class androidx.lifecycle.ProcessLifecycleOwner$* { *; }
-keep class androidx.lifecycle.ReportFragment { *; }
-keep class androidx.lifecycle.ReportFragment$* { *; }
-keep class androidx.lifecycle.LifecycleRegistry { *; }
-keep class androidx.lifecycle.LifecycleRegistry$* { *; }
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <methods>;
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * implements androidx.lifecycle.LifecycleOwner {
    androidx.lifecycle.Lifecycle getLifecycle();
}

# ── AndroidX AppCompat & Activity ────────────────────────────────────────
-keep class androidx.appcompat.** { *; }
-keep interface androidx.appcompat.** { *; }
-keep class androidx.activity.** { *; }
-keep interface androidx.activity.** { *; }

# ── AndroidX Core ─────────────────────────────────────────────────────────
-keep class androidx.core.** { *; }
-keep interface androidx.core.** { *; }

# ── AndroidX Biometric — KRİTİK ──────────────────────────────────────────
-keep class androidx.biometric.** { *; }
-keep interface androidx.biometric.** { *; }
-keepclassmembers class androidx.biometric.BiometricPrompt { *; }
-keepclassmembers class androidx.biometric.BiometricManager { *; }

# ── AndroidX Fragment ─────────────────────────────────────────────────────
-keep class androidx.fragment.app.** { *; }
-keep interface androidx.fragment.app.** { *; }
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    <init>(...);
    public void *(...);
}

# ── AndroidX Room ─────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# ── WebView JavaScript Arayüzleri — KRİTİK ───────────────────────────────
# @JavascriptInterface ile işaretlenen tüm metotlar korunmalı
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.codeioPRO.app.ChatFragment { *; }
-keepclassmembers class com.codeioPRO.app.ChatFragment$* { *; }
-keepclassmembers class com.codeioPRO.app.SecretsFragment$SecretsBridge { *; }
-keepclassmembers class com.codeioPRO.app.ShellFragment$TerminalBridge { *; }
-keepclassmembers class com.codeioPRO.app.SettingsBridge { *; }
-keepclassmembers class com.codeioPRO.app.FilesFragment$* { *; }
-keepclassmembers class com.codeioPRO.app.SubAgentFragment$* { *; }
-keepclassmembers class com.codeioPRO.app.AiMarketFragment$* { *; }
-keepclassmembers class com.codeioPRO.app.EditorActivity$* { *; }

# ── Uygulama sınıflarını koru ─────────────────────────────────────────────
-keep class com.codeioPRO.app.** { *; }
-keep interface com.codeioPRO.app.** { *; }

# ── Android Keystore / Crypto ─────────────────────────────────────────────
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class android.security.keystore.** { *; }

# ── Serileştirme ──────────────────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── Parcelable ────────────────────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ── Material Components ───────────────────────────────────────────────────
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ── ConstraintLayout ──────────────────────────────────────────────────────
-keep class androidx.constraintlayout.** { *; }

# ── RecyclerView ──────────────────────────────────────────────────────────
-keep class androidx.recyclerview.** { *; }

# ── Enums ─────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Kotlin (dolaylı bağımlılık) ───────────────────────────────────────────
-dontwarn kotlin.**
-dontnote kotlin.**
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-keepclassmembers class **$WhenMappings { *; }
-keepclassmembers class kotlin.Metadata { *; }

# ── OkHttp / Retrofit ────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn okio.**

# ── JSON ──────────────────────────────────────────────────────────────────
-keep class org.json.** { *; }

# ── Genel uyarıları bastır ────────────────────────────────────────────────
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
-dontwarn android.content.res.**
