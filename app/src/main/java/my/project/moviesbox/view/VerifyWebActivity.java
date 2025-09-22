package my.project.moviesbox.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.databinding.ActivityWebviewBinding;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/7/30 17:28
 */
public class VerifyWebActivity extends BaseActivity<ActivityWebviewBinding> {
    private String cookies = "";
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
    protected ActivityWebviewBinding inflateBinding(LayoutInflater inflater) {
        return ActivityWebviewBinding.inflate(inflater);
    }

    protected AppBarLayout appBar;
    protected Toolbar toolbar;
    protected ProgressBar linearProgressIndicator;
    protected SwipeRefreshLayout mSwipe;
    protected WebView webView;
    protected ExtendedFloatingActionButton getCookieBtn;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        linearProgressIndicator = binding.progress;
        mSwipe = binding.mSwipe;
        webView = binding.webView;
        getCookieBtn = binding.getCookie;
    }

    @Override
    public void initClickListeners() {
        getCookieBtn.setOnClickListener(v -> {
            Utils.setVibration(v);
            App.setCookies(cookies);
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.setWebViewClient(null);
            webView.stopLoading();
            webView.clearHistory();
            webView.loadUrl("about:blank");

            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }

            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void init() {
        setToolbar(toolbar, "加载网页中...", "");
        currentUrl = getIntent().getStringExtra("url");
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getCookieBtn.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 15);
            getCookieBtn.setLayoutParams(params);
        }
        // 配置 WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                currentUrl = url;
                if (mSwipe != null && mSwipe.isRefreshing())
                    mSwipe.setRefreshing(false);
                // 获取 Cookie
                CookieManager cookieManager = CookieManager.getInstance();
                cookies = cookieManager.getCookie(url);
                LogUtil.logInfo("getCookie", cookies);
            }
        });
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

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
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
