package my.project.moviesbox.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.ParserLogAdapter;
import my.project.moviesbox.bean.ParserLogBean;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: OpenSourceActivity
  * @描述: 解析日志界面
  * @作者: Li Z
  * @日期: 2025/4/28 15:03
  * @版本: 1.0
 */
public class ParserLogActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;

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
        setToolbar(toolbar, "解析日志", "最新的50条解析记录");
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
