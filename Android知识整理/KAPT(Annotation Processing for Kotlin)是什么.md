### 什么是APT？

**Annotation Processing Tool**官方称其为**Pluggable Annotation Processing API**（可插入式注解处理API？），在06年的Java规范[JSR-269](https://link.jianshu.com?t=https%3A%2F%2Fwww.jcp.org%2Fen%2Fjsr%2Fdetail%3Fid%3D269)中提供了一套标准API来处理Annotation([JSR-175](https://link.jianshu.com?t=https%3A%2F%2Fjcp.org%2Fen%2Fjsr%2Fdetail%3Fid%3D175))，这里提到的注解不是在**运行时**通过反射机制运行处理的注解，而是在**编译时**处理的注解。其实就是javac提供的一个工具，用来在编译时期去扫描处理注解信息，它把Java语言中众多元素（如method、type、enum等）映射为Types和Elements，然后把这些扫描到的信息提供给Annotation Processor进行处理，编译器每次执行完**process()**方法就算是一个_“round”_结束，如果一个_“round”_结束后有新的代码生成，编译器会继续调用Annotation Processor，直到没有新代码生成为止。
其实就是Java提供了一个编译插件，允许我们通过这个插件在程序编译时生成一些代码，并且执行生成的代码将会与你手动编写的代码一起被javac编译！！

![](//upload-images.jianshu.io/upload_images/745509-2466eb9612cd113c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/290)

Annotation Processor执行流程.png

### 什么是KAPT？

kpat--[Kotlin Annotation Processing](https://link.jianshu.com?t=https%3A%2F%2Fblog.jetbrains.com%2Fkotlin%2F2015%2F05%2Fkapt-annotation-processing-for-kotlin%2F)，顾名思义，就是服务于Kotlin的Annotation Processing，Kotlin M12版本中发布了kapt1，在这之前，Kotlin的官方Blog中提到JetBrains团队在考虑支持Annotation Processing时提出了3种解决方案：

1.  为Kotlin重新实现一套JSR-269API，但这样做就只支持Kotlin项目了，使用这种方案明显不合理，而且也与“Kotlin可以完美与现有Java项目共存”相违背。
2.  从Kotlin源码中生成Java源码，也就是将你的Kotlin代码逻辑给翻译成可执行的Java代码，并将它们加入到`javac`的classpath中，最后运行注解处理器，这种方案带来的工作量和难度是巨大的；不过另一种基于JVM的语言Groovy已经有另一种[解决方案](https://link.jianshu.com?t=https%3A%2F%2Fdocs.gradle.org%2F2.4-rc-1%2Frelease-notes.html%23support-for-%25E2%2580%259Cannotation-processing%25E2%2580%259D-of-groovy-code)，就是生成"存根类"，这个类里面所有方法的方法体为空，也就是只保留类的结构，然后把这些"存根类"加入`javac` classpath中编译，这种解决方案的一个优点是让引用注解处理器生成的类成为可能，但是这种解决方案也存在一些问题，比如说有很多时候方法的返回类型是需要对表达式进行分析，并且生成这些"存根类"要解决项目中所有的声明，这将会大大降低编译速度。

    ![](//upload-images.jianshu.io/upload_images/745509-4f6d2a2852c08308.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/228)

    解决方案2.png

3.  简单来说就是将Kotlin代码编译成Java编译器可识别的二进制文件，在Java编译器把扫描的注解信息交给Processor执行之前，Kotlin编译器会注入其扫描的注解信息。但是由于在一开始Kotlin就已经将代码编译好，所以代码无法引用Processor生成的类，而且仅在源文件中保留的注解(RetentionPolicy.SOURCE)将被忽略。

![](//upload-images.jianshu.io/upload_images/745509-e17846298b761412.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/437)

解决方案3.png

JetBrains团队最终采取第3种方案实现了kapt1，如上所说，这种实现方案最大的一个限制就是Kotlin代码引用不了Processor生成的代码，生成这些不被引用的代码有什么用？当然，你可以事先定义好这些生成类的路径与方法名然后在源码中通过反射去调用（手动滑稽。因此，随后发布的版本中JetBrains团队做了一些优化：生成源码的"存根类"以支持对生成代码的引用；当然上面提到的问题依然没有解决，只是把生成"存根类"作为可选配置由用户自己决定是否使用该功能而已，而且这种对生成代码的引用并不完美：虽然生成的"存根类"中没有方法体，所以方法体内对尚未生成的代码进行引用不会报错，但是方法签名中如果包含了对这些代码的引用（参数类型或者返回类型），最终Processor生成的Java代码中会引用一些不存在的类从而导致编译器报错（`unresolved reference`）。
后来相继推出了kapt2以及kapt3，kapt2通过包装Intellij平台的抽象语法树实现了[JSR-269](https://link.jianshu.com?t=https%3A%2F%2Fwww.jcp.org%2Fen%2Fjsr%2Fdetail%3Fid%3D269)来克服上面提到的限制，但是Intellij平台没有针对这些实现进行优化，导致某些时候注解处理器可能会非常慢；kapt3替代了kapt2的实现，直接从Kotlin代码生成`javac`的Java AST，后面的步骤直接走`javac`原本的流程即可。
到目前为止kapt3已经解决上面提到的大部分问题，但仍然存在一些不太友好的地方，比如使用kapt3生成了Kotlin代码在AndroidStudio中是不会加入到Kotlin编译器中编译的，需要自己在build.gradle中配置类似下面这样的代码

```
afterEvaluate {
    android.applicationVariants.all { variant ->
        if (variant.buildType.name == 'debug') {
            def kotlinGenerated = file("$buildDir/generated/source/kaptKotlin/${variant.name}")
            variant.addJavaSourceFoldersToModel(kotlinGenerated)
            return
        }
        ...
    }
}

```

而且在Processor中打印的`NOTE`级别日志也没有输出到控制台中，调试的时候非常不方便，debug还要求你手速要快- -！（debug方式参考[这里](https://link.jianshu.com?t=https%3A%2F%2Fmedium.com%2F%40daptronic%2Fannotation-processing-with-kapt-and-gradle-237793f2be57)）
总的来说，现在Kotlin对Annotation Processing的支持除了存在一些小问题，总体上还是不错的，期待JetBrains团队后续的优化

参考链接：
官方博客相关文章：[https://blog.jetbrains.com/kotlin/2015/05/kapt-annotation-processing-for-kotlin/](https://link.jianshu.com?t=https%3A%2F%2Fblog.jetbrains.com%2Fkotlin%2F2015%2F05%2Fkapt-annotation-processing-for-kotlin%2F)
[https://blog.jetbrains.com/kotlin/2015/06/better-annotation-processing-supporting-stubs-in-kapt/](https://link.jianshu.com?t=https%3A%2F%2Fblog.jetbrains.com%2Fkotlin%2F2015%2F06%2Fbetter-annotation-processing-supporting-stubs-in-kapt%2F)
Medium相关文章：[https://medium.com/@workingkills/pushing-the-limits-of-kotlin-annotation-processing-8611027b6711](https://link.jianshu.com?t=https%3A%2F%2Fmedium.com%2F%40workingkills%2Fpushing-the-limits-of-kotlin-annotation-processing-8611027b6711)
[https://medium.com/@daptronic/annotation-processing-with-kapt-and-gradle-237793f2be57](https://link.jianshu.com?t=https%3A%2F%2Fmedium.com%2F%40daptronic%2Fannotation-processing-with-kapt-and-gradle-237793f2be57)

作者：WuRichard
链接：https://www.jianshu.com/p/8c3437006e79
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。