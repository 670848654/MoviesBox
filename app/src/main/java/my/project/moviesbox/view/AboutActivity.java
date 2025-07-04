package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_OFF_HIDDEN_FEATURES;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_ON_HIDDEN_FEATURES;

import android.content.Intent;
import android.os.Environment;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.SettingAboutAdapter;
import my.project.moviesbox.bean.SettingAboutBean;
import my.project.moviesbox.enums.AboutEnum;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.SAFUtils;
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
        setToolbar(toolbar, getString(R.string.aboutTitle), "");
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

    private void initSwipe() {
        mSwipe.setEnabled(false);
    }

    private void initData() {
        boolean turnOnHiddenFeatures = SharedPreferencesUtils.getTurnOnHiddenFeatures();
        for (SettingAboutBean bean : list) {
            if (bean.getTitle().equals(getString(R.string.dataSourcesTitle))) {
                bean.setSubTitle(SharedPreferencesUtils.getUserSetDomain(SharedPreferencesUtils.getDefaultSource()));
            } else if (bean.getTitle().equals(getString(R.string.cacheDirectoryTitle))) {
                String savePath = "<font color=\"#FF5722\">" + (SAFUtils.canReadDownloadDirectory() ? Environment.DIRECTORY_DOWNLOADS+File.separator + "MoviesBox[" + SharedPreferencesUtils.getDataName() + "]" : "私有目录") + "</font>";
                String subTitle = String.format(getString(R.string.cacheDirectorySubContent), savePath);
                bean.setSubTitle(subTitle);
            } else if (bean.getTitle().equals(getString(R.string.authorizationDirectoryTitle))) {
                String authorizationDirectory = SAFUtils.checkHasSetDataSaveUri() ? "<font color=\"#31BDEC\">"+SAFUtils.getUriDirectoryName()+"</font>" : "无授权";
                bean.setSubTitle(authorizationDirectory);
            } else if (bean.getTitle().equals(getString(R.string.testModelTitle)) && turnOnHiddenFeatures) {
                bean.setEndIcon(R.drawable.round_restart_alt_24);
            }
        }
    }

    private static final long DOUBLE_CLICK_TIME_DELTA = 600; // 双击时间间隔
    private long lastClickTime = 0; // 上一次点击的时间
    private int clickCount = 0; // 连续点击次数
    private final static int MAX_CLICK_COUNT = 9; // 最大点击次数

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        settingAboutAdapter = new SettingAboutAdapter(this, list);
//        setAdapterAnimation(settingAboutAdapter);
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
                        application.showToastMsg("再点击"+(MAX_CLICK_COUNT-clickCount)+"次开启隐藏功能！", DialogXTipEnum.DEFAULT);
                    }
                    if (clickCount == 9) {
                        SharedPreferencesUtils.setTurnOnHiddenFeatures(true);
                        EventBus.getDefault().post(REFRESH_ON_HIDDEN_FEATURES);
                        list = AboutEnum.getTurnOnHiddenFeaturesList();
                        initData();
                        settingAboutAdapter.setNewInstance(list);
                        application.showToastMsg("你已开启隐藏功能！", DialogXTipEnum.SUCCESS);
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
        settingAboutAdapter.addChildClickViewIds(R.id.endIcon);
        settingAboutAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            SettingAboutBean aboutBean = list.get(position);
            String title = aboutBean.getTitle();
            switch (view.getId()) {
                case R.id.endIcon:
                    if (title.equals(getString(R.string.testModelTitle))) {
                        // 关闭隐藏功能
                        SharedPreferencesUtils.setTurnOnHiddenFeatures(false);
                        EventBus.getDefault().post(REFRESH_OFF_HIDDEN_FEATURES);
                        application.showToastMsg("你已关闭隐藏功能！", DialogXTipEnum.SUCCESS);
                        list = AboutEnum.getSettingAboutBeanList();
                        initData();
                        settingAboutAdapter.setNewInstance(list);
                    }
                    break;
            }
        });
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this) + 15);
        recyclerView.setAdapter(settingAboutAdapter);
    }
}
