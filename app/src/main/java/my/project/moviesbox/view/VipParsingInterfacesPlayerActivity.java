package my.project.moviesbox.view;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.Jzvd;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VipVideoAdapter;
import my.project.moviesbox.config.JZExoPlayer;
import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.custom.AutoLineFeedLayoutManager;
import my.project.moviesbox.custom.JZPlayer;
import my.project.moviesbox.parser.bean.VipVideoDataBean;
import my.project.moviesbox.presenter.ParsingInterfacesPresenter;
import my.project.moviesbox.service.DLNAService;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.StatusBarUtil;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: ParsingInterfacesPlayerActivity
  * @描述: VIP影视解析播放视图<p>自用</p>
  * @作者: Li Z
  * @日期: 2024/2/23 15:38
  * @版本: 1.0
 */
public class VipParsingInterfacesPlayerActivity extends BaseActivity<ParsingInterfacesContract.View, ParsingInterfacesPresenter> implements
        JZPlayer.CompleteListener, JZPlayer.TouchListener,
        JZPlayer.ShowOrHideChangeViewListener,  JZPlayer.OnProgressListener, JZPlayer.PlayingListener, JZPlayer.PauseListener, JZPlayer.OnQueryDanmuListener, ParsingInterfacesContract.View {
    @BindView(R.id.player)
    JZPlayer player;
    protected String videoTitle, dramaTitle, url;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.other_view)
    LinearLayout otherView;
    @BindView(R.id.hide_progress)
    MaterialSwitch hideProgressSc;
    @BindView(R.id.auto_play_next_video)
    MaterialSwitch autoplayNextVideoSwitch;
    protected boolean playNextVideo;
    protected int clickIndex; // 当前点击剧集
    protected boolean hasPreVideo = false;
    protected boolean hasNextVideo = false;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView; // 剧集列表
    protected VipVideoAdapter adapter;
    @BindView(R.id.speed)
    TextView speedTextView;
    protected String[] speedsStrItems = Utils.getArray(R.array.fast_forward_item);
    protected int[] speedsIntItems = Utils.getIntArray(R.array.fast_forward_set_item);
    protected int userSpeed = 2;

    private AlertDialog alertDialog;
    private List<VipVideoDataBean.DramasItem> dramasItems = new ArrayList<>();

    @Override
    protected ParsingInterfacesPresenter createPresenter() {
        return new ParsingInterfacesPresenter(url, this);
    }

    @Override
    protected void loadData() {
        if (url.endsWith("html"))
            mPresenter.parser();
        else
            play(url);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_player;
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        videoTitle = bundle.getString("videoTitle");
        dramaTitle = bundle.getString("dramaTitle");
        dramasItems = (List<VipVideoDataBean.DramasItem>) bundle.getSerializable("list");
        initPlayerView();
        initNavConfigView();
        initUserConfig();
        setPreNextData();
        setAdapter();
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    @Override
    protected void setConfigurationChanged() {

    }

    /**
     * 点击重试抽象方法
     *
     * @return
     */
    @Override
    protected void retryListener() {

    }

    protected void setActivityName() {
        application.addDestoryActivity(this, "player");
    }


    private void initPlayerView() {
        otherView.setVisibility(View.GONE);
        player.configView.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });
        player.setListener(this, this, this, this, this, this, this, this, this);
        player.WIFI_TIP_DIALOG_SHOWED = true;
        player.backButton.setOnClickListener(v -> finish());
        player.preVideo.setOnClickListener(v -> {
            clickIndex--;
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            changePlayUrl(clickIndex);
        });
        player.nextVideo.setOnClickListener(v -> {
            clickIndex++;
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            changePlayUrl(clickIndex);
        });
        if (gtSdk23()) player.tvSpeedView.setVisibility(View.VISIBLE);
        else player.tvSpeedView.setVisibility(View.GONE);
        player.selectDramaView.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });
        player.pipView.setOnClickListener(view -> {
            if (gtSdk26()) startPic();
        });
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        player.playingShow();
    }

    /**
     * 播放视频
     *
     * @param playUrl
     */
    protected void play(String playUrl) {
        Jzvd.releaseAllVideos();
        player.setUp(playUrl, videoTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        player.startVideo();
        player.startButton.performClick();//响应点击事件
    }

    private void initNavConfigView() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
//                if (drawerLayout.isDrawerOpen(GravityCompat.START)) player.goOnPlayOnPause();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
//                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) player.goOnPlayOnResume();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    private void initUserConfig() {
        switch (SharedPreferencesUtils.getUserSetSpeed()) {
            case 5:
                setUserSpeedConfig(0);
                break;
            case 10:
                setUserSpeedConfig(1);
                break;
            case 15:
                setUserSpeedConfig(2);
                break;
            case 30:
                setUserSpeedConfig(3);
                break;
        }
        hideProgressSc.setChecked(SharedPreferencesUtils.getUserSetHideProgress());
        hideProgressSc.setOnCheckedChangeListener((buttonView, isChecked) -> SharedPreferencesUtils.setUserSetHideProgress(isChecked));
        playNextVideo = SharedPreferencesUtils.getUserAutoPlayNextVideo();
        autoplayNextVideoSwitch.setChecked(playNextVideo);
        autoplayNextVideoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setUserAutoPlayNextVideo(isChecked);
            playNextVideo = isChecked;
        });
    }

    private void setPreNextData() {
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, dramasItems.get(clickIndex-1).getTitle()) : "");
        hasNextVideo = clickIndex != dramasItems.size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, dramasItems.get(clickIndex+1).getTitle()) : "");
    }

    protected void initPlayerPreNextTag() {
        hasPreVideo = clickIndex != 0;
        setPreNextData();
    }

    private void setAdapter() {
        adapter = new VipVideoAdapter(this, dramasItems);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(position);
        });
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new AutoLineFeedLayoutManager());
    }

    protected void changePlayUrl(int position) {
        player.releaseDanMu();
        player.danmuInfoView.setVisibility(View.GONE);
        clickIndex = position;
        initPlayerPreNextTag();
        VipVideoDataBean.DramasItem bean = dramasItems.get(position);
        Jzvd.releaseAllVideos();
        adapter.getData().get(position).setSelected(true);
        adapter.notifyDataSetChanged();
        dramaTitle = bean.getTitle();
        url = bean.getUrl();
        player.playingShow();
        loadData();
    }

    private void setUserSpeedConfig(int userSet) {
        SharedPreferencesUtils.setUserSetSpeed(speedsIntItems[userSet]);
        speedTextView.setText(speedsStrItems[userSet]);
        userSpeed = userSet;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else finish();
    }

    @OnClick({R.id.speed_config})
    public void configBtnClick(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.speed_config:
                setDefaultSpeed();
                break;
        }
    }

    private void setDefaultSpeed() {
        Utils.showSingleChoiceAlert(this,
                getString(R.string.setUserSpeed),
                speedsStrItems,
                true,
                userSpeed,
                (dialogInterface, i) -> {
                    setUserSpeedConfig(i);
                    dialogInterface.dismiss();
                }
        );
    }

    /**
     * 是否为分屏模式
     *
     * @return
     */
    public boolean inMultiWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) return this.isInMultiWindowMode();
        else return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            player.startPIP();
            player.hideDanmmu();
        } else {
            if (getWindow().getDecorView().getVisibility() == View.VISIBLE)
                player.showDanmmu();
            else
                finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        if (isInMultiWindowMode)
            player.goOnPlayOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        hideNavBar();
        if (!inMultiWindow()) player.goOnPlayOnResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startPic() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(this::enterPicInPic, 500);
    }

    /**
     * Android 8.0 画中画
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enterPicInPic() {
        PictureInPictureParams builder = new PictureInPictureParams.Builder().build();
        enterPictureInPictureMode(builder);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, DLNAService.class));
        player.releaseDanMu();
        player.releaseAllVideos();
        super.onDestroy();
    }

    @Override
    public void touch() {
        hideNavBar();
    }

    public void cancelDialog() {
        Utils.cancelDialog(alertDialog);
    }

    /**
     * @return
     * @方法名称: loadingView
     * @方法描述: 用于显示加载中视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void loadingView() {
        alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
    }

    /**
     * @param msg 错误文本信息
     * @return
     * @方法名称: errorView
     * @方法描述: 用于显示加载失败视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void errorView(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {cancelDialog();
            player.onStateError();
            hideNavBar();
            Utils.showAlert(this,
                    getString(R.string.errorDialogTitle),
                    getString(R.string.parseVodPlayUrlError),
                    false,
                    getString(R.string.defaultPositiveBtnText),
                    "",
                    "",
                    (dialog, which) -> dialog.dismiss(),
                    null,
                    null);
        });
    }

    /**
     * @return
     * @方法名称: emptyView
     * @方法描述: 用于显示空数据视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void emptyView() {

    }

    @Override
    public void success(Object object) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            hideNavBar();
            cancelDialog();
            try {
                JSONObject jsonObject = (JSONObject) object;
                if (jsonObject.getInteger("code") == 200) {
                    String aes_key = jsonObject.getString("aes_key");
                    String aes_iv = jsonObject.getString("aes_iv");
                    String videoUrl = VipParsingInterfacesActivity.getData(aes_iv, aes_key, jsonObject.getString("url"));
                    play(videoUrl);
                } else
                    Utils.showAlert(this,
                            getString(R.string.errorDialogTitle),
                            jsonObject.getString("msg"),
                            false,
                            getString(R.string.defaultPositiveBtnText),
                            "",
                            "",
                            (dialog, which) -> dialog.dismiss(),
                            null,
                            null);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showAlert(this,
                        getString(R.string.errorDialogTitle),
                        e.getMessage(),
                        false,
                        getString(R.string.defaultPositiveBtnText),
                        "",
                        "",
                        (dialog, which) -> dialog.dismiss(),
                        null,
                        null);
            }
        });
    }

    @Override
    public void playing() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void complete() {
        if (hasNextVideo && playNextVideo) {
            application.showToastMsg("开始播放下一集");
            clickIndex++;
            changePlayUrl(clickIndex);
        } else {
            player.releaseDanMu();
            application.showToastMsg("全部播放完毕");
            if (!drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void showOrHideChangeView() {

    }

    @Override
    public void getPosition(long position, long duration) {

    }

    @Override
    public void queryDamu(String queryDanmuTitle, String queryDanmuDrama) {

    }
}
