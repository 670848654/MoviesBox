package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VodListAdapter;
import my.project.moviesbox.contract.VodListContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.custom.FabExtendingOnScrollListener;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.VodListModel;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.presenter.VodListPresenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: VodListActivity
  * @描述: 影视列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:13
  * @版本: 1.0
 */
public class VodListActivity extends BaseActivity<VodListModel, VodListContract.View, VodListPresenter> implements VodListContract.View {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.rv_list)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private VodListAdapter adapter;
    private final List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    protected String title, url;
    @BindView(R.id.classFab)
    protected ExtendedFloatingActionButton classFab;
    protected int fabHeight = 0;

    @Override
    protected VodListPresenter createPresenter() {
        return new VodListPresenter(this);
    }

    @Override
    protected void loadData() {
        multiItemEntities.clear();
        mPresenter.loadMainData(true, url, page);
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
        setToolbar(toolbar, title, "");
        initSwipe();
        initDefaultAdapter();
    }

    @Override
    protected void initBeforeView() {

    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(()-> retryListener());
    }

    public void initDefaultAdapter() {
        adapter = new VodListAdapter(multiItemEntities);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            VodDataBean bean = (VodDataBean) adapter.getItem(position);
            String url = bean.getUrl();
            String title = bean.getTitle();
            openVodDetail(title, url);
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            mRecyclerView.postDelayed(() -> {
                if (page >= pageCount) {
                    //数据全部加载完毕
                    adapter.getLoadMoreModule().loadMoreEnd();
                    application.showToastMsg(getString(R.string.noMoreContent), DialogXTipEnum.DEFAULT);
                } else {
                    if (isErr) {
                        //成功获取更多数据
                        page++;
                        mPresenter.loadPageData(url, page);
                        application.showToastMsg(String.format(LOAD_PAGE_AND_ALL_PAGE, page, pageCount), DialogXTipEnum.SUCCESS);
                    } else {
                        //获取更多数据失败
                        page--;
                        isErr = true;
                        adapter.getLoadMoreModule().loadMoreFail();
                    }
                }
            }, 500);
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        mRecyclerView.setAdapter(adapter);
        adapter.setEmptyView(rvView);
        // 监听 RecyclerView 的滑动事件
        mRecyclerView.addOnScrollListener(new FabExtendingOnScrollListener(classFab));
    }

    private void setSubTitle() {
        toolbar.setSubtitle(String.format(PAGE_AND_ALL_PAGE, page, pageCount));
    }

    public void openVodDetail(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        startActivity(new Intent(VodListActivity.this, DetailsActivity.class).putExtras(bundle));
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
            spanCount = parserInterface.setVodList16_9ItemSize(Utils.isPad(), isPortrait);
        } else {
            spanCount = parserInterface.setVodListItemSize(Utils.isPad(), isPortrait);
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void loadingView() {
        mSwipe.setRefreshing(false);
        toolbar.setSubtitle("");
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
                mSwipe.setRefreshing(false);
                this.pageCount = pageCount;
                new Handler().postDelayed(() -> {
                    adapter.setNewInstance(multiItemEntities);
                    setRecyclerViewView();
                }, 500);
            } else {
                adapter.addData(vodDataBeans);
                setLoadState(adapter, true);
            }
            setSubTitle();
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
                setLoadState(adapter, false);
                application.showToastMsg(msg, DialogXTipEnum.ERROR);
            }
        });
    }

    @Override
    public void empty(boolean firstTimeData, String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (firstTimeData) {
                mSwipe.setRefreshing(false);
                rvEmpty(msg);
            } else {
                setLoadState(adapter, false);
                application.showToastMsg(msg, DialogXTipEnum.ERROR);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        emptyRecyclerView(mRecyclerView);
    }
}