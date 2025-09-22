package my.project.moviesbox.view.base;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.animation.ScaleInAnimation;
import com.chad.library.adapter.base.animation.SlideInBottomAnimation;
import com.chad.library.adapter.base.animation.SlideInLeftAnimation;
import com.chad.library.adapter.base.animation.SlideInRightAnimation;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.databinding.BaseEmntyViewBinding;
import my.project.moviesbox.enums.AdapterAnimationTypeEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: UI基类
 * @date 2025/8/28 16:24
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    protected static final int DIRECTORY_REQUEST_CODE = 0x10010;
    protected static final int DIRECTORY_CONFIG_RESULT_CODE = 0x10011;
    protected App application;
    protected VB binding;
    /**
     * 弹出窗
     */
    protected AlertDialog alertDialog;
    protected ParserInterface parserInterface = ParserInterfaceFactory.getParserInterface();
    protected int change;
    protected boolean isPortrait;
    protected static String PREVIDEOSTR = Utils.getString(R.string.preEpisode);
    protected static String NEXTVIDEOSTR = Utils.getString(R.string.nextEpisode);
    protected static String PAGE_AND_ALL_PAGE = Utils.getString(R.string.pageAndAllPage);
    protected static String LOAD_PAGE_AND_ALL_PAGE = Utils.getString(R.string.loadPageAndAllPage);
    protected int position = 0;
    /* 空布局视图相关 */
    protected View rvView;
    protected LinearProgressIndicator linearProgressIndicator; // 加载视图
    protected RelativeLayout errorView; // 出错视图
    protected Button refDataBtn; // 重试按钮
    protected TextView errorMsgView; // 错误视图文本
    private LinearLayout emptyView; // 空布局
    private TextView emptyMsg; // 空布局视图文本
    protected int page =  parserInterface.startPageNum(); // 开始页码
    protected int pageCount = parserInterface.startPageNum(); // 结束页码
    protected boolean isErr = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (application == null)
            application = (App) getApplication();
        Configuration configuration = getResources().getConfiguration();
        change = configuration.orientation;
        isPortrait = change == Configuration.ORIENTATION_PORTRAIT;
        initBeforeView();
        // 初始化 ViewBinding
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Utils.checkHasNavigationBar(this)) {
            /*if (!getRunningActivityName().equals("PlayerActivity"))
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );*/
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.setNavigationBarDividerColor(Color.TRANSPARENT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setNavigationBarContrastEnforced(false);
                window.setStatusBarContrastEnforced(false);
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                );
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(window, false);
            }
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        Utils.createDataFolders();
        hideGap();
        setStatusBarColor();
        initCustomViews();
        findById();
        init();
        initClickListeners();
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

    /**
     * 通用设置toolbar方法
     * @param toolbar toolbar
     * @param title 标题
     * @param subTitle 副标题
     */
    protected void setToolbar(@NonNull Toolbar toolbar, @NonNull String title, String subTitle) {
        toolbar.setTitle(title);
        if (!Utils.isNullOrEmpty(subTitle))
            toolbar.setSubtitle(subTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            Utils.setVibration(view);
            finish();
        });
    }

    private String getRunningActivityName() {
        String contextString = this.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1,
                contextString.indexOf("@"));
    }

    /**
     * 设置视图方向
     * <p>手机 竖屏显示</p>
     * <p>平板 横屏显示</p>
     */
    private void setDeviceOrientation() {
        if (!getRunningActivityName().equals("PlayerActivity") &&
                !getRunningActivityName().equals("LocalPlayerActivity") &&
                !getRunningActivityName().equals("LocalListPlayerActivity") &&
                !getRunningActivityName().equals("VipParsingInterfacesPlayerActivity") &&
                !getRunningActivityName().equals("UpnpActivity")) {
            setRequestedOrientation(Utils.isPad() ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void setStatusBarColor() {
        /*if (!getRunningActivityName().equals("SearchActivity") &&
                !getRunningActivityName().equals("VerifySearchActivity") &&
                !getRunningActivityName().equals("HomeActivity") &&
                !getRunningActivityName().equals("DetailsActivity") &&
                !getRunningActivityName().equals("PlayerActivity") &&
                !getRunningActivityName().equals("LocalPlayerActivity") &&
                !getRunningActivityName().equals("LocalListPlayerActivity") &&
                !getRunningActivityName().equals("VipParsingInterfacesPlayerActivity") &&
                !getRunningActivityName().equals("UpnpActivity") &&
                !getRunningActivityName().equals("ImageActivity") &&
                !getRunningActivityName().equals("ImagePreviewActivity")) {
            StatusBarUtil.setColorForSwipeBack(this, DarkModeUtils.isDarkMode(this) ? getColor(R.color.night_color_primary) : getColor(R.color.light_color_primary), 0);
        }*/
        if (DarkModeUtils.isDarkMode(this) || getRunningActivityName().equals("DetailsActivity"))
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
     * 设置rvlist动画
     * @param adapter
     */
    public <T, VH extends BaseViewHolder> void setAdapterAnimation(BaseQuickAdapter<T, VH> adapter) {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean enableAnimation = configManager.isAnimationEnable();
        if (enableAnimation) {
            adapter.setAnimationEnable(true);
            adapter.setAnimationFirstOnly(configManager.isAnimationFirstOnly());
            String animationDefaultStr = configManager.getAnimationDefault();
            AdapterAnimationTypeEnum animationDefault = AdapterAnimationTypeEnum.valueOf(animationDefaultStr.toUpperCase(Locale.US));
            switch (animationDefault) {
                case ALPHA_IN:
                    adapter.setAdapterAnimation(new AlphaInAnimation());
                    break;
                case SCALE_IN:
                    adapter.setAdapterAnimation(new ScaleInAnimation());
                    break;
                case SLIDE_IN_BOTTOM:
                    adapter.setAdapterAnimation(new SlideInBottomAnimation());
                    break;
                case SLIDE_IN_LEFT:
                    adapter.setAdapterAnimation(new SlideInLeftAnimation());
                    break;
                case SLIDE_IN_RIGHT:
                    adapter.setAdapterAnimation(new SlideInRightAnimation());
                    break;
            }
        }
    }

    protected void emptyRecyclerView(RecyclerView... recyclerViews) {
        for (RecyclerView rv : recyclerViews) {
            if (!Utils.isNullOrEmpty(rv))
                rv.setAdapter(null);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == change) return;
        change = newConfig.orientation;
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setConfigurationChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceOrientation();
    }

    @Override
    protected void onDestroy() {
        alertDialog = null;
        super.onDestroy();
    }

    /**
     * 初始化自定义布局
     */
    protected BaseEmntyViewBinding emptyBinding;
    protected void initCustomViews() {
        emptyBinding = BaseEmntyViewBinding.inflate(getLayoutInflater());
        rvView = emptyBinding.getRoot();
        linearProgressIndicator = emptyBinding.progress;
        errorView = emptyBinding.errorView;
        refDataBtn = emptyBinding.refData;
        errorMsgView = emptyBinding.errorMsg;
        emptyView = emptyBinding.emptyView;
        emptyMsg = emptyBinding.emptyMsg;
        refDataBtn.setOnClickListener(view -> retryListener());
    }

    protected <T, VH extends BaseViewHolder> void setLoadState(BaseQuickAdapter<T, VH> adapter, boolean loadState) {
        isErr = loadState;
        adapter.getLoadMoreModule().loadMoreComplete();
    }

    /**
     * 刷新视图
     */
    protected void rvLoading() {
        if (isFinishing()) return;
        if (linearProgressIndicator.getVisibility() != View.VISIBLE) {
            linearProgressIndicator.setVisibility(View.VISIBLE);
        }
        linearProgressIndicator.show();
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    /**
     * 隐藏加载进度条
     */
    protected void hideProgress() {
        if (isFinishing()) return;
        linearProgressIndicator.hide();
    }

    /**
     * 空视图
     * @return
     */
    protected void rvEmpty(String msg) {
        if (isFinishing()) return;
        hideProgress();
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
        hideProgress();
        errorMsgView.setText(msg);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
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

    /**
     * 初始化视图前的操作
     */
    protected abstract void initBeforeView();

    /**
     * 子类实现，返回具体的 ViewBinding
     * @param inflater
     * @return
     */
    protected abstract VB inflateBinding(LayoutInflater inflater);

    /**
     * 初始化控件
     */
    protected abstract void findById();

    public abstract void initClickListeners();

    /**
     * 初始化数据/控件
     */
    protected abstract void init();

    /**
     * 横竖屏变化时实现
     */
    protected abstract void setConfigurationChanged();

    /**
     * 点击重试抽象方法
     * @return
     */
    protected abstract void retryListener();

    // activity动画
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (!getRunningActivityName().equals("ImagePreviewActivity")) {
            overridePendingTransition(R.anim.scale_fade_in, R.anim.scale_fade_out);
        }
    }
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.scale_fade_in, R.anim.scale_fade_out);
    }
    @Override
    public void finish() {
        super.finish();
        if (!getRunningActivityName().equals("ImagePreviewActivity")) {
            overridePendingTransition(R.anim.scale_fade_in, R.anim.scale_fade_out);
        }
    }
    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        overridePendingTransition(R.anim.scale_fade_in, R.anim.scale_fade_out);
    }
}