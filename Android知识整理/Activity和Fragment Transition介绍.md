这篇文章是对n Android 5.0 中Activity和Fragment transition API的一个总体回顾，这是关于transition系列文章的第一篇。

第一章: Activity和Fragment Transition介绍

第二章: 深入理解内容变换（Content Transition）

第三章上: 深入理解共享元素变换（Shared Element Transition）

第三章下: Shared Element Transitions In Practice (即将发布)

第四章: Activity & Fragment Transition Examples (即将发布)

我们以回答什么是Transition作为文章的开头。

什么是Transition?
安卓5.0中Activity和Fragment 变换是建立在名叫Transitions的安卓新特性之上的。这个诞生于4.4的transition框架为在不同的UI状态之间产生动画效果提供了非常方便的API。该框架主要基于两个概念：场景（scenes）和变换（transitions）。场景（scenes）定义了当前的UI状态，变换（transitions）则定义了在不同场景之间动画变化的过程。虽然transition翻译为变换似乎很确切，但是总觉得还是没有直接使用transition直观，为了更好的理解下面个别地方直接用transition代表变换。

当一个场景改变的时候，transition主要负责：

（1）捕捉每个View在开始场景和结束场景时的状态。

（2）根据两个场景（开始和结束）之间的区别创建一个Animator。


考虑这样一个例子，当用户点击屏幕，让activity中的view逐渐消失。使用安卓的transition框架，我们只需几行代码就可完成，如下：

```
public class ExampleActivity extends Activity implements View.OnClickListener {
    private ViewGroup mRootView;
    private View mRedBox, mGreenBox, mBlueBox, mBlackBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = (ViewGroup) findViewById(R.id.layout_root_view);
        mRootView.setOnClickListener(this);
        mRedBox = findViewById(R.id.red_box);
        mGreenBox = findViewById(R.id.green_box);
        mBlueBox = findViewById(R.id.blue_box);
        mBlackBox = findViewById(R.id.black_box);
    }
    @Override
    public void onClick(View v) {
        TransitionManager.beginDelayedTransition(mRootView, new Fade());
        toggleVisibility(mRedBox, mGreenBox, mBlueBox, mBlackBox);
    }
    private static void toggleVisibility(View... views) {
        for (View view : views) {
            boolean isVisible = view.getVisibility() == View.VISIBLE;
            view.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
```
为了更好的理解幕后发生的事情，让我们来一步一步的分析，假设最开始每个view都是可见的：

（1）当点击事件发生之后调用TransitionManager的beginDelayedTransition()方法，并且传递了mRootView和一个Fade对象最为参数。之后，framework会立即调用transition类的captureStartValues()方法为每个view保存其当前的可见状态(visibility)。

（2）当beginDelayedTransition返回之后，在上面的代码中将每个view设置为不可见。

（3）在接下来的显示中framework会调用transition类的captureEndValues()方法，记录每个view最新的可见状态。

（4）接着，framework调用transition的createAnimator()方法。transition会分析每个view的开始和结束时的数据发现view在开始时是可见的，结束时是不可见的。Fade（transition的子类）会利用这些信息创建一个用于把view的alpha属性变为0的AnimatorSet，并且将此AnimatorSet对象返回。

（5）framework会运行返回的Animator，导致所有的View都渐渐消失。

> 编者注：读者可以在这里回想假如不使用transition框架，我们自己使用属性动画（Animator）来实现是不是复杂很多，其实transition框架的作用就是封装了属性动画的操作。

这个简单的例子强调了transition框架的两个主要优点。第一、Transitions抽象和封装了属性动画，Animator的概念对开发者来说是透明的，因此它极大的精简了代码量。开发者所做的所有事情只是改变一下view前后的状态数据，Transition就会自动的根据状态的区别去生成动画效果。第二、不同场景之间变换的动画效果可以简单的通过使用不同的Transition类来改变，本例中用的是Fade。

