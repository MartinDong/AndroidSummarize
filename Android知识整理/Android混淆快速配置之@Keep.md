代码混淆是每个java程序猿的必修课，但是为项目写混淆配置文件是一件痛苦的事情，关键字比较多不太好记忆，而且一旦配置好很长时间内都不需要修改，也许当时记得，过后慢慢就忘记了。那不配置混淆行不行呢？答案肯定是不行的，因为你要面对虎视眈眈的同行还有行业内的各种黑暗的利益链条，不混淆就发布几乎等于将自己的工作成果拱手让人。那你可能会问那我混淆了就可以确保万无一失了？我只能残酷的告诉你：NO！虽然说混淆不能保证你的代码的100%安全但是这是通往代码安全之路的第一步。下面来讲解如何为我们的项目快速的配置混淆。

1.  Keep Options 保持不变的选项，简单点说就是保持哪些类或者哪些元素不被混淆，“keep家族”就是帮我们做这件事情的。
    `-keep [,modifier,...] class_specification`
    保持class_specification规则；若有[,modifier,...]，则先启用它的规则
    用法示例：

```
#不混淆实现android.os.Parcelable的类
-keep class * implements android.os.Parcelable

```

`-keepclassmembers [,modifier,...]class_specification`
保持类的成员：属性(可以是成员属性、类属性)、方法(可以是成员方 法、类方法)
`-keepclasseswithmembers [,modifier,...] class_specification`
与-keep功能基本一致(经测试)
`-keepnames class_specification`
可以说是 `-keep,allowshrinking class_specification`的简化版
`-keepclassmembernames class_specification`
Short for -keepclassmembers,allowshrinking class_specification
`-keepclasseswithmembernames class_specification`
Short for `-keepclasseswithmembers,allowshrinking class_specification1`-printseeds [filename]`
打印匹配的-keep家族处理的 类和类成员列表，到标准输出。用Proguard 命令行，能看到输出效果（未测试）

1.  @Keep
    在使用@Keep注解之前我们需要先导入
    `compile 'com.android.support:support-annotations:25.1.1'`类库
    使用方法非常简单，可以注解在类，方法和变量,总结起来就是一句话哪里不想被混淆就注解哪里。

3.实战
以上算是基础知识，下面演示如何在实际生产环境中应用它们，下面我们创建一个测试工程，打开android studio创建一个android工程，目录结构如下：
创建要被混淆的类：

```
@Keep
public class Test {
}

public class TestA {
}

```

在混淆脚本中添加如下内容：

```
#打印混淆信息
-verbose
#代码优化选项，不加该行会将没有用到的类删除，这里为了验证时间结果而使用，在实际生产环境中可根据实际需要选择是否使用
-dontshrink
-dontwarn android.support.annotation.Keep
#保留注解，如果不添加改行会导致我们的@Keep注解失效
-keepattributes *Annotation*
-keep @android.support.annotation.Keep class **

```

开始混淆打包，查看混淆后的结果：

![](https://upload-images.jianshu.io/upload_images/1857802-a3ef2f612d566076.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

WX20170215-161319.png

我们发现`TestA`不见了而`Test`保留了下来，说明我们的配置起作用了，下面我们在`Test` 类中增加点内容看看混淆后会变成什么样子，修改后的类内容如下：

```
@Keep
public class Test {
    int age = 20;
    protected String sex = "m";
    public String name = "CodingMaster";
}

```

查看混淆后的结果：

![](https://upload-images.jianshu.io/upload_images/1857802-9c4951c19d40a340.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

WX20170215-162637.png

不幸的是虽然类名保留下来了，但是里面的内容却被混淆了，如果我们想把`name`变量不被混淆怎么办呢？不急，我们修改我们的混淆脚本内容如下：

```
#打印混淆信息
-verbose
#代码优化选项，不加该行会将没有用到的类删除，这里为了验证时间结果而使用，在实际生产环境中可根据实际需要选择是否使用
-dontshrink
-dontwarn android.support.annotation.Keep
#保留注解，如果不添加改行会导致我们的@Keep注解失效
-keepattributes *Annotation*
-keep @android.support.annotation.Keep class **{
@android.support.annotation.Keep <fields>;
}

```

我们继续修改`Test`类，这次我们多加了点东西，会在后面用到，内容如下：

```
@Keep
public class Test {
    int age = 20;
    @Keep
    protected String sex = "m";
    @Keep
    public String name = "CodingMaster";

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    private void cry(){

    }
}

```

重新混淆查看结果：

![](https://upload-images.jianshu.io/upload_images/1857802-9bf189ea8ee56b0c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

WX20170215-172247.png

哈哈我们的`name`变量被成功的保留了，同理如何保留被`sex`变量呢？这里就不买关子了，直接给出答案，为`sex`添加`@Keep`注解就可以了，持怀疑态度的同学👨‍🎓可以自己去验证。
细心的同学可能已经发现，`Test`类里面的方法都被混淆了，怎样指定某个方法不被混淆呢？
继续修改混淆文件：

```
#打印混淆信息
-verbose
#代码优化选项，不加该行会将没有用到的类删除，这里为了验证时间结果而使用，在实际生产环境中可根据实际需要选择是否使用
-dontshrink
-dontwarn android.support.annotation.Keep
#保留注解，如果不添加改行会导致我们的@Keep注解失效
-keepattributes *Annotation*
-keep @android.support.annotation.Keep class **{
@android.support.annotation.Keep <fields>;
@android.support.annotation.Keep <methods>;
}

```

然后为`cry()`方法添加`@Keep`注解，重新混淆查看结果：

![](https://upload-images.jianshu.io/upload_images/1857802-9e2b22a3345daf71.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

WX20170215-172949.png

有没有很简单的感觉呢？哪里不混淆`@Keep`哪里，再也不用为混淆头疼了！

作者：祖传大苹果
链接：https://www.jianshu.com/p/9dacabd351e3
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。