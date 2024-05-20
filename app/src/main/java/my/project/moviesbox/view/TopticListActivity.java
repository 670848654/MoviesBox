package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VodListAdapter;
import my.project.moviesbox.contract.TopTicListContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.presenter.TopticListPresenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: TopticListActivity
  * @描述: 专题列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:13
  * @版本: 1.0
 */
public class TopticListActivity extends BaseActivity<TopTicListContract.View, TopticListPresenter> implements TopTicListContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private VodListAdapter adapter;
    private List<VodDataBean.Item> items = new ArrayList<>();
    private String title, url;
    private int page = parserInterface.startPageNum(); // 开始页码
    private int pageCount = parserInterface.startPageNum();
    private boolean isErr = true;
    private boolean isVodList; // 是否是子视频列表

    @Override
    protected TopticListPresenter createPresenter() {
        return new TopticListPresenter(url, isVodList, page,this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_vod_list;
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        url = bundle.getString("url");
        isVodList = bundle.getBoolean("isVodList");
        initToolbar();
        initSwipe();
        initDefaultAdapter();
    }

    @Override
    protected void initBeforeView() {

    }

    public void initToolbar() {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            finish();
        });
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> retryListener());
    }

    public void initDefaultAdapter() {
        adapter = new VodListAdapter(isVodList ? parserInterface.setVodListItemType() : parserInterface.setTopticListItemType(), items);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            VodDataBean.Item bean = (VodDataBean.Item) adapter.getItem(position);
            String url = bean.getUrl();
            String title = bean.getTitle();
            openList(title, url);
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            mRecyclerView.postDelayed(() -> {
                if (page >= pageCount) {
                    //数据全部加载完毕
                    adapter.getLoadMoreModule().loadMoreEnd();
                    application.showToastMsg(getString(R.string.noMoreContent));
                } else {
                    if (isErr) {
                        //成功获取更多数据
                        page++;
                        mPresenter = createPresenter();
                        mPresenter.loadData(false);
                        application.showToastMsg(String.format(LOAD_PAGE_AND_ALL_PAGE, page, pageCount));
                    } else {
                        //获取更多数据失败
                        isErr = true;
                        adapter.getLoadMoreModule().loadMoreFail();
                    }
                }
            }, 500);
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        mRecyclerView.setAdapter(adapter);
        adapter.setEmptyView(rvView);
    }

    /*private void setSubTitle() {
        toolbar.setSubtitle(String.format(PAGE_ADN_ALL_PAGE, page, pageCount));
    }*/

    public void openList(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        if (!isVodList) {
            // 当前是专题列表
            bundle.putBoolean("isVodList", true);
            startActivity(new Intent(this, TopticListActivity.class).putExtras(bundle));
        } else {
            // 当前是视频列表
            startActivity(new Intent(this, DetailsActivity.class).putExtras(bundle));
        }
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.getLoadMoreModule().loadMoreComplete();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (items.size() == 0)
            setRecyclerViewEmpty();
        else
            setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    @Override
    protected void retryListener() {
        page = parserInterface.startPageNum();
        pageCount = parserInterface.startPageNum();
        loadData();
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, isVodList ? parserInterface.setVodListItemSize(Utils.isPad(), isPortrait) :  parserInterface.setTopticItemListItemSize(Utils.isPad(), isPortrait)));
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void loadingView() {
        mSwipe.setRefreshing(false);
        rvLoading();
    }

    @Override
    public void errorView(String msg) {

    }

    @Override
    public void emptyView() {
        if (isFinishing()) return;
        adapter.setNewInstance(null);
        setRecyclerViewEmpty();
    }

    @Override
    public void success(boolean firstTimeData, VodDataBean vodDataBean, int pageCount) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            items = vodDataBean.getItemList();
            if (firstTimeData) {
                hideProgress();
                this.pageCount = pageCount;
                mSwipe.setRefreshing(false);
                new Handler().postDelayed(() -> {
                    adapter.setNewInstance(items);
                    setRecyclerViewView();
                }, 500);
            } else {
                adapter.addData(items);
                setLoadState(true);
            }
        });
    }

    @Override
    public void error(boolean firstTimeData, String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (firstTimeData) {
            mSwipe.setRefreshing(false);
            rvError(msg);
            Utils.showAlert(this,
                    getString(R.string.errorDialogTitle),
                    msg,
                    false,
                    getString(R.string.defaultPositiveBtnText),
                    "",
                    "",
                    (dialog, which) -> dialog.dismiss(),
                    null,
                    null);
            } else {
                setLoadState(false);
                application.showToastMsg(msg);
            }
        });
    }

    @Override
    public void empty(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            items.clear();
            setRecyclerViewEmpty();
            rvEmpty(msg);
        });
    }
}