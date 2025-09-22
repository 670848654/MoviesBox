package my.project.moviesbox.view;

import android.view.LayoutInflater;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.SourceAdapter;
import my.project.moviesbox.bean.SourceBean;
import my.project.moviesbox.databinding.ActivitySingleListBinding;
import my.project.moviesbox.enums.OpenSourceEnum;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: OpenSourceActivity
  * @描述: 开源相关界面
  * @作者: Li Z
  * @日期: 2024/1/24 13:39
  * @版本: 1.0
 */
public class OpenSourceActivity extends BaseActivity<ActivitySingleListBinding> {
    private AppBarLayout appBar;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipe;
    private RecyclerView recyclerView;
    private SourceAdapter adapter;
    private List<SourceBean> list = OpenSourceEnum.getSourceList();

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivitySingleListBinding inflateBinding(LayoutInflater inflater) {
        return ActivitySingleListBinding.inflate(inflater);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        mSwipe = binding.contentLayout.mSwipe;
        recyclerView = binding.contentLayout.rvList;
    }

    @Override
    public void initClickListeners() {}

    @Override
    protected void init() {
        setToolbar(toolbar, getString(R.string.openSourceTitle), "");
        initSwipe();
        initAdapter();
    }

    @Override
    protected void setConfigurationChanged() {}

    @Override
    protected void retryListener() {}

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
