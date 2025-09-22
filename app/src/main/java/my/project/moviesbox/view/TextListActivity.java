package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.TextListAdapter;
import my.project.moviesbox.contract.TextListContract;
import my.project.moviesbox.databinding.ActivityTextListBinding;
import my.project.moviesbox.model.TextListModel;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.presenter.TextListPresenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseMvpActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: TextListActivity
  * @描述: 排行榜列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:12
  * @版本: 1.0
 */
public class TextListActivity extends BaseMvpActivity<TextListModel, TextListContract.View, TextListPresenter, ActivityTextListBinding> implements TextListContract.View {
    private List<String> topTitles;
    private ArrayAdapter selectedAdapter;
    private List<TextDataBean> textDataBeans = new ArrayList<>();
    private List<TextDataBean.Item> items = new ArrayList<>();
    private TextListAdapter adapter;
    private String title, url;
    private int selectedIndex = 0;

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityTextListBinding inflateBinding(LayoutInflater inflater) {
        return ActivityTextListBinding.inflate(inflater);
    }

    private AppBarLayout appBar;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipe;
    private AutoCompleteTextView selectedView;
    private TextInputLayout selectedLayout;
    private RecyclerView mRecyclerView;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        mSwipe = binding.mSwipe;
        selectedView = binding.selectedText;
        selectedLayout = binding.selectedLayout;
        mRecyclerView = binding.rvList;
    }

    @Override
    protected TextListPresenter createPresenter() {
        return new TextListPresenter(this);
    }

    @Override
    protected void loadData() {
       mPresenter.loadData(url);
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        url = bundle.getString("url");
        setToolbar(toolbar, title, "");
        initSwipe();
        initAdapter();
    }

    @Override
    public void initClickListeners() {

    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> retryListener());
    }

    public void initAdapter() {
        adapter = new TextListAdapter(this, items);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
            TextDataBean.Item item = (TextDataBean.Item) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("name", item.getTitle());
            bundle.putString("url", item.getUrl());
            startActivity(new Intent(this, DetailsActivity.class).putExtras(bundle));
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
        adapter.setEmptyView(rvView);
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected void retryListener() {
        mSwipe.setRefreshing(false);
        if (selectedView.isShown())
            selectedLayout.setVisibility(View.GONE);
        selectedIndex = 0;
        loadData();
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
    }

    @Override
    public void success(List<TextDataBean> textDataBeans) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            hideProgress();
            mSwipe.setRefreshing(false);
            new Handler().postDelayed(() -> {
                this.textDataBeans = textDataBeans;
                if (textDataBeans.size() > 1) {
                    topTitles = new ArrayList<>();
                    for (TextDataBean textDataBean : textDataBeans) {
                        topTitles.add(textDataBean.getTitle());
                    }
                    selectedView.setText(topTitles.get(0));
                    selectedAdapter = new ArrayAdapter(this, R.layout.text_list_item, topTitles);
                    selectedView.setAdapter(selectedAdapter);
                    selectedView.setOnItemClickListener((parent, view, position, id) -> {
                        setAdapterData(position);
                    });
                    selectedLayout.setVisibility(View.VISIBLE);
                }
                adapter.setNewInstance(textDataBeans.get(selectedIndex).getItemList());
            }, 500);
        });
    }

    @Override
    public void empty(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            items.clear();
            mSwipe.setRefreshing(false);
            rvEmpty(msg);
        });
    }

    private void setAdapterData(int position) {
        selectedIndex = position;
        items = textDataBeans.get(position).getItemList();
        adapter.setNewInstance(items);
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
