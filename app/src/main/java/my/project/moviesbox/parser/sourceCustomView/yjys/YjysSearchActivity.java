package my.project.moviesbox.parser.sourceCustomView.yjys;

import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.SearchActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 缘觉影视使用的搜索界面
 * @date 2024/8/17 9:54
 */
public class YjysSearchActivity extends SearchActivity {
    private String cookies = "";

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
    public void error(boolean firstTimeData, String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
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
                    "可能需要进行人机验证",
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
        BottomSheetDialog webviewBottomSheetDialog = new BottomSheetDialog(this);
        FrameLayout view= (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_webview, null);
        WebView webView= view.findViewById(R.id.webView);
        Button getCookie = view.findViewById(R.id.getCookie);
        getCookie.setOnClickListener(v -> {
            webviewBottomSheetDialog.dismiss();
            App.setCookies(cookies);
            if (firstTimeData)
                retryListener();
            else
                mPresenter.loadPageData(title, String.valueOf(page));
        });
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

        webviewBottomSheetDialog.setOnDismissListener(dialog -> {
            // 销毁 WebView
            webView.stopLoading();
            webView.destroy();
        });

        // 加载 URL
        webView.loadUrl(url);

        webviewBottomSheetDialog.setContentView(view);
        // 获取 BottomSheet 并设置为全屏高度
        FrameLayout bottomSheet = webviewBottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT; // 强制全屏高度
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true); // 避免回到折叠状态
        }
        webviewBottomSheetDialog.show();
    }
}
