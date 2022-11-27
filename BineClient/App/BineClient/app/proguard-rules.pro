# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes SourceFile, LineNumberTable

-keep class com.microsoft.mobile.auth.dtos.** { *; }
-keep class com.microsoft.mobile.polymer.mishtu.storage.entities.snappy.** {*;}

-keepclassmembers class * implements android.os.Parcelable {
   static ** CREATOR;
 }

 -dontwarn com.google.android.material.**
 -keep class com.google.android.material.** { *; }

 -dontwarn androidx.**
 -keep class androidx.** { *; }
 -keep interface androidx.** { *; }

 -keep class * implements android.os.Parcelable {*;
 }

 ## Android architecture components: Lifecycle
 -keepclassmembers class android.arch.** { *; }
 -keep class android.arch.** { *; }
 -dontwarn android.arch.**

 -keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, Annotation, EnclosingMethod
 # Gson
 -keep class com.google.gson.examples.android.model.** { *; }
 -keep class * implements com.google.gson.TypeAdapterFactory
 -keep class * implements com.google.gson.JsonSerializer
 -keep class * implements com.google.gson.JsonDeserializer
 # OkHttp3
 -dontwarn okhttp3.**
 # Okio
 -dontwarn okio.**
 # Retrofit 2.X
 -dontwarn javax.annotation.**


#Firebase remote config
-keep class com.firebase.** { *; }

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
    public static int e(...);
}

 -keepclassmembers enum * {
 public static **[] values();
 public static ** valueOf(java.lang.String);
 }

-keepattributes *Annotation*
-keepclassmembers class ** {
     @org.greenrobot.eventbus.Subscribe <methods>;
 }
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keep public class com.esotericsoftware.kryo.serializers.** { *; }
-keep class com.snappydb.** { *; }