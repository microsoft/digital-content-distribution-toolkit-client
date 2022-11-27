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

-keepclassmembers enum * { *; }

-keep class  com.msr.bine_sdk.auth.LoginResponse { *; }
-keep public class com.msr.bine_sdk.models.** {*;}
-keep public class com.msr.bine_sdk.cloud.models.** {*;}

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
 # LifecycleObserver's empty constructor is considered to be unused by proguard
 -keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
     <init>(...);
 }
 # ViewModel's empty constructor is considered to be unused by proguard
 -keepclassmembers class * extends android.arch.lifecycle.ViewModel {
     <init>(...);
 }

 # keep Lifecycle State and Event enums values
 -keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
 -keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }
 # keep methods annotated with @OnLifecycleEvent even if they seem to be unused
 # (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
 -keepclassmembers class * {
     @android.arch.lifecycle.OnLifecycleEvent *;
 }

 -keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
     <init>(...);
 }

 -keep class * implements android.arch.lifecycle.LifecycleObserver {
     <init>(...);
 }
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
 -keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
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


