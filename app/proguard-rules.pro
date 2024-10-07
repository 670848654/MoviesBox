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
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-ignorewarnings

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# 指定代码的压缩级别
-optimizationpasses 5

# 不忽略库中的非public的类成员
-dontskipnonpubliclibraryclassmembers

# google推荐算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 避免混淆Annotation、内部类、泛型、匿名类
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 保持四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

# 保持support下的所有类及其内部类
-keep class android.support.** {*;}

# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**

# 保持自定义控件
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class my.project.moviesbox.database.entity.** {*;}

#ijkplayer
-keep class tv.danmaku.ijk.media.player.** {*;}
-keep class tv.danmaku.ijk.media.player.IjkMediaPlayer{*;}
-keep class tv.danmaku.ijk.media.player.ffmpeg.FFmpegApi{*;}
-dontwarn tv.danmaku.ijk.media.player.*
-keep interface tv.danmaku.ijk.media.player.** { *; }

-keep public class my.project.moviesbox.config.JZExoPlayer {*;}
-keep public class my.project.moviesbox.config.JZMediaIjk {*;}
-keep public class cn.jzvd.JZMediaSystem {*; }

#OkHttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

#Aria
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keep class **$$DownloadGroupListenerProxy{ *; }
-keep class **$$DGSubListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
    @DownloadGroup.* <methods>;
}
# 保持 Zstd 库的所有类不被混淆
-keep class com.github.luben.zstd.** { *; }

# 如果你需要确保 JNI 的 native 方法不被混淆，添加以下规则
-keepclasseswithmembers class * {
    native <methods>;
}

-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class androidx.media3.** {*;}
-keep interface androidx.media3.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class com.kongzue.dialogx.** { *; }
-dontwarn com.kongzue.dialogx.**

# 额外的，建议将 android.view 也列入 keep 范围：
-keep class android.view.** { *; }

# 若启用模糊效果，请增加如下配置：
-dontwarn androidx.renderscript.**
-keep public class androidx.renderscript.** { *; }

