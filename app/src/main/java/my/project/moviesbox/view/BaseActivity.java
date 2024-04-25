package my.project.moviesbox.view;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.animation.ScaleInAnimation;
import com.google.android.material.snackbar.Snackbar;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.parser.config.ParserInterfaceFactory;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.utils.StatusBarUtil;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: BaseActivity
  * @描述: 视图基类
  * @作者: Li Z
  * @日期: 2024/2/4 17:06
  * @版本: 1.0
 */
public abstract class BaseActivity<V, P extends Presenter<V>> extends AppCompatActivity {
    protected P mPresenter;
    protected App application;
    private Unbinder mUnBinder;
    protected static String PREVIDEOSTR = Utils.getString(R.string.preEpisode);
    protected static String NEXTVIDEOSTR = Utils.getString(R.string.nextEpisode);
    protected static String PAGE_AND_ALL_PAGE = Utils.getString(R.string.pageAndAllPage);
    protected static String LOAD_PAGE_AND_ALL_PAGE = Utils.getString(R.string.loadPageAndAllPage);
    protected boolean isPortrait;
    protected int position = 0;
    protected int change;
    protected ParserInterface parserInterface = ParserInterfaceFactory.getParserInterface();
    /* 空布局视图相关 */
    protected View rvView;
    protected ProgressBar progressBar; // 加载视图
    protected RelativeLayout errorView; // 出错视图
    protected Button refDataBtn; // 重试按钮
    protected TextView errorMsgView; // 错误视图文本
    private LinearLayout emptyView; // 空布局
    private TextView emptyMsg; // 空布局视图文本

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration configuration = getResources().getConfiguration();
        change = configuration.orientation;
        isPortrait = change == Configuration.ORIENTATION_PORTRAIT;
//        setDeviceOrientation();
        initBeforeView();
        setContentView(setLayoutRes());
        if (Utils.checkHasNavigationBar(this)) {
            /*if (!getRunningActivityName().equals("PlayerActivity"))
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );*/
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getWindow().setNavigationBarContrastEnforced(false);
                getWindow().setStatusBarContrastEnforced(false);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                );
                getWindow().setNavigationBarColor(Color.TRANSPARENT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(false);
            }
        }
        mUnBinder = ButterKnife.bind(this);
        if (application == null)
            application = (App) getApplication();
        build();
    }

    protected abstract P createPresenter();

    protected abstract void loadData() ;

    protected abstract int setLayoutRes();

    protected abstract void init();

    protected abstract void initBeforeView();

    /**
     * 设置视图方向
     * <p>手机 竖屏显示</p>
     * <p>平板 横屏显示</p>
     */
    private void setDeviceOrientation() {
        if (!getRunningActivityName().equals("PlayerActivity") &&
                !getRunningActivityName().equals("LocalPlayerActivity") &&
                !getRunningActivityName().equals("VipParsingInterfacesPlayerActivity") &&
                !getRunningActivityName().equals("UpnpActivity")) {
            setRequestedOrientation(Utils.isPad() ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 初始化自定义布局
     */
    protected void initCustomViews() {
        rvView = getLayoutInflater().inflate(R.layout.base_emnty_view, null);
        progressBar = rvView.findViewById(R.id.progress);
        errorView = rvView.findViewById(R.id.error_view);
        refDataBtn = rvView.findViewById(R.id.ref_data);
        errorMsgView = rvView.findViewById(R.id.error_msg);
        emptyView = rvView.findViewById(R.id.empty_view);
        emptyMsg = rvView.findViewById(R.id.empty_msg);
        refDataBtn.setOnClickListener(view -> retryListener());
    }


    protected static final int ADAPTER_SCALE_IN_ANIMATION = 0;
    protected static final int ADAPTER_ALPHA_IN_ANIMATION = 1;
    /**
     * 设置rvlist动画
     * @param adapter
     * @param adapterAnimation
     * @param firstOnly
     */
    protected void setAdapterAnimation(BaseQuickAdapter adapter, int adapterAnimation, boolean firstOnly) {
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(firstOnly);
        switch (adapterAnimation) {
            case ADAPTER_SCALE_IN_ANIMATION:
                adapter.setAdapterAnimation(new ScaleInAnimation());
                break;
            case ADAPTER_ALPHA_IN_ANIMATION:
                adapter.setAdapterAnimation(new AlphaInAnimation());
                break;
        }
    }

    /**
     * 刷新视图
     */
    protected void rvLoading() {
        if (isFinishing()) return;
        progressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    /**
     * 隐藏加载进度条
     */
    protected void hideProgress() {
        if (isFinishing()) return;
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 空视图
     * @return
     */
    protected void rvEmpty(String msg) {
        if (isFinishing()) return;
        progressBar.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        emptyMsg.setText(msg);
        emptyView.setVisibility(View.VISIBLE);
    }

    /**
     * 错误视图
     * @return
     */
    protected void rvError(String msg) {
        if (isFinishing()) return;
        progressBar.setVisibility(View.GONE);
        errorMsgView.setText(msg);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }


    /**
     * 隐藏虚拟导航按键
     */
    protected void hideNavBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * 虚拟导航按键
     */
    protected void showNavBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /**
     * Android 9 异形屏适配
     */
    protected void hideGap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceOrientation();
    }

    @Override
    protected void onDestroy() {
        //取消View的关联
        if (null != mPresenter)
            mPresenter.detachView();
        mUnBinder.unbind();
        super.onDestroy();
    }

    protected boolean gtSdk23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    protected boolean gtSdk26() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    protected boolean gtSdk30() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    protected boolean gtSdk33() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    private String getRunningActivityName() {
        String contextString = this.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1,
                contextString.indexOf("@"));
    }

    private void setStatusBarColor() {
        if (!getRunningActivityName().equals("HomeActivity") &&
                !getRunningActivityName().equals("DetailsActivity") &&
                !getRunningActivityName().equals("PlayerActivity") &&
                !getRunningActivityName().equals("LocalPlayerActivity") &&
                !getRunningActivityName().equals("VipParsingInterfacesPlayerActivity") &&
                !getRunningActivityName().equals("UpnpActivity") &&
                !getRunningActivityName().equals("ImageActivity")) {
            StatusBarUtil.setColorForSwipeBack(this, DarkModeUtils.isDarkMode(this) ? getColor(R.color.night_color_primary) : getColor(R.color.light_color_primary), 0);
        }
        if (DarkModeUtils.isDarkMode(this) || getRunningActivityName().equals("DetailsActivity"))
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void build() {
        Utils.createDataFolders();
        hideGap();
        setStatusBarColor();
        initCustomViews();
        init();
        mPresenter = createPresenter();
        loadData();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == change) return;
        change = newConfig.orientation;
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setConfigurationChanged();
    }

    protected abstract void setConfigurationChanged();

    /**
     * 点击重试抽象方法
     * @return
     */
    protected abstract void retryListener();

    protected void showSnackbarMsg(View view, String msg, View anchorView, boolean warning) {
        Snackbar
                .make(view, msg, Snackbar.LENGTH_LONG)
                .setAnchorView(anchorView)
                .setTextColor(getColor(R.color.night_text_color))
                .setBackgroundTint(warning ? getColor(R.color.orange500) : getColor(R.color.pink200))
                .show();
    }
}
