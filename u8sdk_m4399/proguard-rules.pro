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

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

-keep class cn.m4399.operate.** {*;}
-keep class cn.m4399.recharge.** {*;}
-dontwarn cn.m4399.operate.**
-dontwarn cn.m4399.recharge.**
-keepclassmembers class cn.m4399.recharge.R$* {*;}
-keep class com.m4399.gamecenter.** {*;}

-dontwarn com.arcsoft.hpay100.**
-keep class com.arcsoft.hpay100.**{*;}
-dontskipnonpubliclibraryclassmembers
-dontwarn android.net.**
-keep class android.net.SSLCertificateSocketFactory{*;}
-keep class com.ishumei.** { *; }

-keep class cn.com.chinatelecom.account.** {*;}
-dontwarn com.unicom.xiaowo.account.shield.**
-keep class com.unicom.xiaowo.account.shield.**{*;}

# 4399广告联盟

# 不能混淆监听
-keep class com.mob4399.adunion.listener.** {*;}
-keep class com.mob4399.adunion.AdUnion* {
  public *;
}
-keep class com.mob4399.adunion.model.** {*;}
#广点通
-keep class com.qq.e.** {
    public protected *;
}

-keep class android.support.v4.**{
    public *;
}
-keep class android.support.v7.**{
    public *;
}

###
# Mintegral
###
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mintegral.** {*; }
-keep interface com.mintegral.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mintegral.**
-keep class **.R$* { public static final int mintegral*; }
-keep class com.alphab.** {*; }
-keep interface com.alphab.** {*; }

###
# Toutiao
###
-keep class com.bytedance.sdk.openadsdk.** {*;}
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.* {*;}

###
# media 4399
###
-keeppackagenames cn.m4399.admob
-keep class cn.m4399.admob.** {*;}
-keeppackagenames cn.m4399.support
-keep class cn.m4399.support.** {*;}