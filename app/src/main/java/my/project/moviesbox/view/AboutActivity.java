package my.project.moviesbox.view;

import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
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
    private SettingAboutAdapter settingAboutAdapter;
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
                File file = new File(Utils.APP_DATA_PATH);
                bean.setSubTitle(file.canRead() ? String.format(getString(R.string.cacheDirectorySubContent), Utils.APP_DATA_PATH) : this.getFilesDir().getAbsolutePath());
            }
        }
    }

    private static final long DOUBLE_CLICK_TIME_DELTA = 600; // 双击时间间隔
    private long lastClickTime = 0; // 上一次点击的时间
    private int clickCount = 0; // 连续点击次数
    private final static int MAX_CLICK_COUNT = 9; // 最大点击次数
    private static Toast toast;

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        settingAboutAdapter = new SettingAboutAdapter(this, list);
        setAdapterAnimation(settingAboutAdapter, ADAPTER_ALPHA_IN_ANIMATION, true);
        settingAboutAdapter.setOnItemClickListener((adapter, view, position) -> {
            String title = list.get(position).getTitle();
            if (title.equals(getString(R.string.dataSourcesTitle)))
                Utils.viewInChrome(this, parserInterface.getDefaultDomain());
            else if (title.equals(getString(R.string.testModelTitle)) && !SharedPreferencesUtils.getTurnOnHiddenFeatures()) {
                long clickTime = System.currentTimeMillis();
                // 计算点击时间间隔
                long deltaTime = clickTime - lastClickTime;
                if (deltaTime < DOUBLE_CLICK_TIME_DELTA) {
                    // 连续点击
                    clickCount++;
                    if (clickCount > 3 && clickCount <MAX_CLICK_COUNT) {
                        cancelToast();
                        toast = Toast.makeText(this, "再点击"+(MAX_CLICK_COUNT-clickCount)+"次开启隐藏功能！", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    if (clickCount == 9) {
                        SharedPreferencesUtils.setTurnOnHiddenFeatures();
                        list.addAll(5, AboutEnum.getTurnOnHiddenFeaturesList());
                        settingAboutAdapter.notifyDataSetChanged();
                        application.showSnackbarMsgAction(toolbar, "你已开启隐藏功能！", "好", v -> {

                        });
                    }
                } else {
                    // 重新计数
                    clickCount = 1;
                }
                // 更新上一次点击时间
                lastClickTime = clickTime;
            }
             else if (title.equals(getString(R.string.vipVideoParserTitle)))
                startActivity(new Intent(this, VipParsingInterfacesActivity.class));
             else if (title.equals(getString(R.string.githubTitle)))
                Utils.viewInChrome(this, getString(R.string.githubUrl));
            else if (title.equals(getString(R.string.openSourceTitle)))
                startActivity(new Intent(this, OpenSourceActivity.class));
        });
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this) + 15);
        recyclerView.setAdapter(settingAboutAdapter);
    }

    private void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