-dontwarn javax.xml.stream.events.**
-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }
-keepattributes ElementList, Root
-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.google.j2objc.annotations.RetainedWith
-dontwarn com.sun.net.httpserver.HttpContext
-dontwarn com.sun.net.httpserver.HttpHandler
-dontwarn com.sun.net.httpserver.HttpServer
-dontwarn javax.enterprise.context.ApplicationScoped
-dontwarn javax.enterprise.inject.Alternative
-dontwarn javax.inject.Inject
-dontwarn lombok.bytecode.PoolConstantsApp
-dontwarn lombok.bytecode.PostCompilerApp
-dontwarn lombok.bytecode.PreventNullAnalysisRemover
-dontwarn lombok.bytecode.SneakyThrowsRemover
-dontwarn lombok.core.Main$LicenseApp
-dontwarn lombok.core.Main$VersionApp
-dontwarn lombok.core.PublicApiCreatorApp
-dontwarn lombok.core.configuration.ConfigurationApp
-dontwarn lombok.core.handlers.SneakyThrowsAndCleanupDependencyInfo
-dontwarn lombok.core.runtimeDependencies.CreateLombokRuntimeApp
-dontwarn lombok.delombok.DelombokApp
-dontwarn lombok.eclipse.handlers.HandleAccessors
-dontwarn lombok.eclipse.handlers.HandleBuilder
-dontwarn lombok.eclipse.handlers.HandleBuilderDefault
-dontwarn lombok.eclipse.handlers.HandleCleanup
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleAllArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleNoArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleRequiredArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleData
-dontwarn lombok.eclipse.handlers.HandleDelegate
-dontwarn lombok.eclipse.handlers.HandleEqualsAndHashCode
-dontwarn lombok.eclipse.handlers.HandleExtensionMethod
-dontwarn lombok.eclipse.handlers.HandleFieldDefaults
-dontwarn lombok.eclipse.handlers.HandleFieldNameConstants
-dontwarn lombok.eclipse.handlers.HandleGetter
-dontwarn lombok.eclipse.handlers.HandleHelper
-dontwarn lombok.eclipse.handlers.HandleLog$HandleCommonsLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleCustomLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleFloggerLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleJBossLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleJulLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleLog4j2Log
-dontwarn lombok.eclipse.handlers.HandleLog$HandleLog4jLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleSlf4jLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleXSlf4jLog
-dontwarn lombok.eclipse.handlers.HandleNonNull
-dontwarn lombok.eclipse.handlers.HandlePrintAST
-dontwarn lombok.eclipse.handlers.HandleSetter
-dontwarn lombok.eclipse.handlers.HandleSneakyThrows
-dontwarn lombok.eclipse.handlers.HandleSuperBuilder
-dontwarn lombok.eclipse.handlers.HandleSynchronized
-dontwarn lombok.eclipse.handlers.HandleToString
-dontwarn lombok.eclipse.handlers.HandleUtilityClass
-dontwarn lombok.eclipse.handlers.HandleVal
-dontwarn lombok.eclipse.handlers.HandleValue
-dontwarn lombok.eclipse.handlers.HandleWith
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaMapSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaSetListSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaTableSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilListSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilMapSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilSetSingularizer
-dontwarn lombok.installer.Installer$CommandLineInstallerApp
-dontwarn lombok.installer.Installer$CommandLineUninstallerApp
-dontwarn lombok.installer.Installer$GraphicalInstallerApp
-dontwarn lombok.installer.eclipse.AngularIDELocationProvider
-dontwarn lombok.installer.eclipse.EclipseLocationProvider
-dontwarn lombok.installer.eclipse.JbdsLocationProvider
-dontwarn lombok.installer.eclipse.MyEclipseLocationProvider
-dontwarn lombok.installer.eclipse.RhdsLocationProvider
-dontwarn lombok.installer.eclipse.STS4LocationProvider
-dontwarn lombok.installer.eclipse.STSLocationProvider
-dontwarn lombok.javac.handlers.HandleAccessors
-dontwarn lombok.javac.handlers.HandleBuilder
-dontwarn lombok.javac.handlers.HandleBuilderDefault
-dontwarn lombok.javac.handlers.HandleBuilderDefaultRemove
-dontwarn lombok.javac.handlers.HandleCleanup
-dontwarn lombok.javac.handlers.HandleConstructor$HandleAllArgsConstructor
-dontwarn lombok.javac.handlers.HandleConstructor$HandleNoArgsConstructor
-dontwarn lombok.javac.handlers.HandleConstructor$HandleRequiredArgsConstructor
-dontwarn lombok.javac.handlers.HandleData
-dontwarn lombok.javac.handlers.HandleDelegate
-dontwarn lombok.javac.handlers.HandleEqualsAndHashCode
-dontwarn lombok.javac.handlers.HandleExtensionMethod
-dontwarn lombok.javac.handlers.HandleFieldDefaults
-dontwarn lombok.javac.handlers.HandleFieldNameConstants
-dontwarn lombok.javac.handlers.HandleGetter
-dontwarn lombok.javac.handlers.HandleHelper
-dontwarn lombok.javac.handlers.HandleLog$HandleCommonsLog
-dontwarn lombok.javac.handlers.HandleLog$HandleCustomLog
-dontwarn lombok.javac.handlers.HandleLog$HandleFloggerLog
-dontwarn lombok.javac.handlers.HandleLog$HandleJBossLog
-dontwarn lombok.javac.handlers.HandleLog$HandleJulLog
-dontwarn lombok.javac.handlers.HandleLog$HandleLog4j2Log
-dontwarn lombok.javac.handlers.HandleLog$HandleLog4jLog
-dontwarn lombok.javac.handlers.HandleLog$HandleSlf4jLog
-dontwarn lombok.javac.handlers.HandleLog$HandleXSlf4jLog
-dontwarn lombok.javac.handlers.HandleNonNull
-dontwarn lombok.javac.handlers.HandlePrintAST
-dontwarn lombok.javac.handlers.HandleSetter
-dontwarn lombok.javac.handlers.HandleSneakyThrows
-dontwarn lombok.javac.handlers.HandleSuperBuilder
-dontwarn lombok.javac.handlers.HandleSynchronized
-dontwarn lombok.javac.handlers.HandleToString
-dontwarn lombok.javac.handlers.HandleUtilityClass
-dontwarn lombok.javac.handlers.HandleVal
-dontwarn lombok.javac.handlers.HandleValue
-dontwarn lombok.javac.handlers.HandleWith
-dontwarn lombok.javac.handlers.singulars.JavacGuavaMapSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacGuavaSetListSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacGuavaTableSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilListSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilMapSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilSetSingularizer
-dontwarn org.mapstruct.ap.spi.AstModifyingAnnotationProcessor
-dontwarn sun.net.www.protocol.http.Handler