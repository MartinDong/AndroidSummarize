APK的混淆分为资源混淆与代码混淆.一般大部分都使用两者结合.尤其是目前主流的应用.

#### 其中的优点:

*   **防止被恶意破解逆向分析**
*   **减少apk体积,也是瘦身的方法**
*   **代码可阅读性降低**

#### 其中的缺点:

*   **调试不方便(可以配置mapping变得方便)**
*   **测试不充分,可能导致部分功能不能使用(比如注解相关等)**

#### 混淆前(这儿偷个懒直接用工具反编译看):

![这里写图片描述](http://img.blog.csdn.net/20160920105928451)

#### 混淆后:

![这里写图片描述](http://img.blog.csdn.net/20160920110700837)

## 如何使用代码混淆:

1.直接在build.gradle文件中配置即可.如图: 
![这里写图片描述](http://img.blog.csdn.net/20160920103345493) 
图片有了,文件也找到了,接下来了呢?

```java
buildTypes {
        debug {
            // 如果没有提供混淆规则文件，则设置默认的混淆规则文件（SDK/tools/proguard/proguard-android.txt）
            pseudoLocalesEnabled true
            // 显示Log
            buildConfigField "boolean", "LOG_DEBUG", "true"
            //混淆
            minifyEnabled false
            //Zipalign优化
            zipAlignEnabled true
            // 移除无用的resource文件
            shrinkResources true
            //加载默认混淆配置文件
            proguardFiles getDefaultProguardFile('proguard-Android.txt'), 'proguard-rules.pro'
            //签名
            signingConfig signingConfigs.debug
        }
        release {
            // 如果没有提供混淆规则文件，则设置默认的混淆规则文件（SDK/tools/proguard/proguard-android.txt）
            pseudoLocalesEnabled true
            // 不显示Log
            buildConfigField "boolean", "LOG_DEBUG", "false"
            //混淆
            minifyEnabled true
            //Zipalign优化
            zipAlignEnabled true
            // 移除无用的resource文件
            shrinkResources true
            //加载默认混淆配置文件
            proguardFiles getDefaultProguardFile('proguard-Android.txt'), 'proguard-rules.pro'
            //签名
            signingConfig signingConfigs.relealse
        }
    }12345678910111213141516171819202122232425262728293031323334
```

经过这样的配置过后呢,可以发现gradle是加载了一个混淆的配置文件(proguard-Android.txt,这个文件的位置和build.gradle同级),根据混淆配置文件的规则进行混淆的.为什么要混淆配置呢?因为有些东西是不能混淆的,比如jni调用,本身就是根据包名去调用的,如果混淆了就会NotFoundMethod了.所以这个规则就是自定义的了.

## 如何自定义:

```java
-libraryjars class_path //应用的依赖包，如Android-support-v4  
-keep [,modifier,...] class_specification //这里的keep就是保持的意思，意味着不混淆某些类 
-keepclassmembers [,modifier,...] class_specification //同样的保持，不混淆类的成员  
-keepclasseswithmembers [,modifier,...] class_specification //不混淆类及其成员  
-keepnames class_specification //不混淆类及其成员名  
-keepclassmembernames class_specification //不混淆类的成员名  
-keepclasseswithmembernames class_specification //不混淆类及其成员名  
-assumenosideeffects class_specification //假设调用不产生任何影响，在proguard代码优化时会将该调用remove掉。如system.out.println和Log.v等等  
-dontwarn [class_filter] //不提示warnning 123456789
```

接下来看了语法可能有点蒙了,这些只是了解就可以了,一般有通用的.

#### 比如:

```ruby
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
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
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 系统类不需要混淆
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keepattributes Signature
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-dontwarn com.alipay.android.phone.mrpc.core**
-keep class com.alipay.android.phone.mrpc.core.**{*;}

-dontwarn com.alipay.apmobilesecuritysdk.face**
-keep class com.alipay.apmobilesecuritysdk.face.**{*;}

#  百度导航的不需要混淆
#-dontwarn com.baidu.navisdk.comapi.tts.ttsplayer**
#-keep class com.baidu.navisdk.**{*;}

#  Jpush不需要混淆
-dontwarn cn.jpush**
-keep class cn.jpush.** { *; }#Jpush

# XUtils工具不需要混淆
-dontwarn com.lidroid**
-keep class com.lidroid.**{*;}#ViewInject

# 自定义控件不需要混淆
-keep class com.cheweishi.android.widget.** {*;}#CustomView

-dontwarn com.sinovoice**
-keep class com.sinovoice.** { *; }

# 百度地图相关不需要混淆
-dontwarn com.baidu**
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}

#-dontwarn demo.Pinyin4jAppletDemo**
#-keep class demo.Pinyin4jAppletDemo{*;}

# volley工具不需要混淆
-dontwarn com.android.volley.toolbox**
-keep class com.android.volley.toolbox{*;}

# gson工具不需要混淆
-dontwarn com.google.gson**
-keep class com.google.gson.**{*;}

#-dontwarn com.nineoldandroids.**
#-keep class com.nineoldandroids.**{*;}

-dontwarn org.apache.http**
-keep class org.apache.http.**{*;}

-dontwarn com.handmark.pulltorefresh**
-keep class com.handmark.pulltorefresh.**{*;}

-dontwarn com.squareup.picasso**
-keep class com.squareup.picasso.**{*;}

-dontwarn com.cheweishi.android.entity**
-keep class com.cheweishi.android.entity.**{*;}

-keep class com.cheweishi.android.response.BaseResponse

-keep public class com.android.vending.licensing.ILicensingService

-printmapping mapping.txt #混淆后文件映射

#-keep public class com.cheweishi.android.R$*{
#    public static final int *;
#}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119
```

当你混淆后你会发现一些错误日志全是a.b.c.d()等,根本不知道具体错误在哪儿,这就是混淆后调试的确定,竟然提供了混淆肯定就可以还原啦,google早就考虑到了.那我要怎么还原呢?

1.**cmd进入sdk/tools/proguard/bin目录。** 
2.**将混淆后的日志和上文提到的mapping文件放入此目录中。** 
3.**执行命令：retrace.bat mapping.txt XXX.txt**

未还原前: 
![这里写图片描述](http://img.blog.csdn.net/20160920143328134) 
还原后: 
![这里写图片描述](http://img.blog.csdn.net/20160920143343291)

这下有没有觉得比较好调试了,这下你比较关心如何线上的产品如何查看呢?一般(友盟)提供了mapping文件管理的,所以混淆规则的文件(proguard-Android.txt)的生成是非常有必要的.

### 资源混淆:

资源混淆是可以解决apk瘦身,主要就是压缩了资源文件及修改了文件名字及映射关系.看看效果吧.

#### 资源混淆前:

![这里写图片描述](http://img.blog.csdn.net/20160920113545580)

#### 资源混淆后:

![这里写图片描述](http://img.blog.csdn.net/20160920113602298)

##### 关于资源混淆有很多种解决方案,首先我们可以看看最早美团提出来的([美团资源保护实战](http://tech.meituan.com/mt-android-resource-obfuscation.html),这里就不做具体的分析):

*   **1.首先介绍了打包的过程,是通过appt对资源进行记录.**
*   **2.了解原理后,可以通过修改源码在资源文件映射的时候修改文件名字及映射路径**
*   **3.美团仅仅是提供了修改的思路,并未将混淆的函数公开**

其实通过这样修改aapt,然后再Linux编译出来的aapt(可以编译分为Linux或者windows,以windows为例)替换置SDK目录下/build-tools/aapt.exe,然后使用对应版本编译即可.**当然这样的做法虽然可以实现,但是非常麻烦(依赖编译过程,依赖编译源码…),比如aapt升级等**

再看看微信开源的git([AndResGuard](https://github.com/shwenzhang/AndResGuard)),大致原理将资源文件通过7zip进行压缩后重新映射然后打包成apk并对其签名. 
![这里写图片描述](http://img.blog.csdn.net/20160920141903385)

对比: 
![这里写图片描述](http://img.blog.csdn.net/20160920142027948)

如果你还需要对apk瘦身,你可以将编译前无用资源给删除.具体可使用gradle提供的Lint.

#### 如何使用:

![这里写图片描述](http://img.blog.csdn.net/20160920142251372) 
然后build后会在projectName\app\build\outputs\lint-results.xml,该Lint可以检测语法以及无用资源,然后写一个工具就可以删除了.这里也可以推荐大家使用:lintAutoCleaner,该工具选择对应的lint-results.xml文件就可以自动删除并备份了,非常人性化.

