package my.project.moviesbox.view;

import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;

import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.MaterialShapeDrawable;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.project.moviesbox.R;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.databinding.ActivitySniffingVideoBinding;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/8/11 14:29
 */
public class SniffingVideoActivity extends BaseActivity<ActivitySniffingVideoBinding> {
    private AppBarLayout appBar;
    protected Toolbar toolbar;
    private ProgressBar linearProgressIndicator;
    private WebView webView;
    private SwipeRefreshLayout mSwipe;
    private final List<DialogItemBean> playUrls = new ArrayList<>();
    private boolean shouldSniff = false;
    private String vodId;
    private String currentUrl;

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivitySniffingVideoBinding inflateBinding(LayoutInflater inflater) {
        return ActivitySniffingVideoBinding.inflate(inflater);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        linearProgressIndicator = binding.progress;
        webView = binding.webView;
        mSwipe = binding.mSwipe;
    }

    @Override
    public void initClickListeners() {

    }

    @Override
    protected void init() {
        setToolbar(toolbar, "加载网页中...", "");
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsInterface(), "AndroidJs");
        webView.setWebViewClient(new MyWebViewClient(this));
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                toolbar.setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    linearProgressIndicator.setVisibility(View.VISIBLE);
                    linearProgressIndicator.setProgress(newProgress, true);
                } else
                    linearProgressIndicator.setVisibility(View.GONE);

            }
        });
        vodId = getIntent().getStringExtra("vodId");
        currentUrl = getIntent().getStringExtra("url");
        if (currentUrl == null) currentUrl = "";
        if (!currentUrl.startsWith("http")) {
            currentUrl = ParserInterfaceFactory.getParserInterface().getDefaultDomain() + currentUrl;
        }
        shouldSniff = true;
        webView.loadUrl(currentUrl);
        initSwipe();
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            webView.loadUrl(currentUrl);
            mSwipe.setRefreshing(false);
        });
    }

    private boolean linkFound = false; // 防止重复弹窗
    private AlertDialog alertDialog;
    private class MyWebViewClient extends WebViewClient {

        private final WeakReference<SniffingVideoActivity> activityRef;

        MyWebViewClient(SniffingVideoActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            SniffingVideoActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return null;
            }
            if (!shouldSniff) {
                return null; // 当前页面不是 /v/ 的，不嗅探
            }
            String url = request.getUrl().toString();
            Map<String, String> headers = request.getRequestHeaders();
            String contentType = headers.get("Content-Type");

            for (String suffering : ConfigManager.getInstance().getSuffering()) {
                if (url.contains(suffering)) {
                    return null;
                }
            }

            if (!linkFound && url.contains("video.m3u8") ) {
                linkFound = true; // 标记已经找到
                LogUtil.logInfo("嗅探到的视频地址", url);
                DialogItemBean dialogItemBean = new DialogItemBean(url, M3U8);
                if (!playUrls.contains(dialogItemBean)) {
                    playUrls.add(dialogItemBean);
                }
            }
            return null;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String newUrl = request.getUrl().toString();
            shouldSniff = newUrl.contains("/v/"); // 动态更新
            // 每次加载新页面时重置状态
            playUrls.clear();     // 清空已找到的链接
            linkFound = false;    // 重置找到标志

            // 如果之前有弹窗，先关闭，防止旧弹窗还在
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                alertDialog = null;
            }
            // 继续在 WebView 内加载
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            currentUrl = url;
            String js = "javascript:(function(){" +
                    "var activeA = document.querySelector('a.active');" +
                    "if(activeA) {" +
                    "  AndroidJs.onReceiveActiveText(activeA.innerText);" +
                    "} else {" +
                    "  AndroidJs.onReceiveActiveText('');" +
                    "}" +
                    "})()";

            webView.evaluateJavascript(js, null);
        }
    }

    public class JsInterface {
        @JavascriptInterface
        public void onReceiveActiveText(String text) {
            // 这里拿到 a.active 的文本内容了
            if (Utils.isNullOrEmpty(text))
                return;
            // 你可以用handler发送消息，更新UI等
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed() && playUrls.size() > 0) {
                    alertDialog = new MaterialAlertDialogBuilder(SniffingVideoActivity.this, R.style.DialogStyle)
                            .setTitle("\uD83D\uDE06嗅探完成啦！["+text+"]")
                            .setMessage("已找到当前视频播放地址，请选择操作\uD83D\uDC47")
                            .setPositiveButton("播放", (dialog, which) -> {
                                dialog.dismiss();
                                EventBus.getDefault().post(
                                        new VideoSniffEvent(vodId, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.PLAY, true, playUrls, text)
                                );
                                finish();
                            })
                            .setCancelable(false)
                            .setNegativeButton("关闭", null)
                            .setNeutralButton("下载", (dialog, which) -> {
                                dialog.dismiss();
                                EventBus.getDefault().post(
                                        new VideoSniffEvent(vodId, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.DOWNLOAD, true, playUrls, text)
                                );
                                finish();
                            })
                            .show();
                    Utils.dialogSetRenderEffect(SniffingVideoActivity.this);
                    alertDialog.setOnDismissListener(dialog -> Utils.dialogClearRenderEffect(SniffingVideoActivity.this));
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }


    @Override
    protected void setConfigurationChanged() {}

    /**
     * 点击重试抽象方法
     *
     * @return
     */
    @Override
    protected void retryListener() {}
}
