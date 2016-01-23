# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/pierrot/Devel/Android/SDKs/android-studio/sdk/tools/proguard/proguard-android.txt
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
-keepattributes Signature, LineNumberTable

-keep class com.faendir.lightning_launcher.multitool.scripting.** {*;}
-keep class com.faendir.lightning_launcher.multitool.scriptmanager.** {*;}
-keep public class org.acra.** {*;}
-keep public interface org.acra.** {*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.faendir.lightning_launcher.scriptlib.** {*;}
