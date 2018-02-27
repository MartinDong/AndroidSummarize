在app的开发中，页面之间的相互跳转是最基本常用的功能。在Android中的跳转一般通过显式intent和隐式intent两种方式实现的，而Android的原生跳转方式会存在一些缺点：

*   显式intent的实现方式，因为会存在直接的类依赖的问题，导致耦合严重；
*   隐式intent的实现方式，则会出现规则集中式管理，导致协作变得困难；
*   可配置性较差，一般而言配置规则都是在Manifest中的，这就导致了扩展性较差；
*   跳转过程无法控制，一旦使用了StartActivity()就无法插手其中任何环节了，只能交给系统管理；
*   当多组件化开发，使用原生的路由方式很难实现完全解耦；

而阿里的[ARouter](https://link.jianshu.com?t=https://github.com/alibaba/ARouter)路由框架具有解耦、简单易用、支持多模块项目、定制性较强、支持拦截逻辑等诸多优点，很好的解决了上述的问题。关于ARouter具体实现功能，典型应用以及相应技术方案实现的介绍不在这详细介绍，具体可参见[开源最佳实践：Android平台页面路由框架ARouter](https://link.jianshu.com?t=https://yq.aliyun.com/articles/71687?t=t1)。

阿里ARouter的分析计划

*   [阿里ARouter使用及源码解析（一）](https://www.jianshu.com/p/46d174f37e82)
*   [阿里ARouter拦截器使用及源码解析（二）](https://www.jianshu.com/p/c8d7b1379c1b)
*   阿里ARouter参数自动装载使用及源码解析（三）

##### 基本功能使用

1.添加依赖和配置

```
android {
    defaultConfig {
    ...
    javaCompileOptions {
        annotationProcessorOptions {
        arguments = [ moduleName : project.getName() ]
        }
    }
    }
}

dependencies {
    compile 'com.alibaba:arouter-api:1.2.1.1'
    annotationProcessor 'com.alibaba:arouter-compiler:1.1.2.1'
    ...
}

```

2.添加注解

```
// 在支持路由的页面上添加注解(必选)
// 这里的路径需要注意的是至少需要有两级，/xx/xx
@Route(path = "/test/test1")
public class Test1Activity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);
    }
}

```

3.初始化SDK

```
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn1,btn2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn1) {
            // 如果使用了InstantRun，必须在初始化之前开启调试模式，但是上线前需要关闭，InstantRun仅用于开发阶段，
            // 线上开启调试模式有安全风险，可以使用BuildConfig.DEBUG来区分环境
            ARouter.openDebug();
            ARouter.init(getApplication()); // 尽可能早，推荐在Application中初始化
        }
    }
}

```

4.发起跳转操作

```
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn1,btn2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn1) {
            ....
        } else if (v.getId() == R.id.btn2){
            ARouter.getInstance().build("/test/test1").navigation();
        }
    }
}

```

以上相关代码就是ARouter的最基本功能使用的步骤，下面来分析跳转功能是如何实现的。

##### 原理分析

###### 1.ARouter编译的过程

ARouter在编译期的时候，利用自定义注解完成了页面的自动注册。相关注解源码参见[arouter-annotation](https://link.jianshu.com?t=https://github.com/alibaba/ARouter/tree/master/arouter-annotation)，编译处理器源码参见[arouter-compiler](https://link.jianshu.com?t=https://github.com/alibaba/ARouter/tree/master/arouter-compiler)

下面是注解`@Route`的源码介绍：

```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Route {

    /**
     *路由的路径，标识一个路由节点
     */
    String path();

    /**
     * 将路由节点进行分组，可以实现按组动态加载
     */
    String group() default "";

    /**
     * 路由节点名称，可用于生成javadoc文档
     */
    String name() default "undefined";

    /**
     * 用32位int类型标示，可用于页面的一些配置
     */
    int extras() default Integer.MIN_VALUE;

    /**
     * 路由的优先级
     */
    int priority() default -1;
}

```

Route中的`extra`值是个int值，由32位表示，即转换成二进制后，一个int中可以配置31个1或者0，而每一个0或者1都可以表示一项配置（排除符号位），如果从这31个位置中随便挑选出一个表示是否需要登录就可以了，只要将标志位置为1，就可以在声明的拦截器中获取到这个标志位，通过位运算的方式判断目标页面是否需要登录。所以可以通过`extra`给页面配置30多个属性，然后在拦截器中去进行处理。
ARouter在拦截器中会把目标页面的信息封装一个类`Postcard`，这个类就包含了目标页面注解上`@Route`标识的各种信息。关于拦截器的使用以及源码分析，后续会有介绍。

将代码编译一遍，可以看到ARouter生成下面几个源文件：

![](https://upload-images.jianshu.io/upload_images/5994347-6c409a3386b0abd9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/327)

上面三个文件均是通过注解处理器`RouteProcessor`生成的，关于如何自定义注解处理器，可以阅读[Android编译时注解APT实战（AbstractProcessor）](https://www.jianshu.com/p/07ef8ba80562)，同时也需要学习**JavaPoet**的基本使用。下面我们看`RouteProcessor`是如何生成相关文件的。

```
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //判断被注解了的元素集合是否为空
        if (CollectionUtils.isNotEmpty(annotations)) {
            //获取所有被@Route注解的元素集合，Element可以是类、方法、变量等
            Set<? extends Element> routeElements = roundEnv.getElementsAnnotatedWith(Route.class);
            try {
                logger.info(">>> Found routes, start... <<<");
                //具体处理注解，生成java文件的方法
                this.parseRoutes(routeElements);

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;
    }

```

`process()`方法相当于处理器的主函数`main()`，可以在这个方法中扫描、评估和处理注解的代码，以及生成Java文件。`RouteProcessor`中调用了`parseRoutes()`，用来处理所有被`@Route`注解的元素。在分析上述三个java文件如何生成之前，先看看生成文件的具体代码。

*   ARouter$$Root$$app类

```
public class ARouter$$Root$$app implements IRouteRoot {
  @Override
  public void loadInto(Map<String, Class<? extends IRouteGroup>> routes) {
    routes.put("test", ARouter$$Group$$test.class);
  }
}

```

*   ARouter$$Group$$test类

```
public class ARouter$$Group$$test implements IRouteGroup {
  @Override
  public void loadInto(Map<String, RouteMeta> atlas) {
    atlas.put("/test/test1", RouteMeta.build(RouteType.ACTIVITY, Test1Activity.class, "/test/test1", "test", null, -1, -2147483648));
  }
}

```

*   ARouter$$Providers$$app类

```
public class ARouter$$Providers$$app implements IProviderGroup {
  @Override
  public void loadInto(Map<String, RouteMeta> providers) {
  }
}

```

我们接着分析上述三个文件是如何生成的

1.首先获取生成方法的参数的类型和参数名称

```
 private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {

            logger.info(">>> Found routes, size is " + routeElements.size() + " <<<");

            rootMap.clear();
             // TypeElement 表示一个类或接口元素
            // public static final String ACTIVITY = "android.app.Activity";
            //得到类activity元素
            TypeElement type_Activity = elementUtil.getTypeElement(ACTIVITY);
            // public static final String SERVICE = "android.app.Service";
            //得到类service的元素
            TypeElement type_Service = elementUtil.getTypeElement(SERVICE);
            // public static final String SERVICE = "android.app.Fragment";
            TypeMirror fragmentTm = elements.getTypeElement(FRAGMENT).asType();
             // public static final String SERVICE = "android.support.v4.app.Fragment";
            TypeMirror fragmentTmV4 = elements.getTypeElement(Consts.FRAGMENT_V4).asType();

            // public static final String IROUTE_GROUP = "com.alibaba.android.arouter.facade.template.IRouteGroup";
            //得到接口IRouteGroup元素
            TypeElement type_IRouteGroup = elementUtil.getTypeElement(IROUTE_GROUP);
          // public static final String IROUTE_GROUP = "com.alibaba.android.arouter.facade.template.IProviderGroup";
            //得到接口IProviderGroup元素
            TypeElement type_IProviderGroup = elementUtil.getTypeElement(IPROVIDER_GROUP);
            //获取RouteMeta，RouteType类名
            ClassName routeMetaCn = ClassName.get(RouteMeta.class);
            ClassName routeTypeCn = ClassName.get(RouteType.class);

            //下面代码是获取生成java文件中方法的参数类型名称和参数名称。
            /*
              获取获取ARouter$$Root$$app 类中方法参数Map<String, Class<? extends IRouteGroup>>类型的名称
             */
            ParameterizedTypeName inputMapTypeOfRoot = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_IRouteGroup))
                    )
            );

            /*
              获取ARouter$$Group$$test，ARouter$$Providers$$app类中方法参数 Map<String, RouteMeta>类型的名称
             */
            ParameterizedTypeName inputMapTypeOfGroup = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouteMeta.class)
            );

            /*
             获取相关的参数
             */
            //获取ARouter$$Root$$app 类中方法的参数Map<String, Class<? extends IRouteGroup>> routes
            ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTypeOfRoot, "routes").build();
           //获取ARouter$$Group$$test类中方法的参数Map<String, RouteMeta> atlas
            ParameterSpec groupParamSpec = ParameterSpec.builder(inputMapTypeOfGroup, "atlas").build();
             //获取ARouter$$Providers$$app类中方法的参数Map<String, RouteMeta> providers
            ParameterSpec providerParamSpec = ParameterSpec.builder(inputMapTypeOfGroup, "providers").build();  

          .....
        }
    }

```

2.获取了方法的参数的类型和参数名称后，下面便是生成相应的方法

```
 private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {
            ........

            /*
              首先创建ARouter$$Root$$xxx 类中的loadInto()方法
              @Override
              public void loadInto(Map<String, Class<? extends IRouteGroup>> routes) {}
             */
            MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(rootParamSpec);

            //  遍历所有被@Route注解的元素
            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                Route route = element.getAnnotation(Route.class);
                RouteMeta routeMete = null;

                //判断该元素否为 Activity 、IProvider 、 Service 的子类，然后创建相应的RouteMeta 对象
                if (typeUtil.isSubtype(tm, type_Activity.asType())) {                 // Activity
                    logger.info(">>> Found activity route: " + tm.toString() + " <<<");

                    // 如果是acitiviy类型，获取所有被@Autowired的属性
                    //关于@Autowired的注解，我们之后再进行分析
                    Map<String, Integer> paramsType = new HashMap<>();
                    for (Element field : element.getEnclosedElements()) {
                        if (field.getKind().isField() && field.getAnnotation(Autowired.class) != null && !typeUtil.isSubtype(field.asType(), iProvider)) {
                            // It must be field, then it has annotation, but it not be provider.
                            Autowired paramConfig = field.getAnnotation(Autowired.class);
                            paramsType.put(StringUtils.isEmpty(paramConfig.name()) ? field.getSimpleName().toString() : paramConfig.name(), TypeUtils.typeExchange(field.asType()));
                        }
                    }
                    // ACTIVITY类型节点
                    routeMete = new RouteMeta(route, element, RouteType.ACTIVITY, paramsType);
                } else if (typeUtil.isSubtype(tm, iProvider)) {         // IProvider
                    logger.info(">>> Found provider route: " + tm.toString() + " <<<");
                    //从该判断可看出，如果要想成功注册一个 PROVIDER 类型的路由节点，
                    //一定要实现 com.alibaba.android.arouter.facade.template.IProvider 这个接口
                    routeMete = new RouteMeta(route, element, RouteType.PROVIDER, null);
                } else if (typeUtil.isSubtype(tm, type_Service.asType())) {           // Service
                    logger.info(">>> Found service route: " + tm.toString() + " <<<");
                     //SERVICE类型节点
                    routeMete = new RouteMeta(route, element, RouteType.parse(SERVICE), null);
                } else if (types.isSubtype(tm, fragmentTm) || types.isSubtype(tm, fragmentTmV4)) {
                    logger.info(">>> Found fragment route: " + tm.toString() + " <<<");
                   //FRAGMENT类型节点
                    routeMete = new RouteMeta(route, element, RouteType.parse(FRAGMENT), null);
                }

                //routeMete包含了每个路由节点的各种信息，下面的方法的主要功能就是根据@Route注解信息对节点进行分组，保存在groupMap集合中。
               //关于方法的具体实现，后面会有解析
                categories(routeMete);

            }

            .........
        }
    }

```

以上代码主要功能就是遍历所有被@Route注解的元素，然后将每个路由节点的信息按照类型（ACTIVITY类型，实现了IProvider 接口类型以及SERVICE类型）封装到`RouteMeta`中，最后调用`categories(routeMete)`方法将节点分组，保存在`groupMap`集合。

继续往下分析

```
 private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {
            ........

             /*
              然后创建ARouter$$Providers$$xxx 类中的loadInto()方法
             @Override
             public void loadInto(Map<String, RouteMeta> providers) {}
             */
            MethodSpec.Builder loadIntoMethodOfProviderBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(providerParamSpec);

            //遍历分组的集合，生成相应的java文件
           //因为本文使用的例子没有对页面进行分组，所以只生成了一个组文件ARouter$$Group$$xxx
            for (Map.Entry<String, Set<RouteMeta>> entry : groupMap.entrySet()) {
                String groupName = entry.getKey();
               /*
                  创建ARouter$$Group$$xxx 类中的loadInto()方法
                 @Override
                 public void loadInto(Map<String, RouteMeta> atlas) {}
             */
                MethodSpec.Builder loadIntoMethodOfGroupBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(groupParamSpec);

                // 生成loadInto()方法体
                Set<RouteMeta> groupData = entry.getValue();
                //遍历每个组里面的路由节点
                for (RouteMeta routeMeta : groupData) {
                    switch (routeMeta.getType()) {
                        //如果节点类型是PROVIDER，
                        case PROVIDER:  
                          //获取路由节点元素的接口集合
                            List<? extends TypeMirror> interfaces = ((TypeElement) routeMeta.getRawType()).getInterfaces();
                            for (TypeMirror tm : interfaces) {
                             if (types.isSameType(tm, iProvider)) {   // Its implements iProvider interface himself.
                                   //路由节点元素其中一个接口是 com.alibaba.android.arouter.facade.template.IProvider 
                                  //给ARouter$$Providers$$xxx 类中的loadInto()添加方法体
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                            "providers.put($S, $T.build($T." + routeMeta.getType() + ", $T.class, $S, $S, null, " + routeMeta.getPriority() + ", " + routeMeta.getExtra() + "))",
                                            (routeMeta.getRawType()).toString(),//路由节点元素的全名
                                            routeMetaCn,
                                            routeTypeCn,
                                            ClassName.get((TypeElement) routeMeta.getRawType()),
                                            routeMeta.getPath(),
                                            routeMeta.getGroup());
                                } else if (types.isSubtype(tm, iProvider)) {
                                   //路由节点元素其中一个接口是com.alibaba.android.arouter.facade.template.IProvider 接口的子类型
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                            "providers.put($S, $T.build($T." + routeMeta.getType() + ", $T.class, $S, $S, null, " + routeMeta.getPriority() + ", " + routeMeta.getExtra() + "))",
                                            tm.toString(),   //IProvider子类型的全名
                                            routeMetaCn,
                                            routeTypeCn,
                                            ClassName.get((TypeElement) routeMeta.getRawType()),
                                            routeMeta.getPath(),
                                            routeMeta.getGroup());
                                }
                            //上面方法体的代码为：
                          //providers.put("实现接口的名称", RouteMeta.build(RouteType.PROVIDER, 类名.class,   "@Route.path", "@Route.group", null, @Route.priority, @Route.extras));
                            }
                            break;
                        default:
                            break;
                    }

                    // 将路由节点中被@Autowired注解的属性集合转换成字符串
                    StringBuilder mapBodyBuilder = new StringBuilder();
                    //获取路由节点中被@Autowired注解的属性集合
                    Map<String, Integer> paramsType = routeMeta.getParamsType();
                    if (MapUtils.isNotEmpty(paramsType)) {
                        for (Map.Entry<String, Integer> types : paramsType.entrySet()) {
                            mapBodyBuilder.append("put(\"").append(types.getKey()).append("\", ").append(types.getValue()).append("); ");
                        }
                    }
                    String mapBody = mapBodyBuilder.toString();

                    //给ARouter$$Group$$xxx 类中的loadInto()添加方法体
                    //注意：有多个分组就会创建多个组文件
                    loadIntoMethodOfGroupBuilder.addStatement(
                            "atlas.put($S, $T.build($T." + routeMeta.getType() + ", $T.class, $S, $S, " + (StringUtils.isEmpty(mapBody) ? null : ("new java.util.HashMap<String, Integer>(){{" + mapBodyBuilder.toString() + "}}")) + ", " + routeMeta.getPriority() + ", " + routeMeta.getExtra() + "))",
                            routeMeta.getPath(),
                            routeMetaCn,
                            routeTypeCn,
                            ClassName.get((TypeElement) routeMeta.getRawType()),
                            routeMeta.getPath().toLowerCase(),
                            routeMeta.getGroup().toLowerCase());
                }

                  // 真正生成ARouter$$Group$$test JAVA文件
                 //NAME_OF_GROUP = ARouter$$Group$$
                //  groupName = test; 关于groupname的值在方法categories(routeMete)中会有讲解
                String groupFileName = NAME_OF_GROUP + groupName;
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(groupFileName)
                                .addJavadoc(WARNING_TIPS)
                                .addSuperinterface(ClassName.get(type_IRouteGroup))
                                .addModifiers(PUBLIC)
                                .addMethod(loadIntoMethodOfGroupBuilder.build())
                                .build()
                ).build().writeTo(mFiler);

                logger.info(">>> Generated group: " + groupName + "<<<");
                //将生成的组文件放在rootmap集合中去，为下面生成ARouter$$Root$$xxx文件做准备
                rootMap.put(groupName, groupFileName);
            }

         .......
        }
    }

```

以上代码主要功能由几点：

*   遍历`groupmap`集合给ARouter$$Group$$xxx类中的`loadInto()`添加方法体，并且生成ARouter$$Group$$xxx JAVA文件，而文件命名为ARouter$$Group$$+groupname，其中有多个分组就会创建多个组文件。比如`AROUTER`源码中的样例就生成了多个分组文件

![](https://upload-images.jianshu.io/upload_images/5994347-bd709da910e3a722.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/592)

两个分组文件

关于生成的`loadInto()`中的方法体的例子，来自 `AROUTER`源码中的样例：

```
public class ARouter$$Group$$test implements IRouteGroup {
  @Override
  public void loadInto(Map<String, RouteMeta> atlas) {
    //存在被@Autowired注解参数生成的代码
    atlas.put("/test/activity1", RouteMeta.build(RouteType.ACTIVITY, Test1Activity.class, "/test/activity1", "test", new java.util.HashMap<String, Integer>(){{put("name", 18); put("boy", 0); put("age", 3); put("url", 18); }}, -1, -2147483648));
    .....
   //没有被@Autowired注解参数生成的代码
    atlas.put("/test/activity4", RouteMeta.build(RouteType.ACTIVITY, Test4Activity.class, "/test/activity4", "test", null, -1, -2147483648));
    ....
  }
}

```

*   遍历每个组里面的路由节点，查找节点类型是否为PROVIDER类型，如果是就向给ARouter$$Providers$$xxx类中的`loadInto()`添加方法，其文件命名ARouter$$Providers$$+modulename。关于生成的`loadInto()`中的方法体的例子，来自 AROUTER源码中的样例：

```
public class ARouter$$Providers$$app implements IProviderGroup {
  @Override
  public void loadInto(Map<String, RouteMeta> providers) {
    providers.put("com.alibaba.android.arouter.demo.testservice.HelloService", RouteMeta.build(RouteType.PROVIDER, HelloServiceImpl.class, "/service/hello", "service", null, -1, -2147483648));
    //路由节点元素其中一个接口是IProvider的子类型
    providers.put("com.alibaba.android.arouter.facade.service.SerializationService", RouteMeta.build(RouteType.PROVIDER, JsonServiceImpl.class, "/service/json", "service", null, -1, -2147483648));
     //路由节点元素其中一个接口是IProvider接口
    providers.put("com.alibaba.android.arouter.demo.testservice.SingleService", RouteMeta.build(RouteType.PROVIDER, SingleService.class, "/service/single", "service", null, -1, -2147483648));
  }
}

```

*   将生成的组文件放在rootmap集合中去，为下面生成ARouter$$Root$$xxx文件做准备，其文件命名ARouter$$Root$$+modulename。

我们接着分析`parseRoutes()`方法最后一段代码，这段代码其实很简单，主要目的就是给ARouter$$Root$$xxx的`loadInto()`添加方法体，最后生成Router$$Providers$$xxx，ARouter$$Root$$xxx文件

```
 private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {
            ........
            //遍历rootMap集合，给ARouter$$Root$$xxx的`loadInto()`添加方法体
            if (MapUtils.isNotEmpty(rootMap)) {
                // Generate root meta by group name, it must be generated before root, then I can findout the class of group.
                for (Map.Entry<String, String> entry : rootMap.entrySet()) {
                    loadIntoMethodOfRootBuilder.addStatement("routes.put($S, $T.class)", entry.getKey(), ClassName.get(PACKAGE_OF_GENERATE_FILE, entry.getValue()));
                }
            }

            // 生成Router$$Providers$$xxx文件
            String providerMapFileName = NAME_OF_PROVIDER + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(providerMapFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(type_IProviderGroup))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfProviderBuilder.build())
                            .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated provider map, name is " + providerMapFileName + " <<<");

            // 生成ARouter$$Root$$xxx文件
            String rootFileName = NAME_OF_ROOT + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(rootFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(elementUtil.getTypeElement(ITROUTE_ROOT)))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfRootBuilder.build())
                            .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated root, name is " + rootFileName + " <<<");
        }
    }

```

关于生成的`loadInto()`中的方法体的例子，来自 AROUTER源码中的样例：

```
public class ARouter$$Root$$app implements IRouteRoot {
  @Override
  public void loadInto(Map<String, Class<? extends IRouteGroup>> routes) {
    routes.put("service", ARouter$$Group$$service.class);
    routes.put("test", ARouter$$Group$$test.class);
  }
}

```

**上面分析的便是`parseRoutes()`方法所有代码的解析**

3.最后我们看下`categories()`方法是如何分组的

```
   private void categories(RouteMeta routeMete) {
        //如果路由路径合法，且有groupname进行执行
        if (routeVerify(routeMete)) {
            logger.info(">>> Start categories, group = " + routeMete.getGroup() + ", path = " + routeMete.getPath() + " <<<");
             //根据groupname获取该组的路由节点集合，如果集合为空，则创建一个新的组，将该节点添加进去，并将组集合保存在groupmap中；
          //不为空，则添加到所属的组集合中去
            Set<RouteMeta> routeMetas = groupMap.get(routeMete.getGroup());
            if (CollectionUtils.isEmpty(routeMetas)) {
                Set<RouteMeta> routeMetaSet = new TreeSet<>(new Comparator<RouteMeta>() {
                    @Override
                    public int compare(RouteMeta r1, RouteMeta r2) {
                        try {
                            return r1.getPath().compareTo(r2.getPath());
                        } catch (NullPointerException npe) {
                            logger.error(npe.getMessage());
                            return 0;
                        }
                    }
                });
                routeMetaSet.add(routeMete);
                groupMap.put(routeMete.getGroup(), routeMetaSet);
            } else {
                routeMetas.add(routeMete);
            }
        } else {
            logger.warning(">>> Route meta verify error, group is " + routeMete.getGroup() + " <<<");
        }
    }

//判断路由路径是否合法，并且设置groupname
 private boolean routeVerify(RouteMeta meta) {
        String path = meta.getPath();
        //如果路径为空，或者不是由'/'开头，返回false
        if (StringUtils.isEmpty(path) || !path.startsWith("/")) {   // The path must be start with '/' and not empty!
            return false;
        }

         //如果在@Route注解中没有设置group标识，那么就默认取path路径第一段路径名作为groupname
        if (StringUtils.isEmpty(meta.getGroup())) { // Use default group(the first word in path)
            try {
                String defaultGroup = path.substring(1, path.indexOf("/", 1));
                if (StringUtils.isEmpty(defaultGroup)) {
                    return false;
                }

                meta.setGroup(defaultGroup);
                return true;
            } catch (Exception e) {
                logger.error("Failed to extract default group! " + e.getMessage());
                return false;
            }
        }

        return true;
    }

```

通过分析，如果@Route注解中有设置group标识，作为groupname，如果没有就取/xxx1/xxx2，xxx1作为groupname，并将同一组的路由节点放到同一个集合中去。

至此关于`@Route`注解在编译期时生成ARouter$$Root$$xxx，Router$$Providers$$xxx，ARouter$$Group$$xxx三种映射文件的源码分析完毕。

###### 2.ARouter初始化过程

ARouter经过代码编译后，生成了相应的映射文件，我们可以断定，ARouter 的初始化会将这些文件加载到内存中去，形成一个路由表，以供后面路由查找跳转之用。其相关源码可参见 [arouter-api](https://link.jianshu.com?t=https://github.com/alibaba/ARouter/tree/master/arouter-api)

*   `ARouter`的`init()`方法

```
public static void init(Application application) {
        if (!hasInit) {
            logger = _ARouter.logger;
            _ARouter.logger.info(Consts.TAG, "ARouter init start.");
            hasInit = _ARouter.init(application);

            if (hasInit) {
                _ARouter.afterInit();
            }

            _ARouter.logger.info(Consts.TAG, "ARouter init over.");
        }
    }

```

由上面代码可以看出，其初始化实际上是调用了`_ARouter` 的 `init ()`方法，而且其他的跳转方法最终调用的也是`_ARouter` 种的方法。

*   `_ARouter`的`init()`方法

```
  protected static synchronized boolean init(Application application) {
        mContext = application;
        LogisticsCenter.init(mContext, executor);
        logger.info(Consts.TAG, "ARouter init success!");
        hasInit = true;

        return true;
    }

```

`_ARouter`中又调用了`LogisticsCenter.init()`，继续追踪下去，其中传入了一个线程池`executor`，这个线程池在拦截器的时候会使用到。

```
    public synchronized static void init(Context context, ThreadPoolExecutor tpe) throws HandlerException {
        mContext = context;
        executor = tpe;

        try {
             //ROUTE_ROOT_PAKCAGE = "com.alibaba.android.arouter.routes"
            // 获取ROUTE_ROOT_PAKCAGE 包里面的所有文件
            List<String> classFileNames = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE);

            //遍历所有ROUTE_ROOT_PAKCAGE 包里的文件
            for (String className : classFileNames) {
                //文件名以“com.alibaba.android.arouter.routes.ARouter$$Root”开头执行下面代码
                if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                    // 通过反射实例化，并且调用loadInto()，目的即是将编译生成的ARouter$$Group$$xxx文件加载到内存中，保存在Warehouse.groupsIndex；
                    ((IRouteRoot) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.groupsIndex);
                } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_INTERCEPTORS)) {
                    //文件名以“com.alibaba.android.arouter.routes.ARouter$$Interceptors”开头执行下面代码
                    //  执行编译生成的ARouter$$Interceptors$$xxx的loadInto()，将自定义拦截器类存放在Warehouse.interceptorsIndex中
                    ((IInterceptorGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.interceptorsIndex);
                } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
                     //文件名以“com.alibaba.android.arouter.routes.ARouter$$Providers”开头执行下面代码
                   //  执行编译生成的ARouter$$Interceptors$$xxx的loadInto()
                    ((IProviderGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.providersIndex);
                }
            }

            if (Warehouse.groupsIndex.size() == 0) {
                logger.error(TAG, "No mapping files were found, check your configuration please!");
            }

            if (ARouter.debuggable()) {
                logger.debug(TAG, String.format(Locale.getDefault(), "LogisticsCenter has already been loaded, GroupIndex[%d], InterceptorIndex[%d], ProviderIndex[%d]", Warehouse.groupsIndex.size(), Warehouse.interceptorsIndex.size(), Warehouse.providersIndex.size()));
            }
        } catch (Exception e) {
            throw new HandlerException(TAG + "ARouter init logistics center exception! [" + e.getMessage() + "]");
        }
    }

```

*   `_ARouter`的`afterInit()`方法

```
static void afterInit() {
        // 通过路由机制，初始化路由拦截机制。关于路由拦截机制的使用和原理，后续文章会有分析
        interceptorService = (InterceptorService) ARouter.getInstance().build("/arouter/service/interceptor").navigation();
    }

```

以上就是ARouter初始化的所有代码，关于如何查找到`com.alibaba.android.arouter.routes`包内所有文件这里便不做过多分析，大家可以去阅读 [arouter-api](https://link.jianshu.com?t=https://github.com/alibaba/ARouter/tree/master/arouter-api)中`ClassUtils`这个类的源码。
**总结下来，其实ARouter 的初始化只做了一件事，找到自己编译期产生的清单文件，把 Group 、Interceptor 、Provider 三种清单加载到 Warehouse 内存仓库中。**即下面这些文件，来源自AROUTER源码中的样例

![](https://upload-images.jianshu.io/upload_images/5994347-c375d8bc320ad241.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/518)

值得注意的是，在初始化阶段，ARouter 仅载入了 Group 清单，并没有具体载入每个 Group 中包含的具体的路由节点清单，只有当使用到具体的 Group 时，才会加载对应的 Group 列表。这种分组管理，按需加载，大大的降低了初始化时的内存压力。并且`Warehouse`类中保存了路由清单，并且将使用过的路由对象缓存起来，之后查找都是直接使用缓存的对象 。

###### 3.ARouter调用过程分析

页面跳转最基本方法

> ARouter.getInstance().build("/test/activity2").navigation();

获取Provider服务（实现了IProvider接口以及IProvider子类接口的服务类）的方法有两种：

> 1.byName方式
> ARouter.getInstance().build("/service/hello").navigation()

> 2.byType方式
> ARouter.getInstance().navigation(HelloService.class)

ARouter路由跳转采用链式调用，`ARouter.getInstance()`其中采用的单例模式，获取ARouter的实例，这个就不作过多分析，主要分析`build()`和`navigation()`。

**build()方法**
ARouter的`build(String path)`和`init()`方法一样，调用的是`_ARouter`的`build(String path)`方法。

```
  protected Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new HandlerException(Consts.TAG + "Parameter is invalid!");
        } else {
            PathReplaceService pService = ARouter.getInstance().navigation(PathReplaceService.class);
            if (null != pService) {
                path = pService.forString(path);
            }
            return build(path, extractGroup(path));
        }
    }

```

其中`extractGroup(String path)`就是根据path获取分组名，即path第一段“/”符号之间的值

```
  private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new HandlerException(Consts.TAG + "Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }

        try {
            //    /xxx1/xxx2   ===>  defaulGroup = xxx1
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new HandlerException(Consts.TAG + "Extract the default group failed! There's nothing between 2 '/'!");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            logger.warning(Consts.TAG, "Failed to extract default group! " + e.getMessage());
            return null;
        }
    }

```

`build(String path)`方法最终调用的是`build(String path, String group)`

```
    protected Postcard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new HandlerException(Consts.TAG + "Parameter is invalid!");
        } else {
            PathReplaceService pService = ARouter.getInstance().navigation(PathReplaceService.class);
            if (null != pService) {
                path = pService.forString(path);
            }
            return new Postcard(path, group);
        }
    }

```

**值得注意的是其中`ARouter.getInstance().navigation(PathReplaceService.class)`就是得到实现`PathReplaceService`接口的一个服务对象，对原始path进行处理后，生成新的path路径。而这个类需要我们自己自定义去实现，如果没有实现，pService=null，原始path不做任何处理。**
下面是`PathReplaceService`接口，我们可以通过实现`forString()`和`forUri()`方法，对某些url进行替换处理，跳转到其他的目标页面。

```
public interface PathReplaceService extends IProvider {

    /**
     * For normal path.
     *
     * @param path raw path
     */
    String forString(String path);

    /**
     * For uri type.
     *
     * @param uri raw uri
     */
    Uri forUri(Uri uri);
}

```

最后返回一个`Postcard`实例对象，里面封装了路由节点的路径，分组等节点信息。其实`build()`方法的目的只有一个就是根据路由，封装成`Postcard`对象，其对象贯穿之后整个路由过程。Postcard 包含了众多的属性值，提供了路由过程中所有的控制变量。

```
public final class Postcard extends RouteMeta {
    private Uri uri;
    private Object tag;             // A tag prepare for some thing wrong.
    private Bundle mBundle;         // 传递的参数
    private int flags = -1;         // intent 的flag标志
    private int timeout = 300;      // Navigation timeout, TimeUnit.Second !
    private IProvider provider;     // IProvider服务对象
    private boolean greenChannal;
    private SerializationService serializationService;//序列化服务对象

     // 跳转动画
    private Bundle optionsCompat;    // The transition animation of activity
    private int enterAnim;
    private int exitAnim;

    // copy from RouteMeta 
    private RouteType type;         // 路由节点类型
    private Element rawType;        
    private Class<?> destination;  //需要跳转到的页面
    private String path;            // 路径
    private String group;           // 分组
    private int priority = -1;      // 优先级
    private int extra;              // 配置标识
    private Map<String, Integer> paramsType;  // 路由页面被@Autowired注解属性
    // ......
}

```

**navigation()方法**
关于页面跳转的`navigation()`方法有多个重载的方法，但最终都会调用`_ARouter`下面这个方法

```
    protected Object navigation(final Context context, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        try {
            //首先对postcard进行一些处理，设置postcard的destination，type，priority 等一些属性值，completion()后面会有分析
            LogisticsCenter.completion(postcard);
        } catch (NoRouteFoundException ex) {
            logger.warning(Consts.TAG, ex.getMessage());

            if (debuggable()) { // Show friendly tips for user.
                Toast.makeText(mContext, "There's no route matched!\n" +
                        " Path = [" + postcard.getPath() + "]\n" +
                        " Group = [" + postcard.getGroup() + "]", Toast.LENGTH_LONG).show();
            }
            // 如果处理postcard失败，通过 callback 回调失败结果
           // callback为空的情况下，如果有定义全局的降级处理（DegradeService），则使用全局处理
           //降级处理也需要我们自己实现DegradeService接口
            if (null != callback) {
                callback.onLost(postcard);
            } else {    // No callback for this invoke, then we use the global degrade service.
                DegradeService degradeService = ARouter.getInstance().navigation(DegradeService.class);
                if (null != degradeService) {
                    degradeService.onLost(context, postcard);
                }
            }

            return null;
        }
         //路由处理成功，回调callback.onFound()
        if (null != callback) {
            callback.onFound(postcard);
        }

        //目前来说，PROVIDER服务类型，以及FRAGMENT类型不需要通过拦截器外，其他类型均需要通过拦截器
        //关于拦截器相关用法及原理分析在后续的文章中会讲解到，大家去可以关注下
        if (!postcard.isGreenChannel()) {   
            interceptorService.doInterceptions(postcard, new InterceptorCallback() {
                /**
                 * Continue process
                 *
                 * @param postcard route meta
                 */
                @Override
                public void onContinue(Postcard postcard) {
                    _navigation(context, postcard, requestCode, callback);
                }

                /**
                 * Interrupt process, pipeline will be destory when this method called.
                 *
                 * @param exception Reson of interrupt.
                 */
                @Override
                public void onInterrupt(Throwable exception) {
                    if (null != callback) {
                        callback.onInterrupt(postcard);
                    }

                    logger.info(Consts.TAG, "Navigation failed, termination by interceptor : " + exception.getMessage());
                }
            });
        } else {
            return _navigation(context, postcard, requestCode, callback);
        }

        return null;
    }

```

**值得注意的是，当跳转路由处理失败的时候，会获取一个降级服务，我们可以实现`DegradeService`接口，实现`onLost()`方法，对路由处理失败的情况进行处理，比如跳转到一个信息提示页面，让用户去更新版本等操作等。** 下面是`DegradeService`接口：

```
public interface DegradeService extends IProvider {

    /**
     * Router has lost.
     *
     * @param postcard meta
     */
    void onLost(Context context, Postcard postcard);
}

```

通过上面代码的分析，不管是否通过拦截器进行处理，最后都会调用`_navigation()`达到路由的目的：

```
private Object _navigation(final Context context, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        final Context currentContext = null == context ? mContext : context;

        switch (postcard.getType()) {
            case ACTIVITY:
                //下面就是最基本的使用intent进行activity进行跳转
                // 创建intent
                final Intent intent = new Intent(currentContext, postcard.getDestination());
                //设置传参
                intent.putExtras(postcard.getExtras());

                //activity启动标志
                int flags = postcard.getFlags();
                if (-1 != flags) {
                    intent.setFlags(flags);
                } else if (!(currentContext instanceof Activity)) {    // Non activity, need less one flag.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                // 在主线程中进行跳转
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //新版本带转场动画的启动方式
                        if (requestCode > 0) {  // Need start for result
                            ActivityCompat.startActivityForResult((Activity) currentContext, intent, requestCode, postcard.getOptionsBundle());
                        } else {
                            ActivityCompat.startActivity(currentContext, intent, postcard.getOptionsBundle());
                        }

                        if ((0 != postcard.getEnterAnim() || 0 != postcard.getExitAnim()) && currentContext instanceof Activity) {    // Old version.
                            //老版本的跳转动画
                            ((Activity) currentContext).overridePendingTransition(postcard.getEnterAnim(), postcard.getExitAnim());
                        }
                        //跳转成功，回调callback.onArrival()
                        if (null != callback) { // Navigation over.
                            callback.onArrival(postcard);
                        }
                    }
                });

                break;
            case PROVIDER:
                return postcard.getProvider();
            case BOARDCAST:
            case CONTENT_PROVIDER:
            case FRAGMENT:
                Class fragmentMeta = postcard.getDestination();
                try {
                     //实例化fragment，并传递参数
                    Object instance = fragmentMeta.getConstructor().newInstance();
                    if (instance instanceof Fragment) {
                        ((Fragment) instance).setArguments(postcard.getExtras());
                    } else if (instance instanceof android.support.v4.app.Fragment) {
                        ((android.support.v4.app.Fragment) instance).setArguments(postcard.getExtras());
                    }

                    return instance;
                } catch (Exception ex) {
                    logger.error(Consts.TAG, "Fetch fragment instance error, " + TextUtils.formatStackTrace(ex.getStackTrace()));
                }
            case METHOD:
            case SERVICE:
            default:
                return null;
        }

        return null;
    }

```

目前仅ARouter实现了 ACTIVITY ， PROVIDER ，FRAGMENT三种种类型。上面关于postcard的provider，destination的值都是在`completion()`中设置的。我们接着看`LogisticsCenter`的`completion(Postcard postcard)`。

```
    public synchronized static void completion(Postcard postcard) {
        if (null == postcard) {
            throw new NoRouteFoundException(TAG + "No postcard!");
        }

        // 查找Warehouse仓库的路由节点缓存，看是否已在缓存中
        RouteMeta routeMeta = Warehouse.routes.get(postcard.getPath());
        if (null == routeMeta) {   
          // 如果没有，查找仓库的组别清单中是否存在该组别，组别清单已经在初始化的时候加载到仓库中去了
            Class<? extends IRouteGroup> groupMeta = Warehouse.groupsIndex.get(postcard.getGroup());  
            //如果没有抛出异常
            if (null == groupMeta) {
                throw new NoRouteFoundException(TAG + "There is no route match the path [" + postcard.getPath() + "], in group [" + postcard.getGroup() + "]");
            } else {
                // Load route and cache it into memory, then delete from metas.
                try {
                    if (ARouter.debuggable()) {
                        logger.debug(TAG, String.format(Locale.getDefault(), "The group [%s] starts loading, trigger by [%s]", postcard.getGroup(), postcard.getPath()));
                    }
                    // 实例化个组别的类，调用loadInto()，将组别中所有的路由节点加载进仓库Warehouse.routes，缓存
                    IRouteGroup iGroupInstance = groupMeta.getConstructor().newInstance();
                    iGroupInstance.loadInto(Warehouse.routes);
                     // 从组别清单中删除已加载的组别，防止重复加载
                    Warehouse.groupsIndex.remove(postcard.getGroup());

                    if (ARouter.debuggable()) {
                        logger.debug(TAG, String.format(Locale.getDefault(), "The group [%s] has already been loaded, trigger by [%s]", postcard.getGroup(), postcard.getPath()));
                    }
                } catch (Exception e) {
                    throw new HandlerException(TAG + "Fatal exception when loading group meta. [" + e.getMessage() + "]");
                }
                //当路由节点加载到缓存中去后，重新查找执行else代码，对postcard进行处理
                completion(postcard);   // Reload
            }
        } else {
            //给postcard设置destination,type,priority等值，供上面讲解到的_navigation()进行使用
            // 其中routeMeta是在ARouter$$Group$$xxx的loadInto中创建的
            postcard.setDestination(routeMeta.getDestination());
            postcard.setType(routeMeta.getType());
            postcard.setPriority(routeMeta.getPriority());
            postcard.setExtra(routeMeta.getExtra());

            //如果通过build(Uri url) 进行跳转的话 通过解析url ，将传参保存进bundle中
            Uri rawUri = postcard.getUri();
            if (null != rawUri) {  
                //splitQueryParameters()就是在uri中携带的参数进行解析
                Map<String, String> resultMap = TextUtils.splitQueryParameters(rawUri);
                Map<String, Integer> paramsType = routeMeta.getParamsType();

                if (MapUtils.isNotEmpty(paramsType)) {
                    // Set value by its type, just for params which annotation by @Param
                    for (Map.Entry<String, Integer> params : paramsType.entrySet()) {
                        setValue(postcard,
                                params.getValue(),
                                params.getKey(),
                                resultMap.get(params.getKey()));
                    }

                    // Save params name which need autoinject.
                    postcard.getExtras().putStringArray(ARouter.AUTO_INJECT, paramsType.keySet().toArray(new String[]{}));
                }

                // Save raw uri
                postcard.withString(ARouter.RAW_URI, rawUri.toString());
            }

            //从这里也可以看出PROVIDER，FRAGMENT不需要通过拦截器
            switch (routeMeta.getType()) {
                case PROVIDER:  
                    // 如果是PROVIDER节点类型，从服务节点列表中获取，如果没有，则实例化，并保存在服务节点列表Warehouse.providers中
                  //并将实例化的对象设置给postcard的provider属性
                    Class<? extends IProvider> providerMeta = (Class<? extends IProvider>) routeMeta.getDestination();
                    IProvider instance = Warehouse.providers.get(providerMeta);
                    if (null == instance) { // There's no instance of this provider
                        IProvider provider;
                        try {
                            provider = providerMeta.getConstructor().newInstance();
                            provider.init(mContext);
                            Warehouse.providers.put(providerMeta, provider);
                            instance = provider;
                        } catch (Exception e) {
                            throw new HandlerException("Init provider failed! " + e.getMessage());
                        }
                    }
                    postcard.setProvider(instance);
                    postcard.greenChannel();    // Provider should skip all of interceptors
                    break;
                case FRAGMENT:
                    postcard.greenChannel();    // Fragment needn't interceptors
                default:
                    break;
            }
        }
    }

```

分析到这里，关于页面基本跳转的原理分析就已经结束了。最后就是关于获取Provider服务两种方法的源码分析。其中byName方式，和页面跳转是一模一样的。我们只需要看看byType方式即可。byType方式最后调用的是`_ARouter`的`navigation(Class<? extends T> service)`

```
  protected <T> T navigation(Class<? extends T> service) {
        try {
            // 通过 className 获取 Postcard 对象
            Postcard postcard = LogisticsCenter.buildProvider(service.getName());

            // 兼容1.0.5 compiler sdk版本.
            if (null == postcard) { // No service, or this service in old version.
                postcard = LogisticsCenter.buildProvider(service.getSimpleName());
            }
           // 对 Postcard 对象进行处理
            LogisticsCenter.completion(postcard);
             //返回 Postcard 中的 provider 属性值
            return (T) postcard.getProvider();
        } catch (NoRouteFoundException ex) {
            logger.warning(Consts.TAG, ex.getMessage());
            return null;
        }
    }

```

上面代码中的`completion()`方法之前已经分析过了，只需要看下`LogisticsCenter.buildProvider(service.getName())`即可。

```
  public static Postcard buildProvider(String serviceName) {
        RouteMeta meta = Warehouse.providersIndex.get(serviceName);

        if (null == meta) {
            return null;
        } else {
            return new Postcard(meta.getPath(), meta.getGroup());
        }
    }

```

这个方法非常的简单，就是根据服务类名去仓库Warehouse.providersIndex中获去路由节点元素，然后封装在Postcard对象中。服务类清单列表Warehouse.providersIndex中的值是在初始化时缓存的。**值得注意的是，PROVIDER 类型的路由节点既存在于对应的分组中，也存在于服务类清单列表中。所以，ARouter 可通过byType，byName两种方式来获取**。

##### 补充

关于ARouter的基本用法上面只有最基本跳转的介绍，下面对其他一些基本使用进行下补充

*   **带参数跳转**

```
//1.传递参数
 ARouter.getInstance().build("/test/activity1")
                        .withString("name", "老王")
                        .withInt("age", 18)
                        .withBoolean("boy", true)
                        .withLong("high", 180)
                        .withString("url", "https://a.b.c")
                        .withParcelable("pac", testParcelable)
                        .withObject("obj", testObj)
                        .navigation();

//2.直接传递Bundle
  Bundle params = new Bundle();
  ARouter.getInstance()
          .build("/test/activity1")
          .with(params)
          .navigation();

```

这些传参都是保存在生成的`postcard`对象中的`mBundle`属性里，然后在跳转的时候通过`intent.putExtras(postcard.getExtras())`达到传送参数的目的。
值得注意的是，关于对象的传递有两种，一种是`withParcelable()`方法，不过此方法需要传递的对象实现`Parcelable`接口，达到序列化的目的；另外一种是`withObject()`方法，此方法的原理是将实体类转换成json字符串，通过String的方式进行传递，而且使用这种方式需要实现 SerializationService，并使用@Route注解标注，下面是ARouter样例：

```
@Route(path = "/service/json")
public class JsonServiceImpl implements SerializationService {
    @Override
    public void init(Context context) {

    }

    @Override
    public <T> T json2Object(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    @Override
    public String object2Json(Object instance) {
        return JSON.toJSONString(instance);
    }
}

```

而且，需要在跳转到的页面获取`JsonServiceImpl`服务，将json字符串转换成对象。

```
SerializationService serializationService = ARouter.getInstance().navigation(SerializationService.class);
TestObj obj = serializationService.json2Object(getIntent().getString("obj"), TestObj.class);

```

*   **带返回结果跳转**

```
ARouter.getInstance().build("/test/activity2").navigation(this, 666);

```

值得注意的是，这时候的 `navigation`需要传递activit和requestCode。

*   **获取Fragment的实例**

定义一个fragment

```
@Route(path = "/test/fragment")
public class BlankFragment extends Fragment {
    public BlankFragment() {
        //必须要一个空的构造器
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        return textView;
    }

}

```

获取frament

```
Fragment fragment = (Fragment) ARouter.getInstance().build("/test/fragment").navigation();

```

*   **带转场动画跳转**

```
// 转场动画(常规方式)
 ARouter.getInstance() .build("/test/activity2")
                      .withTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                      .navigation(this);

// 转场动画(API16+)
 ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(v, v.getWidth() / 2, v.getHeight() / 2, 0, 0);
ARouter.getInstance().build("/test/activity2").withOptionsCompat(compat) .navigation();

```

*   **获取服务**

服务是全局单例的，只有在第一次使用到的时候才会被初始化。
暴露服务，必须实现IProvider 接口 或者其子类型

```
// 声明接口,其他组件通过接口来调用服务
public interface HelloService extends IProvider {
    String sayHello(String name);
}

// 实现接口
@Route(path = "/service/hello", name = "测试服务")
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
    return "hello, " + name;
    }

    @Override
    public void init(Context context) {

    }
}

```

获取服务

```
//bytype
HelloService helloService1 = ARouter.getInstance().navigation(HelloService.class);
//byname
HelloService helloService2 = (HelloService) ARouter.getInstance().build("/service/hello").navigation();

```

*   **多模块结构**

![](https://upload-images.jianshu.io/upload_images/5994347-767a114aa4746c87.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

app中可能存在多个模块，每个模块下面都有一个root结点，每个root结点都会管理整个模块中的group节点，每个group结点则包含了该分组下的所有页面，而每个模块允许存在多个分组，每个模块中都会有一个拦截器节点就是Interceptor结点，除此之外每个模块还会有控制拦截反转的provider结点

##### 最后

到此，关于ARouter的基本用法以及原理分析的就全部结束了，如果有不清楚或者错误的地方，希望各位同学指出。关于ARouter拦截器，各种服务，依赖注入等更多进阶用法及源码分析会更新在后续的文章。

作者：time_fly
链接：https://www.jianshu.com/p/46d174f37e82
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。