# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in proguard-android-optimize.txt.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve Compose font weight adjustment helper classes on API 31+
-keep class androidx.compose.ui.text.font.FontWeightAdjustmentHelper** { *; }

# Suppress transitive WorkManager / Room warnings brought in by Google Mobile Ads SDK
-dontwarn androidx.room.**
-dontwarn androidx.work.**

# Preserve Protobuf Lite message fields for reflection lookup in MessageSchema (used by Firebase Perf)
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Preserve AccessibilityEventCompat and Api34Impl delegates to prevent R8 inlining issues
-keep class androidx.core.view.accessibility.AccessibilityEventCompat** { *; }
