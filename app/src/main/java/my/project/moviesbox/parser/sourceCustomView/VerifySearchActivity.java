package my.project.moviesbox.parser.sourceCustomView;

import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.SearchActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 存在手动验证使用的搜索界面
 * @date 2024/8/17 9:54
 */
public class VerifySearchActivity extends SearchActivity {
    private String cookies = "";
    private boolean firstTimeData;
    private BottomSheetDialog webviewBottomSheetDialog;
    private FrameLayout frameLayout;
    private WebView webView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Bundle bundle = getIntent().getExtras();
        if (!Utils.isNullOrEmpty(bundle)) {
            title = bundle.getString("title");
            mSearchView.setQuery(title, true);
        }
        return true;
    }

    @Override
    protected void init() {
        super.init();
        webviewBottomSheetDialog = new BottomSheetDialog(this);
        frameLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_webview, null);
        webviewBottomSheetDialog.setOnDismissListener(dialog -> stopWebView());
        ExtendedFloatingActionButton getCookie = frameLayout.findViewById(R.id.getCookie);
        getCookie.setOnClickListener(v -> {
            webviewBottomSheetDialog.dismiss();
            App.setCookies(cookies);
            if (firstTimeData)
                retryListener();
            else
                mPresenter.loadPageData(title, String.valueOf(page));
        });
        webviewBottomSheetDialog.setContentView(frameLayout);
        // 获取 BottomSheet 并设置为全屏高度
        FrameLayout bottomSheet = webviewBottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT; // 强制全屏高度
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true); // 避免回到折叠状态
        }
    }

    @Override
    public void error(boolean firstTimeData, String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            this.firstTimeData = firstTimeData;
            isSearch = false;
            mSwipe.setEnabled(true);
            mSwipe.setRefreshing(false);
            rvError(msg);
            App.getInstance().showToastMsg(msg, DialogXTipEnum.ERROR);
            String[] params = new String[2];
            params[0] = title;
            params[1] = String.valueOf(page);
            String url = parserInterface.getSearchUrl(params);
            Utils.showAlert(this,
                    getString(R.string.errorDialogTitle),
                    "可能需要进行手动验证码验证，校验动过后请手动点击【已完成校验】",
                    false,
                    "去验证",
                    "取消",
                    "",
                    (dialog, which) -> {
                        openWebView(firstTimeData, url);
                        dialog.dismiss();
                    },
                    null,
                    null);
            if (!firstTimeData) {
                setLoadState(adapter, false);
            }
        });
    }

    private void openWebView(boolean firstTimeData, String url) {
        this.firstTimeData = firstTimeData;
        if (Utils.isNullOrEmpty(webView)) {
            webView = frameLayout.findViewById(R.id.webView);
            // 配置 WebView
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // 获取 Cookie
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookies = cookieManager.getCookie(url);
                    LogUtil.logInfo("getCookie", cookies);
                }
            });
            webView.loadUrl(url);
        } else {
            webView.onResume();
            webView.loadUrl(url);
        }
        webviewBottomSheetDialog.show();
    }

    private void stopWebView() {
        if (webView != null) {
            webView.stopLoading();
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        webviewBottomSheetDialog = null;
        super.onDestroy();
    }
}