![image](http://www.jcodecraeer.com/uploads/20150113/1421146073174536.gif)

实现上图中的那两个不同的动画效果可以将Fade替换成Slide或者Explode即可。在接下来的文章中你将会发现，这些优点将使得我们只用少量代码就可以创建复杂的Activity 和Fragment切换动画。在接下来的小节中，将看到是如何使用Lollipop的Activity 和Fragment transition API来实现这种变换的。

5.0中的Activity和Fragment Transition
Android 5.0中Transition可以被用来实现Activity或者Fragment切换时的异常复杂的动画效果。虽然在以前的版本中，已经可以使用Activity的overridePendingTransition() 和 FragmentTransaction的setCustomAnimation()来实现Activity或者Fragment的动画切换，但是他们仅仅局限与将整个视图一起动画变换。新的Lollipop api更进了一步，让单独的view也可以在进入或者退出其布局容器中时发生动画效果，甚至还可以在不同的activity/Fragment中共享一个view。

在开始讲解之前我们先做一些约定，虽然下面的约定是针对activity的，但是在Fragment中也是一样的约定。


**5.0中的Activity和Fragment Transition**

Android 5.0中Transition可以被用来实现Activity或者Fragment切换时的异常复杂的动画效果。虽然在以前的版本中，已经可以使用Activity的overridePendingTransition() 和 FragmentTransaction的setCustomAnimation()来实现Activity或者Fragment的动画切换，但是他们仅仅局限与将整个视图一起动画变换。新的Lollipop api更进了一步，让单独的view也可以在进入或者退出其布局容器中时发生动画效果，甚至还可以在不同的activity/Fragment中共享一个view。

在开始讲解之前我们先做一些约定，虽然下面的约定是针对activity的，但是在Fragment中也是一样的约定。

> A和B分别是两个Activity，假设activity A 调用activity B。将A代表调用Activity ，B代表被调用Activity。

> Activity transition API围绕退出（exit），进入（enter），返回（return）和再次进入（reenter）四种transition。按照上面对A和B的约定，我这样描述这一过程。

> Activity A的退出变换（exit transition）决定了在A调用B的时候，A中的View是如何播放动画的。

> Activity B的进入变换（enter transition）决定了在A调用B的时候，B中的View是如何播放动画的。

> Activity B的返回变换（return transition）决定了在B返回A的时候，B中的View是如何播放动画的。

> Activity A的再次进入变换（reenter transition）决定了在B返回A的时候，A中的View是如何播放动画的。

最后framework提供了两种Activity transition- 内容transition和共享元素的transition：

> A content transition determines how an activity's non-shared views—called transitioning views—enter or exit the activity scene.

> A shared element transition determines how an activity's shared elements (also called hero views) are animated between two activities.

![image](http://www.jcodecraeer.com/uploads/150113/1-150113195F54J.gif)

上图中演示了Google Play Newsstand 应用的效果，虽然我们无法查看它的源码，但是我敢打赌它用了以下的transition：

> activity A 中exit和reenter transition是为null的，因为A中的非共享view在退出和再次进入的时候没有动画效果。

> activity B中的enter content transition使用了自定义的slide-in变换。该变换使B中list的元素从下到上过度。

> activity B中return content transition是一组TransitionSet，同时播放了两个子元素的变换：上半部分和下半部分的slide变换。看起来就像整个界面被从从中间分割成了两半。

> enter and return 共享元素变换是用了ChangeImageTransform。让两个activity中的ImageView无缝切换。

你可能还注意到了在共享元素变换播放的同时还有个圆形水波效果的动画。我们将在以后的博客中讨论如何实现。目前为了是问题更简单，我们主要讨论熟悉的activity变换。

**Activity Transition API介绍**

用5.0的API创建一个基本的Activity transition是较为简单的。下面的总结是实现Activity transition的步骤。这篇文章主要是对Activity transition做简单的介绍，作为引入的篇章。在后续的文章中我们再介绍一些高级的用法。

1.在调用与被调用的activity中，通过设定==Window.FEATURE_ACTIVITY_TRANSITIONS==  和 ==Window.FEATURE_CONTENT_TRANSITIONS==来启用transition api ，可以通过代码也可以通过设置主题来启用：

代码方式，在setContentView之前调用：

```
getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
```
主题xml

```
<item name="android:windowContentTransitions">true</item>
```
2.分别在调用与被调用的activity中设置exit 和enter transition。Material主题默认会将exit的transition设置成null而enter的transition设置成Fade .如果reenter 或者 return transition没有明确设置，则将用exit 和enter的transition替代。

3.分别在调用与被调用的activity中设置exit 和enter 共享元素的transition。Material主题默认会将exit的共享元素transition设置成null而enter的共享元素transition设置成@android:transition/move.如果reenter 或者 return transition没有明确设置，则将用exit 和enter的共享元素transition替代。

开始一个activity的content transaction需要调用startActivity(Context, Bundle)方法，将下面的bundle作为第二个参数：


```
ActivityOptions.makeSceneTransitionAnimation(activity, pairs).toBundle();
```

其中pairs参数是一个数组：Pair<View, String> ，该数组列出了你想在activity之间共享的view和view的名称。别忘了给你的共享元素加上一个唯一的名称，否则transition可能不会有正确的结果。

4.在代码中触发通过finishAfterTransition()方法触发返回动画，而不是调用finish()方法。

5.默认情况下，material主题的应用中enter/return的content transition会在exit/reenter的content transitions结束之前开始播放（只是稍微早于），这样会看起来更加连贯。如果你想明确屏蔽这种行为，可以调用setWindowAllowEnterTransitionOverlap() 和 setWindowAllowReturnTransitionOverlap()方法。

**Fragment Transition API介绍**

如果你想在Fragment中使用transition，除了一小部分区别之外和activity大体一致：



1.Content的exit, enter, reenter, 和return transition需要调用fragment的相应方法来设置，或者通过fragment的xml属性来设置。

2.共享元素的enter和return transition也n需要调用fragment的相应方法来设置，或者通过fragment的xml属性来设置。

3.虽然在activity中transition是被startActivity()和finishAfterTransition()触发的，但是Fragment的transition却是在其被FragmentTransaction执行下列动作的时候自动发生的。added, removed, attached, detached, shown, ，hidden。

4.在Fragment commit之前，共享元素需要通过调用addSharedElement(View, String) 方法来成为FragmentTransaction的一部分。

总结
在本文中，我们仅仅是对新的Activitiy 和Fragment transition API做了一个简单的介绍。但是在接下来的文章中你会看到对基础概念的掌握会让你以后学习的更快，尤其是当你要做一些自定义Transition的时候。后面的文章中我们将更深入的去理解content transition和hared element transition，同时对Activity 和Fragment transition的工作原理有更深的理解。