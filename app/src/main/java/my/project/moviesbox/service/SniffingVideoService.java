package my.project.moviesbox.service;

import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;
import static my.project.moviesbox.parser.config.VodTypeEnum.OTHER;

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
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.SharedPreferencesUtils;

/**
 * @author Li
 * @version 1.0
 * @description: 视频嗅探服务
 * @date 2024/5/30 14:15
 */
public class SniffingVideoService extends Service {
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
            String url = intent.getStringExtra("url");
            if (!url.startsWith("http"))
                url = ParserInterfaceFactory.getParserInterface().getDefaultDomain() + url;
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
        private final List<DialogItemBean> playUrls;
        private final List<DialogItemBean> notFoundUrls;

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
                List<DialogItemBean> urlsToPost = foundUrls ? playUrls : notFoundUrls;
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
            String contentType = headers.get("Content-Type"); // 通过请求头获取 MIME 类型
            // 忽略集合
            for (String suffering : ConfigManager.getInstance().getSuffering()) {
                if (url.contains(suffering)) {
                    // 如果 URL 包含任何一个 suffering 字符串，直接跳过该 URL
                    continue;
                }
                // 判断是否是 MP4 链接
                boolean isMp4 = url.toLowerCase().contains("mp4") ||
                        (contentType != null &&
                                (contentType.equals("video/mp4") ||
                                        contentType.equals("audio/mp4") ||
                                        contentType.equals("application/octet-stream")));

                if (isMp4 || url.contains("m3u8")) {
                    // 如果 URL 是 MP4 或 M3U8 类型
                    LogUtil.logInfo("嗅探到的视频地址", url);
                    DialogItemBean dialogItemBean = new DialogItemBean(url, isMp4 ? MP4 : M3U8);
                    if (!playUrls.contains(dialogItemBean))
                        playUrls.add(dialogItemBean);
                }
                else
                    notFoundUrls.add(new DialogItemBean(url, OTHER));
                break;
            }
            return null;
        }
    }

    public static VideoSniffEvent setVideoSniffEvent(boolean success, String activityEnum, String sniffEnum, List<DialogItemBean> urls) {
        return new VideoSniffEvent(
                VideoSniffEvent.ActivityEnum.valueOf(activityEnum),
                VideoSniffEvent.SniffEnum.valueOf(sniffEnum),
                success,
                urls
        );
    }
}