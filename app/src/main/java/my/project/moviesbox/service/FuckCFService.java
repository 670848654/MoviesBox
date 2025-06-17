package my.project.moviesbox.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.Queue;

import lombok.AllArgsConstructor;
import lombok.Data;
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
    private Queue<UrlQueue> urlQueue = new LinkedList<>(); // 用于保存传入的 URL 队列
    private UrlQueue queue;
    private boolean isProcessing = false; // 标记当前是否有任务在处理
    private Runnable timeoutRunnable;
    private boolean isPageLoaded = false;
    private String html = "";

    @Data
    @AllArgsConstructor
    public static class UrlQueue {
        private String url;
        private String type;
    }

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
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    isPageLoaded = false;
                    timeoutRunnable = () -> {
                        if (!isPageLoaded) {
                            LogUtil.logInfo( "页面加载超时，停止加载！", "");
                            view.stopLoading();  // 停止加载
                            sendData();
                        }
                    };
                    // 10秒后检查是否加载完成
                    handler.postDelayed(timeoutRunnable, SharedPreferencesUtils.getByPassCFTimeout() * 1000L);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // 获取页面的 HTML 内容
                    view.evaluateJavascript("document.documentElement.outerHTML", value -> {
                        isPageLoaded = true;
                        handler.removeCallbacks(timeoutRunnable);
                        value = value.replace("\\u003C", "<")
                                .replace("\\u003E", ">")
                                .replace("\\u0026", "&")
                                .replace("\\n", "")
                                .replace("\\t", "")
                                .replace("\\\"", "\"");
                        // 设定时间内获取最后的源码内容
                        html = value;
                        sendData();
                    });
                }
            });

            webView.setWebChromeClient(new WebChromeClient());
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String url = intent.getStringExtra("url");
            String type = intent.getStringExtra("type");
            UrlQueue newUrlQueue = new UrlQueue(url, type);
            // 判断是否已存在相同的 URL
            if (!urlQueue.contains(newUrlQueue)) {
                urlQueue.add(newUrlQueue); // 如果队列中不存在相同的 URL，则添加
                processNextUrl(); // 开始处理下一个 URL
            }
        } else
            stopSelf();
        return START_STICKY;
    }

    private void processNextUrl() {
        if (!isProcessing && !urlQueue.isEmpty()) {
            isProcessing = true; // 标记当前正在处理
            queue = urlQueue.poll(); // 取出队列中的下一个 URL

            handler.post(() -> {
                webView.loadUrl(queue.getUrl()); // 加载新的 URL
            });
        }
    }

    private void sendData() {
        if (html.isEmpty()) {
            html = "设定时间内未获取到网站源代码...";
        }
        EventBus.getDefault().post(new HtmlSourceEvent(html, queue.getType()));
        isProcessing = false; // 完成处理，标记当前处理已结束
        if (!urlQueue.isEmpty())
            processNextUrl(); // 继续处理下一个 URL
        else
            stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.logInfo("没有下一个任务了，关闭服务", "");
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        webView = null;
    }
}