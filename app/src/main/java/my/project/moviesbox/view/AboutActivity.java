package my.project.moviesbox.view;

import android.content.Intent;
import android.view.HapticFeedbackConstants;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.animation.SlideInBottomAnimation;

import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.SettingAboutAdapter;
import my.project.moviesbox.bean.SettingAboutBean;
import my.project.moviesbox.config.AboutEnum;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: AboutActivity
  * @描述: 关于页面
  * @作者: Li Z
  * @日期: 2024/1/24 13:38
  * @版本: 1.0
 */
public class AboutActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private SettingAboutAdapter adapter;
    private List<SettingAboutBean> list = AboutEnum.getSettingAboutBeanList();

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
        initToolbar();
        initSwipe();
        initData();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {}

    @Override
    protected void setConfigurationChanged() {}

    @Override
    protected void retryListener() {

    }

    private void initToolbar() {
        toolbar.setTitle(getString(R.string.aboutTitle));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            finish();
        });
    }

    private void initSwipe() {
        mSwipe.setEnabled(false);
    }

    private void initData() {
        for (SettingAboutBean bean : list) {
            if (bean.getTitle().equals(getString(R.string.dataSourcesTitle))) {
                bean.setSubTitle(SharedPreferencesUtils.getUserSetDomain(SharedPreferencesUtils.getDefaultSource()));
            } else if (bean.getTitle().equals(getString(R.string.cacheDirectoryTitle))) {
                bean.setSubTitle(String.format(getString(R.string.cacheDirectorySubContent), Utils.APP_DATA_PATH));
            }
        }
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SettingAboutAdapter(this, list);
        adapter.setAnimationEnable(true);
        adapter.setAdapterAnimation(new SlideInBottomAnimation());
        adapter.setOnItemClickListener((adapter, view, position) -> {
            String title = list.get(position).getTitle();
            if (title.equals(getString(R.string.dataSourcesTitle)))
                Utils.viewInChrome(this, parserInterface.getDefaultDomain());
             else if (title.equals(getString(R.string.vipVideoParserTitle)))
                startActivity(new Intent(this, VipParsingInterfacesActivity.class));
             else if (title.equals(getString(R.string.githubTitle)))
                Utils.viewInChrome(this, getString(R.string.githubUrl));
            else if (title.equals(getString(R.string.openSourceTitle)))
                startActivity(new Intent(this, OpenSourceActivity.class));
        });
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this) + 15);
        recyclerView.setAdapter(adapter);
    }
}
