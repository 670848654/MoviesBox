package my.project.moviesbox.view;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.HomeViewPageAdapter;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.service.DownloadService;
import my.project.moviesbox.service.RssService;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: HomeActivity
  * @描述: 首页数据视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:11
  * @版本: 1.0
 */
public class HomeActivity extends BaseActivity {
    @BindView(R.id.nav_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.viewpage2)
    ViewPager2 viewPager2;
    private HomeViewPageAdapter homeViewPageAdapter;
    private List<Fragment> list = new ArrayList<>();

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        startRssService();
        list.add(new HomeFragment());
        list.add(new MyFragment());
        list.add(new SettingFragment());
        homeViewPageAdapter = new HomeViewPageAdapter(this, list);
        viewPager2.setAdapter(homeViewPageAdapter);
        viewPager2.setUserInputEnabled(false);
//        viewPager2.setPageTransformer(new ParallaxTransformer());
        //设置bottomNavigationView item 点击事件
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            switch (itemId){
                case R.id.home:
                    viewPager2.setCurrentItem(0);
                    break;
                case R.id.my:
                    viewPager2.setCurrentItem(1);
                    break;
                case R.id.setting:
                    viewPager2.setCurrentItem(2);
                    break;
            }
            return true;
        });
        //实现滑动的时候 联动 bottomNavigationView的selectedItem
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.my);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.setting);
                        break;
                }
            }
        });

        if (!SharedPreferencesUtils.appMainInfo()) {
            Utils.showAlert(
                    this,
                    getString(R.string.statementTitle),
                    getString(R.string.firstOpenAppDialogContent),
                    false,
                    getString(R.string.defaultPositiveBtnText),
                    "",
                    "",
                    (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        SharedPreferencesUtils.setAppMainInfo();
                    },
                    null,
                    null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            createShortcut();
    }

    /**
     * 开启支持RSS订阅站点获取当日更新信息的服务
     */
    public void startRssService() {
        String rssUrl = parserInterface.getRssUrl();
        if (!rssUrl.isEmpty()) {
            Intent intent = new Intent(this, RssService.class);
            intent.putExtra("url", rssUrl);
            startService(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEnum refresh) {
        switch (refresh) {
            case CHANGE_SOURCES:
//                runOnUiThread(() -> application.showToastMsg("应用即将重启"));
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }
    }

    @Override
    protected void initBeforeView() {

    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected void retryListener() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        parserInterface = null;
        EventBus.getDefault().unregister(this);
        stopService(new Intent(this, DownloadService.class));
    }

    private boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        application.showToastMsg(getString(R.string.pressAgain2ExitApp), DialogXTipEnum.DEFAULT);
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000); // 2秒内再次按下返回键生效
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void createShortcut() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager != null) {
            shortcutManager.removeDynamicShortcuts(Arrays.asList("open_favorite", "open_download"));
            List<ShortcutInfo> shortcuts = new ArrayList<>();
            shortcuts.add(createFragmentShortcut("open_91", "91吃瓜中心", "91吃瓜中心", R.drawable.ic_shortcut_insert_link, "app://open_91"));
            shortcuts.add(createFragmentShortcut("open_vip", "VIP视频解析助手", "VIP视频解析助手", R.drawable.ic_shortcut_insert_link, "app://open_vip"));
            shortcutManager.setDynamicShortcuts(shortcuts);
        }
    }

    /**
     *
     * @param openId
     * @param shortLabel
     * @param longLabel
     * @param icon
     * @param location
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private ShortcutInfo createFragmentShortcut(String openId, String shortLabel, String longLabel, @DrawableRes int icon, String location) {
        return new ShortcutInfo.Builder(this, openId)
                .setShortLabel(shortLabel)  // 快捷方式显示的名称
                .setLongLabel(longLabel)  // 长按时显示的描述
                .setIcon(Icon.createWithResource(this, icon))
                .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(location)))  // 设置启动时的 Intent
                .build();
    }
}
