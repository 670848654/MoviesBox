package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.TextListAdapter;
import my.project.moviesbox.contract.TextListContract;
import my.project.moviesbox.model.TextListModel;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.presenter.TextListPresenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: TextListActivity
  * @描述: 排行榜列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:12
  * @版本: 1.0
 */
public class TextListActivity extends BaseActivity<TextListModel, TextListContract.View, TextListPresenter> implements TextListContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.selected_text)
    AutoCompleteTextView selectedView;
    @BindView(R.id.selected_layout)
    TextInputLayout selectedLayout;
    private List<String> topTitles;
    private ArrayAdapter selectedAdapter;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private List<TextDataBean> textDataBeans = new ArrayList<>();
    private List<TextDataBean.Item> items = new ArrayList<>();
    private TextListAdapter adapter;
    private String title, url;
    private int selectedIndex = 0;

    @Override
    protected TextListPresenter createPresenter() {
        return new TextListPresenter(this);
    }

    @Override
    protected void loadData() {
       mPresenter.loadData(url);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_text_list;
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
    protected void initBeforeView() {

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
}
