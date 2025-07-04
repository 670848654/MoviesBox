package my.project.moviesbox.view;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.animation.ScaleInAnimation;
import com.chad.library.adapter.base.animation.SlideInBottomAnimation;
import com.chad.library.adapter.base.animation.SlideInLeftAnimation;
import com.chad.library.adapter.base.animation.SlideInRightAnimation;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.enums.AdapterAnimationTypeEnum;
import my.project.moviesbox.model.BaseModel;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.utils.StatusBarUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.lazyLoadImage.LazyLoadImgListener;

/**
  * @包名: my.project.moviesbox.view
  * @类名: BaseActivity
  * @描述: 视图基类
  * @作者: Li Z
  * @日期: 2024/2/4 17:06
  * @版本: 1.0
 */
public abstract class BaseActivity<M extends BaseModel, V, P extends Presenter<V, M>> extends AppCompatActivity {
    protected static final int DIRECTORY_REQUEST_CODE = 0x10010;
    protected static final int DIRECTORY_CONFIG_RESULT_CODE = 0x10011;
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
    protected int page =  parserInterface.startPageNum(); // 开始页码
    protected int pageCount = parserInterface.startPageNum(); // 结束页码
    protected boolean isErr = true;
    /**
     * 弹出窗
     */
    protected AlertDialog alertDialog;

    protected LazyLoadImgListener lazyLoadImgListener;

    protected void setLazyLoadImgListener(LazyLoadImgListener lazyLoadImgListener) {
        this.lazyLoadImgListener = lazyLoadImgListener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration configuration = getResources().getConfiguration();
        change = configuration.orientation;
        isPortrait = change == Configuration.ORIENTATION_PORTRAIT;
        initBeforeView();
        setContentView(setLayoutRes());
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
     * 通用设置toolbar方法
     * @param toolbar toolbar
     * @param title 标题
     */
    protected void setToolbar(Toolbar toolbar, @NonNull String title, String subTitle) {
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

    /**
     * 设置rvlist动画
     * @param adapter
     */
    protected <T, VH extends BaseViewHolder> void setAdapterAnimation(BaseQuickAdapter<T, VH> adapter) {
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

    protected <T, VH extends BaseViewHolder> void setLoadState(BaseQuickAdapter<T, VH> adapter, boolean loadState) {
        isErr = loadState;
        adapter.getLoadMoreModule().loadMoreComplete();
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
    protected void onStart() {
        super.onStart();
        if (null != mPresenter)
            mPresenter.registerEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mPresenter)
            mPresenter.unregisterEventBus();
    }

    @Override
    protected void onDestroy() {
        //取消View的关联
        if (null != mPresenter)
            mPresenter.detachView();
        mUnBinder.unbind();
        alertDialog = null;
        super.onDestroy();
    }

    protected void emptyRecyclerView(RecyclerView... recyclerViews) {
        for (RecyclerView rv : recyclerViews) {
            if (!Utils.isNullOrEmpty(rv))
                rv.setAdapter(null);
        }
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
     * 图片懒加载
     */
    protected void lazyLoadImg() {
        if (!Utils.isNullOrEmpty(lazyLoadImgListener))
            lazyLoadImgListener.loadImg();
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
                !getRunningActivityName().equals("ImageActivity") &&
                !getRunningActivityName().equals("ImagePreviewActivity")) {
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
}
