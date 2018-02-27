## Fragment或View如何支持组件化

距离 [Android组件化方案](http://blog.csdn.net/guiying712/article/details/55213884) 发布已经半年有余，虽说这个方案已经能够解决一些项目的需求，但是依然不够完美。很多开发者也在博客和GitHub中留言甚至发邮件问我，Fragment怎么办？ 目前市面上APP的风格还是类似于微信界面的比较多，好几个Fragment摆在主界面中，然后点击NavigationBar上的图标显示不同的Fragment。但是很显然[Android组件化方案](http://blog.csdn.net/guiying712/article/details/55213884) 
并不适合这种情况。刚开始我给大家想了一个不是那么优雅的折中方案，这个方案是将我们应用的MainActivity移动到“app壳工程”中，因为“app壳工程”的本身就肩负着管理和组装业务组件的功能，因此这个MainActivity自然也就拿到了分散到其他业务组件中的Fragment，就像下图这样：

![偷懒的Fragment组件化工程模型](http://img.blog.csdn.net/20170921214603904?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ3VpeWluZzcxMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

这个方案虽说也是可行的，但是显然没有达到我们期望的结果，我们理想的“app壳工程”是不应该跟业务有关的，他应该负责管理和组装其他组件，并将这些业务组件包装成一个可以发布到应用市场的APP，也就是说我们希望“app壳工程”不要和任何业务相关，不要耦合其他组件中的代码，我想我可以随意的替换那个空壳工程，而不会影响到我的APP打包，显然这个偷懒的方案是做不到这一点的。因此我必须解决的问题是：**一个业务组件如何在不依赖其他业务组件的情况下拿到这些业务组件中的Fragment或者其他View？**

假设小A收到一个邀请函，邀请他要去参加一个互联网技术会议，而这个会议在一个叫”XX大酒店“中举行，但是小A之前并没有听过这个酒店，那么他怎么才能找到这个酒店并参加会议呢？大多数同学都会习惯性的打开百度地图，然后输入“XX大酒店”，百度地图就会帮我找到这个酒店。但是大家有没有想过为什么百度地图能找到这个酒店呢？这时候肯定有人会说：这不是废话吗，百度地图都不知道还有谁知道? 这让我想起08年那时候还没有智能手机，我想去兰州的一个大厦，但是我问了周围很多路人都没有人知道这个大厦在哪里。而现在我们去一个地方从问路人变成了问百度地图，那么又回到哪句话，百度地图是怎么知道这些地方呢？有两种可能：一种是有人告诉百度地图某个地点在那里（那些小商店就是这样做的），另一种是百度地图派人去城市里晃悠把城市的所有显著的地标都记录下来。他们的关系就像下图表示的这样：

![这里写图片描述](http://img.blog.csdn.net/20170923145311291?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ3VpeWluZzcxMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

其实在 [Android组件化方案](http://blog.csdn.net/guiying712/article/details/55213884) 中已经有类似功能的组件：Common组件，还有另外一个就ARouter了。但是鉴于ARouter是开源库，我们不方便去修改，那么我们就在Common组件中做手脚。如果我想让Common组件知道D组件中的DFragment，我们需要怎么做呢？首先将CFragment和DFragment添加到Common组件中去，B组件想要获取DFragment，直接就去Common组件查找就行。

![这里写图片描述](http://img.blog.csdn.net/20171008200144364?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ3VpeWluZzcxMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

这时候你一定很激动，仿佛发现了什么绝世秘密一样，你恨不得立马就写个Demo测试下这个方案。当你撸起袖子开干后发现， What? 怎么才能把DFragment添加到BaseApplication啊？我们都知道Application启动后会回调onCreate（）方法，貌似我们可以在Application启动的时候在onCreate方法中把Fragment添加到BaseApplication中去。这时候你脑海肯定会付出那个黑人问号的表情，总不能让D组件去依赖Common吧？这关系太特么乱了。

但是经过前面的铺垫，其实大家都发现了点什么，那就是：**只要我们能在业务组件中知道Application的生命周期，那么我们就可以在Application onCreate 时将业务组件中的Fragment添加到Common组件中！**那么这个时候我们就需要解决：如何才能让业务组件知道Application的生命周期呢？问题分析到这里，我们看看下面的类图：

![这里写图片描述](http://img.blog.csdn.net/20170923160443647?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ3VpeWluZzcxMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

首先我们Common组件中定义一个代理接口，这个代理接口定义了Application中的回调方法，然后各个业务组件实现这个代理接口，然后在onCreate方法中做自己想做的事情，而BaseApplication会在调用onCreate方法时找到所有实现了ApplicationDelegate的类，并调用这些实现类的方法，这样业务组件就知道了我们应用程序的生命周期；**当业务组件知道应用程序的声明周期后，不仅可以在业务组件中将Fragment添加到Common组件中，而且还可以在业务组件中初始化数据，由于全局Context可以在任何组件中获取，实际上这种方式已经等同于在Application中初始化数据。**

## 如何管理组件

在 [Android组件化方案](http://blog.csdn.net/guiying712/article/details/55213884) 中，由于所有组件都在同一个项目中，并且使用 **compile project(‘:组件名’)** 方式依赖其他组件，这样就会导致很多问题。

**1\. 编译很慢。**由于所有的组件工程都在同一个项目中，并且组件之间或app壳工程会依赖其他组件，导致每次打包APP都需要把各个组件编译一次，如果项目中的组件达到十几个后，结果真的很感人！随着组件数量的增长，编译时间几乎呈指数性增加，这个滋味，我想每位Android开发者都深有体会。 
**2\. 组件不方便引用。**因为我们的组件是以源代码的形式置于项目中，如果另外一个项目也需要某个组件，这个时候就只能再复制一份代码到新项目中。这就导致一个组件存在于多个项目中，那么最终肯定无法保证这个组件的代码会不会被修改，也就是说组件已经无法保证唯一性了。 
**3\. 无法控制权限，也不方便混淆。因为项目中包含所有的组件源代码，这时候肯定没有办法控制代码权限了，假如某个组件是另外一个部门或公司提供给你用的，那么他们当然不希望给你源代码。

那么如果解决这些问题呢？我想大多数Android开发者都能想到这个办法。如果你把开源的三方库当做一个功能组件的话，那么很显然，我们在使用这些三方库的时候是通过什么方式呢？难道你会下载它的源代码吗，应该很少有人会这样做吧。那么让我们看看我们是怎么引入三方库的:

```java
    compile 'com.github.bumptech.glide:glide:3.8.0'
    compile 'io.reactivex.rxjava2:rxjava:2.1.3'
    compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
    compile 'com.squareup.retrofit2:retrofit:2.3.0'
    compile 'com.google.code.gson:gson:2.8.1'
    compile 'org.greenrobot:eventbus:3.0.0'123456
```

这样大家就很熟悉了，这些开源库一般都是上传到maven或jcenter仓库上供我们引用。那么我们自己开发的组件能不能也传到maven或jcenter仓库呢？当然了不是让你传到开源仓库上去，我的意思是我们可以在公司内部搭建一个私有的maven仓库，将我们开发好的组件上传到这个私有的maven仓库上，然后内部开发人员就可以像引用三方库那样轻而易举的将组件引入到项目中了，这是他们关系就像下图这样：

![这里写图片描述](http://img.blog.csdn.net/20171019203927625?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ3VpeWluZzcxMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

搭建仓库管理私服主要有如下目的:

1.  提升编译性能和可靠性
2.  为所有二进制软件组件及其依赖提供配置管理中心
3.  为你所在组织和公开仓库提供一个高级可配置的代理
4.  建立私有组件发布中心
5.  通过改善组件的可用性、版本控制、安全、质量而提升其可维护性和可管理性。

而这也恰好解决了我们在组件化项目中碰到的问题。本来我想将[Android组件化项目AndroidModulePattern](https://github.com/guiying712/AndroidModulePattern) 中的组件上传到 **jitpack** ，然后给大家做个演示，但是很可惜，我试了很多次都失败了，大家只能自己试试了。