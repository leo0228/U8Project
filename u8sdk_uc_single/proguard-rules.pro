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

-dontwarn
-ignorewarnings
#-------------------------   联运 start ---------------------------------

-dontskipnonpubliclibraryclassmembers
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,LocalVariable*Table,*Annotation*,Synthetic,EnclosingMethod

-keep public class * extends cn.gundam.sdk.shell.even.SDKEventReceiver

-keep class android.**{
    <methods>;
    <fields>;
}
-keep class cn.uc.**{
    <methods>;
    <fields>;
}
-keep class cn.gundam.**{
    <methods>;
    <fields>;
}

#-------------------------   联运 end ---------------------------------