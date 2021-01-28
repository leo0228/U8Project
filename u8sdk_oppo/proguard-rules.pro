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

#-keep class com.bytedance.sdk.openadsdk.** { *; }
#-keep class com.androidquery.callback.** {*;}
#-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
#-keep class com.ss.sys.ces.* {*;}

-keep class com.nearme.** { *; }

-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.* {*;}
-keep class com.qq.e.** {
      public protected *;
}
-keep class android.support.v4.**{
      public *;
}
-keep class android.support.v7.**{
      public *;
}
-keep class com.opos.** { *;}
-keep class com.cdo.oaps.ad.**{ *; }
-keepattributes Exceptions,InnerClasses
-keep class com.nearme.instant.router.Instant{public <fields>;public <methods>;}
-keep class com.nearme.instant.router.Instant$*{public <methods>;}
-keep class com.nearme.instant.router.callback.Callback{public <methods>;}
-keep class com.nearme.instant.router.callback.Callback$*{public <fields>;public <methods>;}
-keep class com.nearme.instant.router.ui.**{*;}
-keep class com.nearme.instant.patchtool.**{*;}
-keep class com.heytap.instant.upgrade.**{*;}