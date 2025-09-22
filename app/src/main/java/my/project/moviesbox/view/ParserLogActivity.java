package my.project.moviesbox.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;

import my.project.moviesbox.adapter.ParserLogAdapter;
import my.project.moviesbox.bean.ParserLogBean;
import my.project.moviesbox.databinding.ActivitySingleListBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: OpenSourceActivity
  * @描述: 解析日志界面
  * @作者: Li Z
  * @日期: 2025/4/28 15:03
  * @版本: 1.0
 */
public class ParserLogActivity extends BaseActivity<ActivitySingleListBinding> {
    private AppBarLayout appBar;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipe;
    private RecyclerView recyclerView;

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
        setToolbar(toolbar, "解析日志", "最新的50条解析记录");
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
        ParserLogAdapter parserLogAdapter = new ParserLogAdapter(LogUtil.getLogs());
        parserLogAdapter.setOnItemClickListener((adapter, view, position) -> {
            Utils.setVibration(view);
            ParserLogBean parserLogBean = (ParserLogBean) adapter.getData().get(position);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", parserLogBean.getDateTime() + parserLogBean.getContent());
            clipboard.setPrimaryClip(clip);
            application.showToastMsg("已复制到剪切板", DialogXTipEnum.SUCCESS);
        });
        setAdapterAnimation(parserLogAdapter);
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this) + 15);
        recyclerView.setAdapter(parserLogAdapter);
    }
}
