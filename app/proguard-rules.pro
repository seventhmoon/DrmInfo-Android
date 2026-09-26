# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\fung.lam\AppData\Local\Android\sdk\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-keepclassmembers class com.androidfung.drminfo.JwtResult {
#    public *;
#}

# Keep Configuration fields to avoid NoSuchFieldError on font weight adjustments in Compose
-keepclassmembers class android.content.res.Configuration {
    public int fontWeightAdjustment;
}

# Preserve Compose font weight adjustment helper classes
-keep class androidx.compose.ui.text.font.FontWeightAdjustmentHelper** { *; }

# Prevent R8 from stripping or mismanaging StrictMode methods used by Play Services
-keep class android.os.StrictMode$VmPolicy$Builder {
    public *;
}

# WorkManager keep rules (brought in transitively by Google Mobile Ads SDK)
# Required for R8 Full Mode reflection instantiation in androidx.startup.InitializationProvider
-dontwarn androidx.room.**
-dontwarn androidx.work.**
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>();
    *;
}

-keep interface androidx.work.** { *; }

