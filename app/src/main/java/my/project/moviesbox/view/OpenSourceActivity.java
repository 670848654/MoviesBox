package my.project.moviesbox.view;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.SourceAdapter;
import my.project.moviesbox.bean.SourceBean;
import my.project.moviesbox.enums.OpenSourceEnum;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: OpenSourceActivity
  * @描述: 开源相关界面
  * @作者: Li Z
  * @日期: 2024/1/24 13:39
  * @版本: 1.0
 */
public class OpenSourceActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private SourceAdapter adapter;
    private List<SourceBean> list = OpenSourceEnum.getSourceList();

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_single_list;
    }

    @Override
    protected void init() {
        setToolbar(toolbar, getString(R.string.openSourceTitle), "");
        initSwipe();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {}

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected void retryListener() {

    }

    public void initSwipe() {
        mSwipe.setEnabled(false);
    }

    public void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SourceAdapter(list);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
            Utils.viewInChrome(this, list.get(position).getUrl());
        });
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this) + 15);
        recyclerView.setAdapter(adapter);
    }
}
