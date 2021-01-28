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

-dontwarn com.smwl.smsdk.**
-keep class com.smwl.smsdk.** {*;}
-dontwarn com.alipay.**
-keep class com.alipay.** {*;}
-dontwarn  com.ta.utdid2.**
-keep class com.ta.utdid2.** {*;}
-dontwarn  com.ut.device.**
-keep class com.ut.device.** {*;}
-dontwarn  com.tencent.**
-keep class com.tencent.** {*;}
-dontwarn  com.unionpay.**
-keep class com.unionpay.** {*;}
-dontwarn com.pingplusplus.**
-keep class com.pingplusplus.** {*;}
-dontwarn com.baidu.**
-keep class com.baidu.** {*;}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}