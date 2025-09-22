package my.project.moviesbox.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.SearchHistoryAdapter;
import my.project.moviesbox.adapter.VodListAdapter;
import my.project.moviesbox.contract.SearchContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.custom.VideoPreviewDialog;
import my.project.moviesbox.databinding.ActivitySearchListBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.SearchModel;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.presenter.SearchPresenter;
import my.project.moviesbox.utils.SearchHistoryManager;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseMvpActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: SearchActivity
  * @描述: 搜索列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:12
  * @版本: 1.0
 */
public class SearchActivity extends BaseMvpActivity<SearchModel, SearchContract.View, SearchPresenter, ActivitySearchListBinding> implements SearchContract.View {
    protected AppBarLayout appBar;
    protected MaterialToolbar toolbar;
    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipe;
    protected  VodListAdapter adapter;
    protected  final List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    protected  String searchContent;
    protected SearchBar searchBar;
    protected SearchView searchView;
    protected RecyclerView rvHistory;
    protected  boolean isSearch = false;

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivitySearchListBinding inflateBinding(LayoutInflater inflater) {
        return ActivitySearchListBinding.inflate(inflater);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.appBar;
        toolbar = binding.toolbar;
        searchBar = binding.searchBar;
        searchView = binding.searchView;
        rvHistory = binding.rvHistory;
        mRecyclerView = binding.contentLayout.rvList;
        mSwipe = binding.contentLayout.mSwipe;
    }

    @Override
    public void initClickListeners() {

    }

    @Override
    protected SearchPresenter createPresenter() {
        return new SearchPresenter(this);
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected void init() {
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        setToolbar(toolbar, "", "");
        initSwipe();
        initDefaultAdapter();
        initSearchBar();
    }

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
            Utils.setVibration(view);
            VodDataBean bean = (VodDataBean) adapter.getItem(position);
            String url = bean.getUrl();
            String title = bean.getTitle();
            openVodDesc(title, url);
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            VodDataBean bean = (VodDataBean) adapter.getItem(position);
            String title = bean.getTitle();
            String previewUrl = bean.getPreviewUrl();
            if (Utils.isNullOrEmpty(previewUrl)) return false;
            VideoPreviewDialog dialog = new VideoPreviewDialog(this, title, bean.getUrl(), previewUrl, bean.getImg());
            dialog.show();
            return true;
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            mRecyclerView.postDelayed(() -> {
                if (page >= pageCount) {
                    //数据全部加载完毕
                    adapter.getLoadMoreModule().loadMoreEnd();
                    isSearch = false;
                    application.showToastMsg(getString(R.string.noMoreContent), DialogXTipEnum.SUCCESS);
                } else {
                    if (isErr) {
                        //成功获取更多数据
                        page++;
                        mPresenter.loadPageData(searchContent, String.valueOf(page));
                        application.showToastMsg(String.format(LOAD_PAGE_AND_ALL_PAGE, (parserInterface.startPageNum() == 0 ? page+1 : page), pageCount), DialogXTipEnum.DEFAULT);
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

    private void initSearchBar() {
        List<String> historyList = SearchHistoryManager.getHistory(this);
        SearchHistoryAdapter adapter = new SearchHistoryAdapter(historyList);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        divider.setLastItemDecorated(false);
        rvHistory.addItemDecoration(divider);
        rvHistory.setNestedScrollingEnabled(false);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
        if (Utils.checkHasNavigationBar(this)) rvHistory.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        rvHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // 隐藏键盘
                    Utils.hideKeyboard(SearchActivity.this);
                }
            }
        });
        // 点击 item 填充搜索框
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            searchContent = adapter.getItem(position);
            if (searchContent != null) {
                searchView.setText(searchContent); // 填充到搜索框
                searchData();
            }
        });

        // 删除历史
        adapter.addChildClickViewIds(R.id.iv_delete);
        adapter.setOnItemChildClickListener((adapter12, view, position) -> {
            Utils.setVibration(view);
            if (view.getId() == R.id.iv_delete) {
                String keyword = adapter.getItem(position);
                historyList.remove(keyword);
                adapter.notifyDataSetChanged();
                // 更新SP
                getSharedPreferences("search_history", MODE_PRIVATE)
                        .edit()
                        .putString("history", TextUtils.join(",", historyList))
                        .apply();
            }
        });

        searchBar.setHint(parserInterface.searchHint());
        searchView.setHint(parserInterface.searchHint());
        // 将 SearchView 附加到 SearchBar
        searchView.setupWithSearchBar(searchBar);
        // 监听输入框提交
        searchView.addTransitionListener((searchView1, previousState, newState) -> {
            if (newState == SearchView.TransitionState.HIDDEN) {
                // SearchView关闭时刷新历史
                adapter.setList(SearchHistoryManager.getHistory(this));
            }
        });

        // Activity 创建时直接展开 SearchView
        searchView.show();
        // 注册返回键拦截
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView.isShowing()) {
                    searchView.hide();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // 如果还想自动弹出软键盘，可以再加：
        searchView.post(() -> {
            searchView.getEditText().requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchView.getEditText(), InputMethodManager.SHOW_IMPLICIT);
            }
        });
        // 监听输入变化
        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 监听软键盘搜索按钮
        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = v.getText().toString();
                if (isSearch) {
                    application.showToastMsg(getString(R.string.searchPleaseTryAgainLater), DialogXTipEnum.WARNING);
                    return false;
                }else {
                    searchContent = query.trim();
                    if (!searchContent.isEmpty()) {
                        searchData();
                        SearchHistoryManager.saveHistory(this, searchContent);
                    }
                }
                return true;
            }
            return false;
        });
    }

    private void searchData() {
        page = parserInterface.startPageNum();
        pageCount = parserInterface.startPageNum();
        multiItemEntities.clear();
        mPresenter.loadMainData(true, searchContent, String.valueOf(page));
        // 收起 SearchView
        searchView.hide();
        searchBar.setText(searchContent);
    }

    private void setSubTitle() {
        toolbar.setSubtitle(String.format(PAGE_AND_ALL_PAGE, parserInterface.startPageNum() == 0 ? page+1 : page, pageCount));
    }

    public void openVodDesc(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        startActivity(new Intent(SearchActivity.this, DetailsActivity.class).putExtras(bundle));
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
        multiItemEntities.clear();
        mPresenter.loadMainData(true, searchContent, String.valueOf(page));
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
                    lazyLoadImg();
                }, 500);
            } else {
                adapter.addData(vodDataBeans);
                setLoadState(adapter, true);
                lazyLoadImg();
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