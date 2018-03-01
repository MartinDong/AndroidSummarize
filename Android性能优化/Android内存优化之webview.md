> 提起android端的webview，它既是天使，又是魔鬼。

_在混合型app中它是主角，一切由它呈现，如58同城，赶集网等；在另一些超级app中亦有它的影子，微信，qq，支付宝，没有一个超级app能少了它，既能展示最新最潮的实时资讯，又能扮演盘踞一方的全功能型网站，与native结合后又能扮演诸如公众号之内的应用等等，其能力可想而知。_

webview在android端的演化可谓_曲折_，2015年google宣布不在支持4.4版本一下的webview[[1]](#fn1)，这意味着目前扔有近四分之一的Android用户因无法获得Google的支持而受到安全威胁。对于深度依赖webview的巨头开始自家的内核及上层sdk以求提升整体稳定性及其性能,腾讯x5内核是比较通用的一种.

目前大致的webview内存处理方式分为两类:

### 1.独立的web进程，与主进程隔开

这个方法被运用于类似qq，微信这样的超级app中，这也是解决任何webview内存问题屡试不爽的方法
对于封装的webactivity，在`manifest.xml`中设置

```
<activity android:name=".webview.WebViewActivity" android:launchMode="singleTop" android:process=":remote" android:screenOrientation="unspecified" />

```

然后在关闭webactivity时销毁进程

```
@Overrideprotected void onDestroy() {                
     super.onDestroy(); 
     System.exit(0);
}

```

关闭浏览器后便销毁整个进程，这样一般`95%`的情况下不会造成内存泄漏之类的问题，但这就涉及到[android进程间通讯](https://link.jianshu.com?t=http://blog.csdn.net/hitlion2008/article/details/9824009)，比较不方便处理， 优劣参半，也是可选的一个方案.

### 2.封装过的webview

相比系统内置的webview的支持自2005年之后就没有了，而首推google的chrome。 腾讯的x5webview对h5的兼容性与稳定性与安全性逐渐凸显出来，并自成一系, 下面以使用[x5webview](https://link.jianshu.com?t=http://x5.tencent.com/)为例做说明:

*   首先使用webview的时候，不在xml里面声明，而是直接代码new个对象，传入application context防止activity引用滥用.

```
webView =  new BridgeWebView(getContext().getApplicationContext());
webFrameLayout.addView(webView, 0);

```

在使用了这个方式后，基本上90%的webview内存泄漏的问题便得以解决.

*   而在android4.4版本以下，会出现android webview无法自动释放，如在`fragment`中,使用`ondetach`的释放webview是比较好的时机[[2]](#fn2)

```
public void onDetach() {
    releaseWebViews();
    super.onDetach();
}
public synchronized void releaseWebViews() {
    if(webView != null) {
        try {
            if(webView.getParent() != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//this is causing the segfault occasionally below 4.2
            webView.destroy();
    //                }
        }catch (IllegalArgumentException e) {
            DLog.p(e);
        }
        RefWatcher refWatcher = FApplication.getRefWatcher();
        refWatcher.watch(webView);
        webView = null;
    }
}

```

其中webview自身的销毁代码如下：

```
@Override
public void destroy()
//flushMessageQueue();
    clearCache(true);
    clearFormData();
    clearMatches();
    clearSslPreferences();
    clearDisappearingChildren();
    clearHistory();
    //@Deprecated
    //clearView();
    clearAnimation();
    loadUrl("about:blank");
    removeAllViews();
    freeMemory();
    super.destroy();
}

```

*   如果以上的方案还不管用，实时加入`反射`来清理webview的引用：

```
public void setConfigCallback(WindowManager windowManager) {
    try {
        Field field = WebView.class.getDeclaredField("mWebViewCore");
        field = field.getType().getDeclaredField("mBrowserFrame");
        field = field.getType().getDeclaredField("sConfigCallback");
        field.setAccessible(true);
        Object configCallback = field.get(null);
        if (null == configCallback) {
            return;
        }
        field = field.getType().getDeclaredField("mWindowManager");
        field.setAccessible(true);
        field.set(configCallback, windowManager);
    } catch(Exception e) {
    }
}

```

然后在activity中加入

```
public void onCreate(BundlesavedInstanceState){
    super.onCreate(savedInstanceState);
    setConfigCallback((WindowManager);
    getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
}
publicvoidonDestroy()
{
    setConfigCallback(null);
    super.onDestroy();
}

```

*   对于application 级别的可能的misbehaving callbacks，加入

```
private static String[] misbehavingClasses = new String[]{
    "com.google.android.gms.ads",
    "com.android.org.chromium.android_webview.AwContents$AwComponentCallbacks",
};
public static boolean isMisbehavingCallBacks(String name){
    for(String s : misbehavingClasses){
        if(name.startsWith(s)){
            return true;
        }
    }
    return false;
}

```

然后重写applicaiton类记录这些callback, 并在适当的时机删掉：

```
@Override
public void registerComponentCallbacks(ComponentCallbacks callback) {
    super.registerComponentCallbacks(callback);
    ComponentCallbacksBehavioralAdjustmentToolIcs.INSTANCE.onComponentCallbacksRegistered(callback);
}

@Override
public void unregisterComponentCallbacks(ComponentCallbacks callback) {
    ComponentCallbacksBehavioralAdjustmentToolIcs.INSTANCE.onComponentCallbacksUnregistered(callback);
    super.unregisterComponentCallbacks(callback);
}

public void forceUnregisterComponentCallbacks() {
    ComponentCallbacksBehavioralAdjustmentToolIcs.INSTANCE.unregisterAll(this);
}

private static class ComponentCallbacksBehavioralAdjustmentToolIcs {
    private static final String TAG = "componentCallbacks";
    static ComponentCallbacksBehavioralAdjustmentToolIcs INSTANCE = new ComponentCallbacksBehavioralAdjustmentToolIcs();

    private WeakHashMap<ComponentCallbacks, ApplicationErrorReport.CrashInfo> mCallbacks = new WeakHashMap<>();
    private boolean mSuspended = false;

    public void onComponentCallbacksRegistered(ComponentCallbacks callback) {
        Throwable thr = new Throwable("Callback registered here.");
        ApplicationErrorReport.CrashInfo ci = new ApplicationErrorReport.CrashInfo(thr);

        if (FApplication.DEBUG) DLog.w(TAG, "registerComponentCallbacks: " + callback.getClass().getName(), thr);

        if (!mSuspended) {
            if (BugFix.isMisbehavingCallBacks(callback.getClass().getName())) {
                mCallbacks.put(callback, ci);
            }
            // TODO: other classes may still prove to be problematic?  For now, only watch for .gms.ads, since we know those are misbehaving
        } else {
            if (FApplication.DEBUG) DLog.e(TAG, "ComponentCallbacks was registered while tracking is suspended!");
        }
    }

    public void onComponentCallbacksUnregistered(ComponentCallbacks callback) {
        if (!mSuspended) {
            if (FApplication.DEBUG) {
                DLog.i(TAG, "unregisterComponentCallbacks: " + callback, new Throwable());
            }

            mCallbacks.remove(callback);
        }
    }

    public void unregisterAll(Context context) {
        mSuspended = true;
        for (Map.Entry<ComponentCallbacks, ApplicationErrorReport.CrashInfo> entry : mCallbacks.entrySet()) {
            ComponentCallbacks callback = entry.getKey();
            if (callback == null) continue;

            if (FApplication.DEBUG) {
                DLog.w(TAG, "Forcibly unregistering a misbehaving ComponentCallbacks: " + entry.getKey());
                DLog.w(TAG, entry.getValue().stackTrace);
            }

            try {
                context.unregisterComponentCallbacks(entry.getKey());
            } catch (Exception exc) {
                if (FApplication.DEBUG) DLog.e(TAG, "Unable to unregister ComponentCallbacks", exc);
            }
        }

        mCallbacks.clear();
        mSuspended = false;
    }
}

```

##### 为方便webactivity的debug

*   在`application`的`oncreate`里面，我们加入

```
private static void enableStrictMode() {
StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
    new StrictMode.ThreadPolicy.Builder()
    .detectAll()
    .penaltyLog();
    StrictMode.VmPolicy.Builder vmPolicyBuilder =
    new StrictMode.VmPolicy.Builder()
    .detectAll()
    .penaltyLog();
    threadPolicyBuilder.penaltyFlashScreen();
    vmPolicyBuilder.setClassInstanceLimit(WebActivity.class, 1);
    StrictMode.setThreadPolicy(threadPolicyBuilder.build());
    StrictMode.setVmPolicy(vmPolicyBuilder.build());
}

```

*   在webview中，对于android4.4以上的版本，我们开启调试模式

```
if (FApplication.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    WebView.setWebContentsDebuggingEnabled(true);
}

```

*   最后不可缺少的是, leakcanary的加入来跟踪这些消耗内存的组件:
    在`webfragment`，`webactivity`，`webview` ondestory后加上

```
    RefWatcher refWatcher = FApplication.getRefWatcher();
    refWatcher.watch(obj);

```

> 总结: 如果你只是简单地用 webview 做呈现, 使用application context启动webview已经足够了，但如果你需要webview来播放视频，处理弹窗等复杂工作, 新建一个进程来处理会更可靠.

* * *

1.  [Google将不再为Android 4.4之前版本提供WebView补丁](https://link.jianshu.com?t=http://www.leiphone.com/news/201501/1NkxWlVGV1IVMUyr.html) [↩](#fnref1)

2.  [this is how I fixed my WebView leak inside a fragment](https://link.jianshu.com?t=http://stackoverflow.com/a/12408703/1369016) [↩](#fnref2)

作者：jefforeilly
链接：https://www.jianshu.com/p/c2412918b2b5
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。