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
-keep class com.androidfung.drminfo.ui.mobile.** { *; }
-keep class com.androidfung.drminfo.** { *; }
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
#-keep class android.support.v7.** { *; }
#-keep interface android.support.v7.** { *; }
#-keep class android.support.v4.** { *; }
#-keep interface android.support.v4.** { *; }
-keep public class com.google.android.gms.**
#-keep class !android.support.v7.internal.view.menu.**,
-keep class android.support.** {*;}
-dontwarn android.support.v7.**
-dontwarn com.google.android.gms.**
-dontwarn android.support.**
-dontwarn com.github.**
-dontwarn com.squareup.picasso.**
-dontwarn com.etsy.android.grid.**

# for GPS
-keep class com.google.api.services.*.mode.*
-keep class com.google.api.client.**
-keepattributes Signature,RuntimeVisableAnnotations,AnnotationDefault

# for Maps
-keep class com.android.**
-keep class com.google.android.**
-keep class com.google.android.gms.**
-keep class com.google.android.gms.location.**
-keep class com.google.api.client.**
-keep class com.google.maps.android.**


-keep class android.os.Parcelable.Creator
-keep class com.google.android.gms.location.ActivityRecognitionResult
-keep class com.google.android.gms.** {*;}

-keep class com.google.android.flexbox.FlexboxLayout
-keep class com.google.android.flexbox.**
-keep class com.google.android.flexbox.** {*;}

-keep class android.content.ServiceConnection
-keep class android.content.ServiceConnection {*;}
-keep class android.os.SystemProperties
-keep class com.google.appengine.api.ThreadManager
-keep class com.google.android.gms.tagmanager.TagManagerService
-keep class sun.misc.Unsafe
-keep class com.google.protobuf.ExtensionRegistry
-keep class com.google.protobuf.Extension
-keep class libcore.io.Memory
-keep class org.robolectric.Robolectric
#-keep class android.content.**
#-keep class android.content.** {*;}
-dontwarn android.content.ServiceConnection
-dontwarn android.content.**

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn com.google.common.util.concurrent.FuturesGetChecked**
-dontwarn javax.lang.model.element.Modifier
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**

-keep class com.google.common.collect.** { *; }
-dontwarn com.google.common.collect.**

-keep class com.google.firebase.firestore.FirebaseFirestoreSettings
-keep class com.google.common.base.Objects
-keep class com.google.android.gms.internal.firebase-perf.** { *; }
-keep class com.google.android.gms.internal.measurement.** { *; }