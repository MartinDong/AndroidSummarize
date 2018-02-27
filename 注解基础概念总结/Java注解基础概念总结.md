**注解的概念**

注解（Annotation），也叫元数据（Metadata），是Java5的新特性，JDK5引入了Metadata很容易的就能够调用Annotations。注解与类、接口、枚举在同一个层次，并可以应用于包、类型、构造方法、方法、成员变量、参数、本地变量的声明中，用来对这些元素进行说明注释。

**注解的语法与定义形式**

（1）以@interface关键字定义
（2）注解包含成员，成员以无参数的方法的形式被声明。其方法名和返回值定义了该成员的名字和类型。
（3）成员赋值是通过@Annotation(name=value)的形式。
（4）注解需要标明注解的生命周期，注解的修饰目标等信息，这些信息是通过元注解实现。

以 **java.lang.annotation** 中定义的 **Target **注解来说明：

```java
@Retention(value = RetentionPolicy.RUNTIME)  
@Target(value = { ElementType.ANNOTATION_TYPE } )  
public @interface Target {  
    ElementType[] value();  
} 
```
源码分析如下：
第一：元注解@Retention，成员value的值为RetentionPolicy.RUNTIME。
第二：元注解@Target，成员value是个数组，用{}形式赋值，值为ElementType.ANNOTATION_TYPE
第三：成员名称为value，类型为ElementType[]
另外，需要注意一下，如果成员名称是value，在赋值过程中可以简写。如果成员类型为数组，但是只赋值一个元素，则也可以简写。如上面的简写形式为：

```java
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.ANNOTATION_TYPE)  
public @interface Target {  
    ElementType[] value();  
}  
```
**注解的分类**

注解的分类有两种分法：

**第一种分法**

1、基本内置注解，是指Java自带的几个Annotation，如@Override、Deprecated、@SuppressWarnings等；

2、元注解（meta-annotation），是指负责注解其他注解的注解，JDK 1.5及以后版本定义了4个标准的元注解类型，如下：

*   @Target 
*   @Retention 
*   @Documented 
*   @Inherited 

3、自定义注解，根据需要可以自定义注解，自定义注解需要用到上面的meta-annotation

**第二种分法**

注解需要标明注解的生命周期，这些信息是通过元注解 **@Retention** 实现，注解的值是 **enum **类型的 **RetentionPolicy**，包括以下几种情况：

```java
public enum RetentionPolicy {  
    /** 
     * 注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃. 
     * 这意味着：Annotation仅存在于编译器处理期间，编译器处理完之后，该Annotation就没用了 
     */  
    SOURCE,  
  
    /** 
     * 注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期. 
     */  
    CLASS,  
  
    /** 
     * 注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在， 
     * 保存到class对象中，可以通过反射来获取 
     */  
    RUNTIME  
}  
```

**元注解**

如上所介绍的Java定义了4个标准的元注解：

@Documented：标记注解，用于描述其它类型的注解应该被作为被标注的程序成员的公共API，因此可以被例如javadoc此类的工具文档化。

```java
@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.ANNOTATION_TYPE)  
public @interface Documented {  
} 
```
@Inherited：标记注解，允许子类继承父类的注解
```java
@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.ANNOTATION_TYPE)  
public @interface Inherited {  
}  
```
@Retention：指Annotation被保留的时间长短，标明注解的生命周期，3种**RetentionPolicy**取值含义上面以说明

```java
@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.ANNOTATION_TYPE)  
public @interface Retention {  
    RetentionPolicy value();  
}  
```
@Target：标明自定义注解的修饰目标，也就是说你写出来的这个注解要作用在什么位置上，共有
```java
@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.ANNOTATION_TYPE)  
public @interface Target {  
    ElementType[] value();  
}  
  
// ElementType取值  
public enum ElementType {  
    /** 类、接口（包括注解类型）或枚举 */  
    TYPE,  
    /** field属性，也包括enum常量使用的注解 */  
    FIELD,  
    /** 方法 */  
    METHOD,  
    /** 参数 */  
    PARAMETER,  
    /** 构造函数 */  
    CONSTRUCTOR,  
    /** 局部变量 */  
    LOCAL_VARIABLE,  
    /** 注解上使用的元注解 */  
    ANNOTATION_TYPE,  
    /** 包 */  
    PACKAGE  
}  
```