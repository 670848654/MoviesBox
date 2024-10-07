package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VodListAdapter;
import my.project.moviesbox.contract.SearchContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.SearchModel;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.presenter.SearchPresenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: SearchActivity
  * @描述: 搜索列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:12
  * @版本: 1.0
 */
public class SearchActivity extends BaseActivity<SearchModel, SearchContract.View, SearchPresenter> implements SearchContract.View {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.rv_list)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.mSwipe)
    protected SwipeRefreshLayout mSwipe;
    protected  VodListAdapter adapter;
    protected  final List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    protected  String title;
    protected  SearchView mSearchView;
    protected  boolean isSearch = false;

    @Override
    protected SearchPresenter createPresenter() {
        return new SearchPresenter(this);
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_vod_list;
    }

    @Override
    protected void init() {
        setToolbar(toolbar, "", "");
        initSwipe();
        initDefaultAdapter();
    }

    @Override
    protected void initBeforeView() {}

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setEnabled(false);
        mSwipe.setOnRefreshListener(this::retryListener);
    }

    public void initDefaultAdapter() {
        adapter = new VodListAdapter(multiItemEntities);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            VodDataBean bean = (VodDataBean) adapter.getItem(position);
            String url = bean.getUrl();
            String title = bean.getTitle();
            openVodDesc(title, url);
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            mRecyclerView.postDelayed(() -> {
                isSearch = true;
                if (page >= pageCount) {
                    //数据全部加载完毕
                    adapter.getLoadMoreModule().loadMoreEnd();
                    isSearch = false;
                    application.showToastMsg(getString(R.string.noMoreContent), DialogXTipEnum.SUCCESS);
                } else {
                    if (isErr) {
                        //成功获取更多数据
                        page++;
                        mPresenter.loadPageData(title, String.valueOf(page));
                        application.showToastMsg(String.format(LOAD_PAGE_AND_ALL_PAGE, page, pageCount), DialogXTipEnum.DEFAULT);
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
    }

    private void setSubTitle() {
        toolbar.setSubtitle(String.format(PAGE_AND_ALL_PAGE, page, pageCount));
    }

    public void openVodDesc(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        startActivity(new Intent(SearchActivity.this, DetailsActivity.class).putExtras(bundle));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        final MenuItem item = menu.findItem(R.id.search);
//        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.onActionViewExpanded();
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("请输入检索关键字");
        mSearchView.setMaxWidth(4000);
        if (!Utils.isNullOrEmpty(title)) {
            mSearchView.setQuery(title, false);
            mSearchView.clearFocus();
            Utils.hideKeyboard(mSearchView);
        }
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isSearch) {
                    application.showToastMsg(getString(R.string.searchPleaseTryAgainLater), DialogXTipEnum.WARNING);
                }else {
                    title = query.trim();
                    if (!title.isEmpty()) {
                        page = parserInterface.startPageNum();
                        pageCount = parserInterface.startPageNum();
                        multiItemEntities.clear();
                        mPresenter.loadMainData(true, title, String.valueOf(page));
                        toolbar.setTitle(title);
                        mSearchView.clearFocus();
                        Utils.hideKeyboard(mSearchView);
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
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
        mPresenter.loadMainData(true, title, String.valueOf(page));
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
        if (isFinishing()) return;
        isSearch = true;
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
            isSearch = false;
            mSwipe.setEnabled(true);
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
            isSearch = false;
            mSwipe.setEnabled(true);
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
    public void empty(boolean firstTimeData,String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            isSearch = false;
            mSwipe.setEnabled(true);
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
        emptyRecyclerView(mRecyclerView);
        super.onDestroy();
    }
}