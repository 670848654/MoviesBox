package my.project.moviesbox.view;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.Unbinder;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DirectoryAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.model.BaseModel;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoAlertUtils;
import my.project.moviesbox.view.lazyLoadImage.LazyLoadImgListener;

/**
  * @包名: my.project.moviesbox.view
  * @类名: BaseFragment
  * @描述: fragment基类
  * @作者: Li Z
  * @日期: 2024/2/4 17:06
  * @版本: 1.0
 */
public abstract class BaseFragment< M extends BaseModel,V, P extends Presenter<V, M>> extends Fragment {
    protected static final int DIRECTORY_REQUEST_CODE = 0x10010;
    protected static final int DIRECTORY_CONFIG_RESULT_CODE = 0x10011;
    protected DirectoryPopupWindowAdapterClickListener directoryPopupWindowAdapterClickListener;
    protected P mPresenter;
    protected App application;
    protected Unbinder mUnBinder;
    protected int position = 0;
    protected boolean isPortrait;
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
    protected PopupWindow popupWindow;

    protected LazyLoadImgListener lazyLoadImgListener;

    protected VideoAlertUtils videoAlertUtils;

    protected void setLazyLoadImgListener(LazyLoadImgListener lazyLoadImgListener) {
        this.lazyLoadImgListener = lazyLoadImgListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration mConfiguration = getResources().getConfiguration();
        change = mConfiguration.orientation;
        if (change == mConfiguration.ORIENTATION_LANDSCAPE) isPortrait = false;
        else if (change == mConfiguration.ORIENTATION_PORTRAIT) isPortrait = true;
        mPresenter = createPresenter();
        initCustomViews();
        if (application == null) application = (App) getActivity().getApplication();
        View view = initViews(inflater, container, savedInstanceState);
        EventBus.getDefault().register(this);
        videoAlertUtils = new VideoAlertUtils(this.getActivity());
        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mPresenter)
            mPresenter.registerEventBus();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != mPresenter)
            mPresenter.unregisterEventBus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消View的关联
        if (null != mPresenter)
            mPresenter.detachView();
        if (null != videoAlertUtils)
            videoAlertUtils.release();
        EventBus.getDefault().unregister(this);
        try {
            mUnBinder.unbind();
        } catch (IllegalStateException e) {}
    }

    /**
     * 初始化自定义视图
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
     * 刷新视图
     */
    protected void rvLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    /**
     * 隐藏加载进度条
     */
    protected void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 空视图
     * @return
     */
    protected void rvEmpty(String msg) {
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
        hideProgress();
        errorMsgView.setText(msg);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 防止两次调用
        if (newConfig.orientation == change) return;
        change = newConfig.orientation;
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setConfigurationChanged();
    }

    /**
     * 设置菜单通用方法
     * @param view 依附视图
     * @param menuRes 菜单布局
     * @param menuId 需要设置颜色的文本ID
     * @param listener 点击监听事件
     */
    @SuppressLint("RestrictedApi")
    protected void setMenu(View view, @MenuRes int menuRes, int menuId, PopupMenu.OnMenuItemClickListener listener) {
        final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());
        if (menuId != -1) {
            SpannableString ss = new SpannableString(popupMenu.getMenu().findItem(menuId).getTitle());
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getActivity().getColor(R.color.delete_color));
            ss.setSpan(foregroundColorSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            popupMenu.getMenu().findItem(menuId).setTitle(ss);
        }
        MenuBuilder menuBuilder = (MenuBuilder) popupMenu.getMenu();
        menuBuilder.setOptionalIconsVisible(true);
        popupMenu.setOnMenuItemClickListener(listener);
        popupMenu.show();
        registerForContextMenu(view);
    }

    /**
     * 图片懒加载
     */
    protected void lazyLoadImg() {
        if (!Utils.isNullOrEmpty(lazyLoadImgListener))
            lazyLoadImgListener.loadImg();
    }

    protected abstract void setConfigurationChanged();

    protected abstract View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected abstract P createPresenter();

    protected abstract void loadData();

    /**
     * 点击重试接口
     * @return
     */
    protected abstract void retryListener();

    public abstract void onEvent(RefreshEnum refresh);

    /**
     * 目录清单列表适配器点击接口
     */
    public interface DirectoryPopupWindowAdapterClickListener {
        void onItemClickListener(TDirectory tDirectory);
    }

    public void setDirectoryPopupWindowAdapterClickListener(DirectoryPopupWindowAdapterClickListener listener) {
        this.directoryPopupWindowAdapterClickListener = listener;
    }

    /**
     * 显示目录清单PopupWindow
     * @param tDirectories
     * @param showAsView
     */
    protected void showDirectoryPopupWindow(List<TDirectory> tDirectories, View showAsView) {
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_directory_selection, null);
        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DirectoryAdapter directoryAdapter = new DirectoryAdapter(false, tDirectories);
        recyclerView.setAdapter(directoryAdapter);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置 PopupWindow 背景变暗
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = 0.5f; // 设置透明度，值越小背景越暗
        getActivity().getWindow().setAttributes(layoutParams);
        // 当 PopupWindow 消失时恢复背景亮度
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.alpha = 1.0f; // 恢复透明度
            getActivity().getWindow().setAttributes(lp);
        });
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setAnimationStyle(R.style.PopupTopAnim);
        // 显示 PopupWindow
        popupWindow.showAsDropDown(showAsView);
        directoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            directoryPopupWindowAdapterClickListener.onItemClickListener(tDirectories.get(position));
        });
    }
}
