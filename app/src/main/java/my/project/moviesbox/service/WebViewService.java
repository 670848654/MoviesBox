package my.project.moviesbox.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.parser.config.ParserInterfaceFactory;
import my.project.moviesbox.utils.SharedPreferencesUtils;

/**
 * @author Li
 * @version 1.0
 * @description: 视频嗅探服务
 * @date 2024/5/30 14:15
 */
public class WebViewService extends Service {
    private WebView webView;

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置 WebView 相关配置
        webView = new WebView(this);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String activityEnum = intent.getStringExtra("activityEnum");
            String sniffEnum = intent.getStringExtra("sniffEnum");
            String url = ParserInterfaceFactory.getParserInterface().getDefaultDomain() + intent.getStringExtra("url");
            webView.setWebViewClient(new MyWebViewClient(this, activityEnum, sniffEnum));
            webView.loadUrl(url);
        } else
            stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 销毁 WebView
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static class MyWebViewClient extends WebViewClient {
        private final String activityEnum;
        private final String sniffEnum;
        private final Service service;
        private final List<String> playUrls;
        private final List<String> notFoundUrls;

        private final Handler handler;
        private final Runnable timeoutRunnable;

        public MyWebViewClient(Service service, String activityEnum, String sniffEnum) {
            this.service = service;
            this.activityEnum = activityEnum;
            this.sniffEnum = sniffEnum;
            playUrls = new ArrayList<>();
            notFoundUrls = new ArrayList<>();
            this.handler = new Handler(Looper.getMainLooper());
            this.timeoutRunnable = () -> {
                // 如果在设定秒内没有找到匹配的URL，停止服务
                boolean foundUrls = !playUrls.isEmpty();
                List<String> urlsToPost = foundUrls ? playUrls : notFoundUrls;
                EventBus.getDefault().post(setVideoSniffEvent(foundUrls, this.activityEnum, this.sniffEnum, urlsToPost));
                this.service.stopSelf();
            };
        }

        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            // 页面开始加载时启动超时定时器
            handler.postDelayed(timeoutRunnable, SharedPreferencesUtils.getSniffTimeout()* 1000L);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            // 获取请求的 URL
            String url = request.getUrl().toString();
            // 获取请求的方法（GET、POST等）
            String method = request.getMethod();
            // 获取请求的头部信息
            Map<String, String> headers = request.getRequestHeaders();
            // 忽略集合
            for (String suffering : ConfigManager.getInstance().getSuffering()) {
                if (url.contains(suffering)) {
                    // 如果 URL 包含任何一个 suffering 字符串，直接跳过该 URL
                    continue;
                }
                if (url.contains("m3u8") || url.contains("mp4"))
                    playUrls.add(url);
                else
                    notFoundUrls.add(url);
                break;
            }
            return null;
        }
    }

    public static VideoSniffEvent setVideoSniffEvent(boolean success, String activityEnum, String sniffEnum, List<String> urls) {
        return new VideoSniffEvent(
                VideoSniffEvent.ActivityEnum.valueOf(activityEnum),
                VideoSniffEvent.SniffEnum.valueOf(sniffEnum),
                success,
                urls
        );
    }
}