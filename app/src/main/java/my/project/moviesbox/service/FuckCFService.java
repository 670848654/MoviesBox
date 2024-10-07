package my.project.moviesbox.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.SharedPreferencesUtils;

/**
 * @author Li
 * @version 1.0
 * @description: 尝试绕过浏览器安全检查服务
 * @date 2024/5/30 14:15
 */
public class FuckCFService extends Service {
    private WebView webView;
    private Handler handler;
    private String url, type;
    private String html = "";

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            webView = new WebView(getApplicationContext());
            webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // 获取页面的 HTML 内容
                    view.evaluateJavascript("document.documentElement.outerHTML", value -> {
                        // 处理获取到的 HTML 源代码
                        value = value.replace("\\u003C", "<")
                                .replace("\\u003E", ">")
                                .replace("\\u0026", "&")
                                .replace("\\n", "")
                                .replace("\\t", "")
                                .replace("\\\"", "\"");
                        // 设定时间内获取最后的源码内容
                        html = value;
                    });
                }
            });

            webView.setWebChromeClient(new WebChromeClient());
            Map<String, String> headers = new HashMap<>();

            webView.loadUrl(url);
        });

        handler.postDelayed(() -> {
            html = html.isEmpty() ? "设定时间内未获取到网站源代码..." : html;
            EventBus.getDefault().post(new HtmlSourceEvent(html, type));
            stopSelf();
        }, SharedPreferencesUtils.getByPassCFTimeout() * 1000L);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            url = intent.getStringExtra("url");
            type = intent.getStringExtra("type");
            LogUtil.logInfo(this.getClass().getName() + "url", url);
        } else
            stopSelf();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
    }
}