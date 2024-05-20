package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.HomeAdapter;
import my.project.moviesbox.contract.HomeContract;
import my.project.moviesbox.custom.FabExtendingOnScrollListener;
import my.project.moviesbox.event.RefreshEvent;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.presenter.HomePresenter;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: HomeFragment
  * @描述: 首页数据视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:11
  * @版本: 1.0
 */
public class HomeFragment extends BaseFragment<HomeContract.View, HomePresenter> implements HomeContract.View, HomeAdapter.OnItemClick {
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
    private HomeAdapter adapter;
    @BindView(R.id.search)
    ExtendedFloatingActionButton searchFAB;
    private int fabHeight = 0;

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
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() ->
            refreshData()
        );
    }

    @OnClick({R.id.search, R.id.change_source})
    public void viewClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.change_source:
                Utils.showSingleChoiceAlert(
                        getActivity(),
                        "切换站点",
                        SourceEnum.getSourcesArr(),
                        true,
                        SharedPreferencesUtils.getDefaultSource(),
                        (dialogInterface, i) -> {
                            if (SharedPreferencesUtils.getDefaultSource() == i) {
                                // 当前选择的是当前源
                                dialogInterface.dismiss();
                                return;
                            }
                            SharedPreferencesUtils.setDefaultSource(i);
                            EventBus.getDefault().post(new RefreshEvent(-1));
                            dialogInterface.dismiss();
                        }
                );
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
        adapter = new HomeAdapter(getActivity(), multiItemEntities, this);
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
            homeActivity.setAdapterAnimation(adapter);
        adapter.addChildClickViewIds(R.id.more);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (adapter.getItemViewType(position)) {
                case MainDataBean.ITEM_LIST: // 更多点击事件
                    MainDataBean mainDataBean = (MainDataBean) adapter.getData().get(position);
                    if (!mainDataBean.isHasMore()) return;
                    Bundle bundle = new Bundle();
                    if (mainDataBean.getOpenMoreClass().equals(ClassificationVodListActivity.class)) {
                        // 打开分类列表界面视图
                        String title = mainDataBean.getTitle();
                        // 接口定义的长度 且 分页参数为最后一个
                        String[] params = new String[parserInterface.setClassificationParamsSize()];
                        params[0] = mainDataBean.getMore();
                        params[params.length-1] = String.valueOf(parserInterface.startPageNum());
                        bundle.putString("title", title);
                        bundle.putStringArray("params", params);
                    } else if (mainDataBean.getOpenMoreClass().equals(VodListActivity.class) || mainDataBean.getOpenMoreClass().equals(TextListActivity.class) || mainDataBean.getOpenMoreClass().equals(TopticListActivity.class)) {
                        // 打开对应列表界面视图
                        bundle.putString("title", mainDataBean.getTitle());
                        bundle.putString("url", mainDataBean.getMore());
                    }
                    startActivity(new Intent(getActivity(), mainDataBean.getOpenMoreClass()).putExtras(bundle));
                    break;
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.setEmptyView(rvView);
        // 监听 RecyclerView 的滑动事件
        recyclerView.addOnScrollListener(new FabExtendingOnScrollListener(searchFAB));
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent refresh) {
        switch (refresh.getIndex()) {
            case 0:
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
                Toast.makeText(getActivity(), "NO OPEN CLASS...", Toast.LENGTH_SHORT).show();
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
                } else if (tag.getOpenClass().equals(VodListActivity.class) || tag.getOpenClass().equals(TextListActivity.class)) {
                    bundle.putString("title", tag.getTitle());
                    bundle.putString("url", tag.getUrl());
                }
                startActivity(new Intent(getActivity(), tag.getOpenClass()).putExtras(bundle));
            }
        });
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
                for (MainDataBean homeBean : mainDataBeans) {
                    multiItemEntities.add(homeBean);
                }
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
            }, 500);
        });
    }
}
