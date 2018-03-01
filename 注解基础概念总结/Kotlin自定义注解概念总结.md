## Kotlin注解

### 注解声明

注解是一种将元数据附加到代码中的方法。声明注解需要在类前面使用 annotation 关键字：

```kotlin
annotation class fancy
```
注解的附加属性可以通过用元注解标注注解类来指定：
- @Target  指定可以用 该注解标注的元素的可能的类型（类、函数、属性、表达式等）；
- @Retention  指定该注解是否 存储在编译后的 class 文件中，以及它在运行时能否通过反
射可见 （默认都是 true）；
- @Repeatable  允许 在单个元素上多次使用相同的该注解；
- @MustBeDocumented  指定 该注解是公有 API 的一部分，并且应该包含在 生成的 API 文档
中显示的类或方法的签名中。
```kotlin
@Target(AnnotationTarget.CLASS,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.VALUE_PARAMETER,
	AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Fancy
```

### 用法

```
@Fancy class Foo {
    @fancy fun baz(@fancy foo: Int): Int {
        return (@fancy 1)
    }
}
```
如果需要对类的主构造函数进行标注，则需要在构造函数声明中添加  constructor  关键字 ，
并将注解添加到其前面：
```kotlin
class Foo @Inject constructor(dependency: MyDependency) {
    // ……
}
```
你也可以标注属性访问器：
```kotlin
class Foo {
var x: MyDependency? = null
    @Inject set
}
```
### 构造函数
注解可以有接受参数的构造函数。
```kotlin
annotation class Special(val why: String)
@Special("example") class Foo {}
```
### 允许的参数类型有：
- 对应于 Java 原生类型的类型（Int、 Long等）；
- 字符串；
- 类（ Foo::class  ）；
- 枚举；
- 其他注解；
- 上面已列类型的数组。
注解参数不能有可空类型，因为 JVM 不支持将  null  作为 注解属性的值存储。
如果注解用作另一个注解的参数，则其名称不以 @ 字符为前缀：

```kotlin
annotation class ReplaceWith(val expression: String)
annotation class Deprecated(
	val message: String,
	val replaceWith: ReplaceWith = ReplaceWith(""))
@Deprecated("This function is deprecated, use === instead",ReplaceWith("this === other"))
```
如果需要将一个类指定为注解的参数，请使用 Kotlin 类 （KClass）。Kotlin 编译器会 自动将
其转换为 Java 类，以便 Java 代码能够正常看到该注解和参数 。
```kotlin
import kotlin.reflect.KClass
annotation class Ann(val arg1: KClass<*>, val arg2: KClass<out Any?>)
@Ann(String::class, Int::class) class MyClass
```
### Lambda 表达式
注解也可以用于 lambda 表达式。它们会被应用于生成 lambda 表达式体的  invoke()  方法上。这对于像 Quasar 这样的框架很有用， 该框架使用注解进行并发控制。
```kotlin
annotation class Suspendable
val f = @Suspendable { Fiber.sleep(10) }
```
### 注解使用处目标
当对属性或主构造函数参数进行标注时，从相应的 Kotlin 元素 生成的 Java 元素会有多个，因此在生成的 Java 字节码中该注解有多个可能位置 。如果要指定精确地指定应该如何生成该注解，请使用以下语法：
```kotlin
class Example(@field:Ann val foo, // 标注 Java 字段
	@get:Ann val bar, // 标注 Java getter
	@param:Ann val quux) // 标注 Java 构造函数参数
```
可以使用相同的语法来标注整个文件。 要做到这一点，把带有目标  file  的注解放在 文件的顶层、package 指令之前或者在所有导入之前（如果文件在默认包中的话）：
```kotlin
@file:JvmName("Foo")
package org.jetbrains.demo
```
如果你对同一目标有多个注解，那么可以这样来避免目标重复——在目标后面添加方括号 并
将所有注解放在方括号内：
```kotlin
class Example {
    @set:[Inject VisibleForTesting]
    var collaborator: Collaborator
}
```
支持的使用处目标的完整列表为：
- file
- property  （具有此目标的注解对 Java 不可见）
- field
- get  （属性 getter）
- set  （属性 setter）
- receiver  （扩展函数或属性的接收者参数）
- param  （构造函数参数）
- setparam  （属性 setter 参数）
- delegate  （为委托属性存储其委托实例的字段）

要标注扩展函数的接收者参数，请使用以下语法：
```kotlin
fun @receiver:Fancy String.myExtension() { }
```
如果不指定使用处目标，则根据正在使用的注解的  @Target  注解来选择目标 。如果有多个适用的目标，则使用以下列表中的第一个适用目标：
- param
- property
- field

## Java 注解
Java 注解与 Kotlin 100% 兼容：

