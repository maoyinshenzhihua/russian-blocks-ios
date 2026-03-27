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

# Basic Android ProGuard rules
-keepattributes *Annotation*
-dontwarn android.support.**
-keep class androidx.appcompat.app.** { *; }
-keep interface androidx.appcompat.app.** { *; }

# Keep all model classes if you have any
# -keep class com.example.model.** { *; }

# Keep all activity classes
-keep class * extends android.app.Activity
-keep class * extends androidx.appcompat.app.AppCompatActivity

# Keep all service classes
-keep class * extends android.app.Service

# Keep all broadcast receiver classes
-keep class * extends android.content.BroadcastReceiver

# Keep all content provider classes
-keep class * extends android.content.ContentProvider

# Keep all fragment classes
-keep class * extends android.app.Fragment
-keep class * extends androidx.fragment.app.Fragment

# Keep all view classes
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep all classes with @Keep annotation
-keep @androidx.annotation.Keep class *
-keepclassmembers @androidx.annotation.Keep class * {
    *;
}

# Keep all classes with @OnClick annotation
-keepclassmembers class * {
    @androidx.annotation.OnClick *;
}

# Keep all classes with @BindView annotation
-keepclassmembers class * {
    @butterknife.BindView *;
}

# Keep all Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep all Serializable implementations
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep all enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep all R classes
-keep class **.R {
    *;
}

# Keep all BuildConfig classes
-keep class **.BuildConfig {
    *;
}