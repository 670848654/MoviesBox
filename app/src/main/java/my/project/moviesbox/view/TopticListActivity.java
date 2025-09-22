package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VodListAdapter;
import my.project.moviesbox.contract.TopTicListContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.databinding.ActivityVodListBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.TopticListModel;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.presenter.TopticListPresenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseMvpActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: TopticListActivity
  * @描述: 专题列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:13
  * @版本: 1.0
 */
public class TopticListActivity extends BaseMvpActivity<TopticListModel, TopTicListContract.View, TopticListPresenter, ActivityVodListBinding> implements TopTicListContract.View {
    private VodListAdapter adapter;
    private final List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    private String title, url;
    private boolean isVodList; // 是否是子视频列表

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityVodListBinding inflateBinding(LayoutInflater inflater) {
        return ActivityVodListBinding.inflate(inflater);
    }

    private AppBarLayout appBar;
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipe;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        mRecyclerView = binding.contentLayout.rvList;
        mSwipe = binding.contentLayout.mSwipe;
    }

    @Override
    public void initClickListeners() {}

    @Override
    protected TopticListPresenter createPresenter() {
        return new TopticListPresenter(this);
    }

    @Override
    protected void loadData() {
        multiItemEntities.clear();
        mPresenter.loadMainData(true, url, isVodList, page);
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        url = bundle.getString("url");
        isVodList = bundle.getBoolean("isVodList");
        setToolbar(toolbar, title, "");
        initSwipe();
        initDefaultAdapter();
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> retryListener());
    }

    public void initDefaultAdapter() {
        adapter = new VodListAdapter(multiItemEntities);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
            VodDataBean bean = (VodDataBean) adapter.getItem(position);
            String url = bean.getUrl();
            String title = bean.getTitle();
            openList(title, url);
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (page >= pageCount) {
                //数据全部加载完毕
                adapter.getLoadMoreModule().loadMoreEnd();
                application.showToastMsg(getString(R.string.noMoreContent), DialogXTipEnum.SUCCESS);
            } else {
                if (isErr) {
                    //成功获取更多数据
                    page++;
                    mPresenter.loadPageData(url, isVodList, page);
                    application.showToastMsg(String.format(LOAD_PAGE_AND_ALL_PAGE, (parserInterface.startPageNum() == 0 ? page+1 : page), pageCount), DialogXTipEnum.DEFAULT);
                } else {
                    //获取更多数据失败
                    page--;
                    isErr = true;
                    adapter.getLoadMoreModule().loadMoreFail();
                }
            }
        }, 500));
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

    @Override
    public void onResume() {
        super.onResume();
        if (multiItemEntities.size() == 0)
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
        int spanCount;
        if (multiItemEntities.size() > 0 && multiItemEntities.get(0).getItemType() == VodItemStyleEnum.STYLE_16_9.getType()) {
            spanCount = parserInterface.setVodList16_9ItemSize(Utils.isPad(), isPortrait, false);
        } else {
            spanCount = parserInterface.setVodListItemSize(Utils.isPad(), isPortrait, false);
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
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
    public void success(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (firstTimeData) {
                multiItemEntities.addAll(vodDataBeans);
                hideProgress();
                this.pageCount = pageCount;
                mSwipe.setRefreshing(false);
                new Handler().postDelayed(() -> {
                    adapter.setNewInstance(multiItemEntities);
                    setRecyclerViewView();
                }, 500);
            } else {
                adapter.addData(vodDataBeans);
                setLoadState(adapter, true);
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
                    R.drawable.round_warning_24,
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
                setLoadState(adapter, false);
                application.showToastMsg(msg, DialogXTipEnum.ERROR);
            }
        });
    }

    @Override
    public void empty(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            multiItemEntities.clear();
            setRecyclerViewEmpty();
            rvEmpty(msg);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        emptyRecyclerView(mRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            View view = findViewById(R.id.action_search);
            Utils.setVibration(view);
            startActivity(new Intent(this, parserInterface.searchOpenClass()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}