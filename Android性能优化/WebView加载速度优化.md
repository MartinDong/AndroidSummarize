在做混合应用的时候，有几个痛点，一个是无网络无法使用，还有一个是受网络环境影响的网页加载速度。今天就这两个问题，和大家交流一下自己的经验。

## 离线缓存

这个比较容易，开启webView的缓存功能就可以了。

```
WebSettings settings = webView.getSettings();
settings.setAppCacheEnabled(true);
settings.setDatabaseEnabled(true);
settings.setDomStorageEnabled(true);//开启DOM缓存，关闭的话H5自身的一些操作是无效的
settings.setCacheMode(WebSettings.LOAD_DEFAULT);

```

这边我们通过setCacheMode方法来设置WebView的缓存策略，WebSettings.LOAD_DEFAULT是默认的缓存策略，它在缓存可获取并且没有过期的情况下加载缓存，否则通过网络获取资源。这样的话可以减少页面的网络请求次数，那我们如何在离线的情况下也能打开页面呢，这里我们在加载页面的时候可以通过判断网络状态，在无网络的情况下更改webview的缓存策略。

```
ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
NetworkInfo info = cm.getActiveNetworkInfo();
if(info.isAvailable())
{
    settings.setCacheMode(WebSettings.LOAD_DEFAULT);
}else 
{
    settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);//不使用网络，只加载缓存
}

```

这样我们就可以使我们的混合应用在没有网络的情况下也能使用一部分的功能，不至于什么都显示不了了，当然如果我们将缓存做的更好一些，**在网络好的时候，比如说在WIFI状态下，去后台加载一些网页缓存起来，这样处理的话，即使在无网络情况下第一次打开某些页面的时候，也能将该页面显示出来。**
当然缓存资源后随之会带来一个问题，那就是资源无法及时更新，WebSettings.LOAD_DEFAULT中的页面中的缓存版本好像不是很起作用，所以我们这边可能需要自己做一个缓存版本控制。这个缓存版本控制可以放在APP版本更新中。

```
if (upgrade.cacheControl > cacheControl)
{
    webView.clearCache(true);//删除DOM缓存
    VersionUtils.clearCache(mContext.getCacheDir());//删除APP缓存
    try
    {
        mContext.deleteDatabase("webview.db");//删除数据库缓存
        mContext.deleteDatabase("webviewCache.db");
    }
    catch (Exception e)
    {
    }
}

```

## 预加载

有时候一个页面资源比较多，图片，CSS，js比较多，还引用了JQuery这种庞然巨兽，从加载到页面渲染完成需要比较长的时间，有一个解决方案是将这些资源打包进APK里面，然后当页面加载这些资源的时候让它从本地获取，这样可以提升加载速度也能减少服务器压力。重写WebClient类中的shouldInterceptRequest方法，再将这个类设置给WebView。

```
webView.setWebViewClient(new WebViewClient()
{

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        if (url.contains("[tag]"))
        {
            String localPath = url.replaceFirst("^http.*[tag]\\]", "");
            try
            {
                InputStream is = getApplicationContext().getAssets().open(localPath);
                Log.d(TAG, "shouldInterceptRequest: localPath " + localPath);
                String mimeType = "text/javascript";
                if (localPath.endsWith("css"))
                {
                    mimeType = "text/css";
                }
                return new WebResourceResponse(mimeType, "UTF-8", is);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            return null;
        }

    }
});

```

这里我们队页面中带有特殊标记的请求进行过滤替换，也就是上面代码中的[tag]，这个可以跟做后台开发的同事约定好来就行了。对图片资源或者其他资源进行替换也是可以的。补充一个小点可以通过settings.setLoadsImagesAutomatically(true);来设置在页面装载完成之后再去加载图片。

## H5优化

Android的OnPageFinished事件会在Javascript脚本执行完成之后才会触发。如果在页面中使 用JQuery，会在处理完DOM对象，执行完**$(document).ready(function() {});**事件自会后才会渲染并显示页面。而同样的页面在iPhone上却是载入相当的快，因为iPhone是显示完页面才会触发脚本的执行。所以我们这边的解决方案延迟JS脚本的载入，这个方面的问题是需要Web前端工程师帮忙优化的，网上应该有比较多LazyLoad插件，这里放一个比较老的链接[Painless JavaScript lazy loading with LazyLoad](https://link.jianshu.com?t=http://wonko.com/post/painless_javascript_lazy_loading_with_lazyload),同样也放上一小段前端代码，仅供参考。

```
<script src="/css/j/lazyload-min.js" type="text/javascript"></script>
<script type="text/javascript" charset="utf-8">
  loadComplete() {
    //instead of document.read();
  } 
  function loadscript() {
    LazyLoad.loadOnce([
      '/css/j/jquery-1.6.2.min.js',
      '/css/j/flow/jquery.flow.1.1.min.js',
      '/css/j/min.js?v=2011100852'
      ], loadComplete);
  }
  setTimeout(loadscript,10);
</script>
```

作者：我是午饭
链接：https://www.jianshu.com/p/427600ca2107
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。