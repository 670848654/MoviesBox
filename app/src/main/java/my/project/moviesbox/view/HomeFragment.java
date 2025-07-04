package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.CHANGE_SOURCES;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.VOD_LIST;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jzvd.Jzvd;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.HomeAdapter;
import my.project.moviesbox.adapter.SourceListAdapter;
import my.project.moviesbox.contract.HomeContract;
import my.project.moviesbox.custom.FabExtendingOnScrollListener;
import my.project.moviesbox.custom.VideoPreviewDialog;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.model.HomeModel;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.presenter.HomePresenter;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.lazyLoadImage.VodListLazyImgActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: HomeFragment
  * @描述: 首页数据视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:11
  * @版本: 1.0
 */
public class HomeFragment extends BaseFragment<HomeModel, HomeContract.View, HomePresenter> implements HomeContract.View, HomeAdapter.OnItemClick, SourceListAdapter.OnItemClick {
    private View view;
    /*@BindView(R.id.source_title)
    TextView sourceTitleView;*/
    @BindView(R.id.change_source)
    Button sourceTitleView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipe;
    List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    protected HomeAdapter adapter;
    @BindView(R.id.search)
    ExtendedFloatingActionButton searchFAB;
    private int fabHeight = 0;
    private SourceListAdapter sourceListAdapter;
    private BottomSheetDialog sourceListBottomSheetDialog;
    private WebView webView;

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        initHeader();
        initSwipe();
        initAdapter();
        return view;
    }

    private void initHeader() {
        String sourceName = parserInterface.getSourceName();
        char firstChar = sourceName.charAt(0);
        String newStr = "<font color='#f48fb1'><strong>"+firstChar+"</strong></font>" + sourceName.substring(1);
        sourceTitleView.setText(Html.fromHtml(newStr));
    }

    private void initSwipe() {
        mSwipe.setEnabled(false);
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(this::refreshData);
    }

    @OnClick({R.id.search, R.id.change_source, R.id.open_web_view})
    public void viewClick(View view) {
        Utils.setVibration(view);
        switch (view.getId()) {
            case R.id.search:
                startActivity(new Intent(getActivity(), parserInterface.searchOpenClass()));
                break;
            case R.id.change_source:
                sourceListBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                sourceListBottomSheetDialog.show();
                break;
            case R.id.open_web_view:
                if (SharedPreferencesUtils.getByPassCF())
                    openWebView();
                else
                    application.showToastMsg(getString(R.string.onlyEnableByPassCF), DialogXTipEnum.WARNING);
                break;
        }
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        searchFAB.setVisibility(View.GONE);
        multiItemEntities.clear();
        adapter.setNewInstance(null);
        loadData();
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new HomeAdapter(getActivity(), multiItemEntities, isPortrait, this);
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
            homeActivity.setAdapterAnimation(adapter);
        adapter.addChildClickViewIds(R.id.more);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (adapter.getItemViewType(position) == ITEM_LIST.getType()
                   || adapter.getItemViewType(position) == VOD_LIST.getType()) { // 更多点击事件
                Utils.setVibration(view);
                MainDataBean mainDataBean = (MainDataBean) adapter.getData().get(position);
                if (!mainDataBean.isHasMore()) return;
                Bundle bundle = new Bundle();
                if (mainDataBean.getOpenMoreClass().equals(ClassificationVodListActivity.class)) {
                    // 打开分类列表界面视图
                    String title = mainDataBean.getTitle();
                    // 接口定义的长度 且 分页参数为最后一个
                    String[] params = new String[parserInterface.setClassificationParamsSize()];
                    params[0] = mainDataBean.getMore();
                    params[params.length - 1] = String.valueOf(parserInterface.startPageNum());
                    bundle.putString("title", title);
                    bundle.putStringArray("params", params);
                } else if (mainDataBean.getOpenMoreClass().equals(VodListActivity.class) ||
                        mainDataBean.getOpenMoreClass().equals(TextListActivity.class) ||
                        mainDataBean.getOpenMoreClass().equals(TopticListActivity.class) ||
                        mainDataBean.getOpenMoreClass().equals(VodListLazyImgActivity.class)) {
                    // 打开对应列表界面视图
                    bundle.putString("title", mainDataBean.getTitle());
                    bundle.putString("url", mainDataBean.getMore());
                }
                startActivity(new Intent(getActivity(), mainDataBean.getOpenMoreClass()).putExtras(bundle));
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.setEmptyView(rvView);
        // 监听 RecyclerView 的滑动事件
        recyclerView.addOnScrollListener(new FabExtendingOnScrollListener(searchFAB));

        View sourceListView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_source_list, null);
        RecyclerView sourceListRecyclerView = sourceListView.findViewById(R.id.rv_list);
        sourceListAdapter = new SourceListAdapter(SourceEnum.getSourceDataBeanList(), this);
        sourceListAdapter.setAnimationEnable(true);
        sourceListAdapter.setAdapterAnimation(new AlphaInAnimation());
        sourceListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sourceListRecyclerView.setAdapter(sourceListAdapter);
        sourceListBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        sourceListBottomSheetDialog.setContentView(sourceListView);
    }

    private void openWebView() {
        BottomSheetDialog webviewBottomSheetDialog = new BottomSheetDialog(getActivity());
        FrameLayout view = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_webview, null);
        webView = view.findViewById(R.id.webView);
        Button getCookie = view.findViewById(R.id.getCookie);
        getCookie.setOnClickListener(v -> {
            webviewBottomSheetDialog.dismiss();
            retryListener();
        });
        // 配置 WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        webviewBottomSheetDialog.setOnDismissListener(dialog -> {
            webView.stopLoading();
            webView.onPause();
        });

        // 加载 URL
        webView.loadUrl(parserInterface.getDefaultDomain());

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

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected void retryListener() {
        refreshData();
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEnum refresh) {
        switch (refresh) {
            case REFRESH_INDEX:
                HomeActivity homeActivity = (HomeActivity) getActivity();
                if (homeActivity != null)
                    homeActivity.startRssService();
                multiItemEntities.clear();
                adapter.notifyDataSetChanged();
                mPresenter.loadData(true);
                break;
        }
    }

    @Override
    public void onTagClick(MainDataBean.Tag tag) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (Utils.isNullOrEmpty(tag.getOpenClass()))
                application.showToastMsg(getString(R.string.openActivityIsUndefined), DialogXTipEnum.ERROR);
            else {
                Bundle bundle = new Bundle();
                if (tag.getOpenClass().equals(WeekActivity.class)) {
                    // 跳转新番时间表视图
                    bundle.putString("title", tag.getTitle());
                    bundle.putString("url", String.format(tag.getUrl(), parserInterface.getDefaultDomain()));
                } else if (tag.getOpenClass().equals(ClassificationVodListActivity.class)) {
                    // 跳转分类视图
                    // 接口定义的长度 且 分页参数为最后一个
                    String[] params = new String[parserInterface.setClassificationParamsSize()];
                    params[0] = tag.getUrl();
                    params[params.length-1] = String.valueOf(parserInterface.startPageNum());
                    bundle.putString("title", tag.getTitle());
                    bundle.putStringArray("params", params);
                } else if (tag.getOpenClass().equals(TopticListActivity.class)) {
                    // 跳转动漫专题视图
                    bundle.putString("title", tag.getTitle());
                    bundle.putString("url", tag.getUrl());
                    bundle.putBoolean("isVodList", false);
                } else if (tag.getOpenClass().equals(VodListActivity.class) ||
                        tag.getOpenClass().equals(TextListActivity.class) ||
                        tag.getOpenClass().equals(VodListLazyImgActivity.class)) {
                    // 跳转列表视图
                    bundle.putString("title", tag.getTitle());
                    bundle.putString("url", tag.getUrl());
                }
                startActivity(new Intent(getActivity(), tag.getOpenClass()).putExtras(bundle));
            }
        });
    }

    /**
     * 存在下拉菜单时点击接口
     *
     * @param dropDownMenus
     */
    @Override
    public void onDropDownTagClick(List<MainDataBean.DropDownTag.DropDownMenu> dropDownMenus, View chip) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            PopupMenu popupMenu = new PopupMenu(getActivity(), chip);
            for (int i=0,size=dropDownMenus.size(); i<size; i++) {
                popupMenu.getMenu().add(Menu.NONE,  Menu.NONE+i, Menu.NONE, dropDownMenus.get(i).getTitle());
            }
            popupMenu.setOnMenuItemClickListener(item1 -> {
                int position = item1.getItemId();
                MainDataBean.DropDownTag.DropDownMenu dropDownMenu = dropDownMenus.get(position);
                setDropDownTagOpenClass(dropDownMenu.getOpenClass(), dropDownMenu.getTitle(), dropDownMenu.getUrl());
                return true;
            });
            popupMenu.show();
        });
    }

    /**
     * 不存在下拉菜单时点击接口
     *
     * @param dropDownTag
     * @param view
     */
    @Override
    public void onDropDownTagNoMenusClick(MainDataBean.DropDownTag dropDownTag, View view) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> setDropDownTagOpenClass(dropDownTag.getOpenClass(), dropDownTag.getTitle(), dropDownTag.getUrl()));
    }

    /**
     * 下拉菜单跳转视图公共方法
     * @param openClass
     * @param title
     * @param url
     */
    private void setDropDownTagOpenClass(Class openClass, String title, String url) {
        if (Utils.isNullOrEmpty(openClass)) {
            application.showToastMsg(getString(R.string.openActivityIsUndefined), DialogXTipEnum.ERROR);
        } else {
            Bundle bundle = new Bundle();
            if (openClass.equals(ClassificationVodListActivity.class)) {
                // 跳转分类视图
                // 接口定义的长度 且 分页参数为最后一个
                String[] params = new String[parserInterface.setClassificationParamsSize()];
                params[0] = url;
                params[params.length-1] = String.valueOf(parserInterface.startPageNum());
                bundle.putString("title", title);
                bundle.putStringArray("params", params);
            }
            startActivity(new Intent(getActivity(), openClass).putExtras(bundle));
        }
    }

    @Override
    public void onVideoClick(MainDataBean.Item data) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("title", data.getTitle());
            bundle.putString("url", data.getUrl());
            startActivity(new Intent(getActivity(), data.getOpenClass()).putExtras(bundle));
        });
    }

    /**
     * ITEM长按点击接口
     *
     * @param data
     */
    @Override
    public void onVideoLongClick(MainDataBean.Item data) {
        if (getActivity() == null) return;
        if (Utils.isNullOrEmpty(data.getPreviewUrl())) return;
        getActivity().runOnUiThread(() -> {
            VideoPreviewDialog dialog = new VideoPreviewDialog(this.getActivity(), data.getTitle(), data.getPreviewUrl());
            dialog.show();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos(); // 万一还在播放，及时清理
    }

    @Override
    public void loadingView() {
        mSwipe.setRefreshing(false);
        mSwipe.setEnabled(false);
        rvLoading();
    }

    @Override
    public void errorView(String msg) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            mSwipe.setEnabled(true);
            Utils.showAlert(getActivity(),
                    getString(R.string.errorDialogTitle),
                    msg,
                    false,
                    getString(R.string.retryBtnText),
                    getString(R.string.closeBtnText),
                    "",
                    (dialog, which) -> refreshData(),
                    (dialog, which) -> dialog.dismiss(),
                    null);
            rvError(msg);
        });
    }

    @Override
    public void emptyView() {
    }

    @Override
    public void success(List<MainDataBean> mainDataBeans) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            mSwipe.setEnabled(true);
            hideProgress();
            new Handler().postDelayed(() -> {
                multiItemEntities.addAll(mainDataBeans);
                adapter.setNewInstance(multiItemEntities);
                searchFAB.setVisibility(View.VISIBLE);
                if (fabHeight == 0) {
                    // 添加布局完成监听器
                    ViewTreeObserver viewTreeObserver = searchFAB.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            fabHeight = searchFAB.getHeight();
                            recyclerView.setPadding(0,0,0, fabHeight+Utils.dpToPx(getActivity(), 32));
                            // 由于已经获取了高度，因此可以移除监听器，避免重复调用
                            searchFAB.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                }
                lazyLoadImg();
            }, 500);
        });
    }

    /**
     * 发布页
     *
     * @param url
     */
    @Override
    public void onWebsiteReleaseClick(String url) {
        Utils.viewInChrome(getActivity(), url);
    }

    /**
     * RSS
     *
     * @param url
     */
    @Override
    public void onRssClick(String url) {
        Utils.viewInChrome(getActivity(), url);
    }

    /**
     * 换源
     *
     * @param source
     */
    @Override
    public void onChangeSource(int source) {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        SharedPreferencesUtils.setDefaultSource(source);
        EventBus.getDefault().post(CHANGE_SOURCES);
    }
}
