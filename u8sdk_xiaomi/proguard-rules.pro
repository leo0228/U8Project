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
-keepattributes InnerClasses,Signature,Exceptions,Deprecated,*Annotation*
-dontwarn android.**
-dontwarn com.google.**
-keep class android.** {*;}
-keep class com.google.** {*;}
-keep class com.android.** {*;}
-dontwarn org.apache.**
-keep class org.apache.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.** {*;}
-keep public class android.arch.core.internal.FastSafeIterableMap
-keep public class android.arch.core.util.Function
-keep public class android.arch.lifecycle.Lifecycle
-keep public class android.arch.lifecycle.Observer
-keep public class android.arch.lifecycle.ReportFragment
-keep public class android.arch.lifecycle.ViewModel
-keep public class android.support.v4.app.Fragment
-keep public class android.support.annotation.AnimatorRes
-keep public class android.support.v4.app.ActivityCompat
-keep public class android.support.design.widget.CoordinatorLayout
-keep public class android.support.v4.app.AppLaunchChecker
-keep public class android.support.v4.app.BackStackState
#-libraryjars libs/alipaySdk.jar
-dontwarn com.alipay.**
-keep class com.alipay.** {*;}
-keep class com.ut.device.** {*;}
-keep class com.ta.utdid2.** {*;}
#-libraryjars libs/eventbus-3.jar
-keep class org.greenrobot.eventbus.** { *; }
-keep class de.greenrobot.event.** { *; }
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#-libraryjars libs/wechat.jar
-keep class com.tencent.** {*;}
#-libraryjars libs/glide.jar
-keep class com.bumptech.glide.** {*;}
-dontwarn com.xiaomi.**
-keep class com.xiaomi.** {*;}
-keep class com.mi.** {*;}
-keep class com.wali.** {*;}
-keep class cn.com.wali.** {*;}
-keep class miui.net.**{*;}
-keep class org.xiaomi.** {*;}
#保留位于View类中的get和set⽅法
-keepclassmembers public class * extends android.view.View{ void set*(***); *** get*(); }
#保留在Activity中以View为参数的⽅法不变
-keepclassmembers class * extends android.app.Activity{ public void *(android.view.View); }
#保留实现了Parcelable的类名不变，
-keep class * implements android.os.Parcelable{ public static final android.os.Parcelable$Creator *; }
#保留R$*类中静态成员的变量名
-keep class **.R$* {*;}
-dontwarn android.support.**
-keep class **.R$styleable{*;}

# mimo SDK
-keep class com.miui.zeus.mimo.sdk.* {
    *;
}
-keep class com.miui.analytics.** { *; }
-keep class com.xiaomi.analytics.* { public protected *; }
-keep class * extends android.os.IInterface{
    *;
}

# gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}
