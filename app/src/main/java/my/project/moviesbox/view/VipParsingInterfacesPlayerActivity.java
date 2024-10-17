package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_PLAYER_KERNEL;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.Jzvd;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VipVideoAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.JZExoPlayer;
import my.project.moviesbox.config.JZMediaIjk;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.custom.AutoLineFeedLayoutManager;
import my.project.moviesbox.custom.DanmakuJsonParser;
import my.project.moviesbox.custom.DanmukuXmlParser;
import my.project.moviesbox.custom.JZPlayer;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.ParsingInterfacesModel;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DanmuDataBean;
import my.project.moviesbox.parser.bean.VipVideoDataBean;
import my.project.moviesbox.presenter.DanmuPresenter;
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
public class VipParsingInterfacesPlayerActivity extends BaseActivity<ParsingInterfacesModel, ParsingInterfacesContract.View, ParsingInterfacesPresenter> implements
        JZPlayer.CompleteListener, JZPlayer.TouchListener,
        JZPlayer.ShowOrHideChangeViewListener, JZPlayer.OnProgressListener, JZPlayer.PlayingListener,
        JZPlayer.PauseListener, JZPlayer.OnQueryDanmuListener, JZPlayer.ActivityOrientationListener, ParsingInterfacesContract.View,
        DanmuContract.View {
    @BindView(R.id.episodes_view)
    LinearLayout episodesView;
    @BindView(R.id.config_view)
    LinearLayout configView;
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

    private List<VipVideoDataBean.DramasItem> dramasItems = new ArrayList<>();

    protected DanmuPresenter danmuPresenter = new DanmuPresenter(this);

    @Override
    protected ParsingInterfacesPresenter createPresenter() {
        return new ParsingInterfacesPresenter(this);
    }

    @Override
    protected void loadData() {
        if (url.contains("?url")) {
            // 需要二次解析
            Matcher matcher = VipParsingInterfacesActivity.URL_PATTERN.matcher(url);
            if (matcher.find()) {
                url = matcher.group();
                mPresenter.parser(url);
            } else
                mPresenter.parser(url);
        }
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

    private void initPlayerView() {
        App.addDestroyActivity(this, "player");
        if (dramasItems.size() == 0)
            player.selectDramaView.setVisibility(View.GONE);
        otherView.setVisibility(View.GONE);
        player.configView.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });
        player.setListener(this, this, this, this, this, this, this, this, this);
        player.backButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            finish();
        });
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
        player.changePlayerKernel.setOnClickListener(v -> {
            // 切换播放器内核
            int userSetPlayerKernel = SharedPreferencesUtils.getUserSetPlayerKernel();
            switch (userSetPlayerKernel) {
                case 0:
                    // EXO 切换至 IJK
                    SharedPreferencesUtils.setUserSetPlayerKernel(1);
                    player.setMediaInterface(JZMediaIjk.class);
                    application.showToastMsg("已切换至IjkPlayer内核", DialogXTipEnum.SUCCESS);
                    break;
                case 1:
                    // IJK 切换至 EXO
                    SharedPreferencesUtils.setUserSetPlayerKernel(0);
                    player.setMediaInterface(JZExoPlayer.class);
                    application.showToastMsg("已切换至ExoPlayer内核", DialogXTipEnum.SUCCESS);
                    break;
            }
            EventBus.getDefault().post(REFRESH_PLAYER_KERNEL);
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
        // 默认显示弹幕属性
        player.openDamuConfig = true;
        player.hasDanmuConfig = true;
        player.danmuView.setVisibility(View.VISIBLE);
        player.danmuInfoView.setVisibility(View.VISIBLE);
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


    /**
     * 获取弹幕
     */
    private void getDanmu(String danmuUrl) {
        danmuPresenter.loadVipDanmu(danmuUrl);
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
        if (dramasItems.size() == 0) return;
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
    protected void onStart() {
        super.onStart();
        if (danmuPresenter != null)
            danmuPresenter.registerEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (danmuPresenter != null)
            danmuPresenter.unregisterEventBus();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, DLNAService.class));
        player.releaseDanMu();
        Jzvd.releaseAllVideos();
        player.danmakuView = null;
        if (danmuPresenter != null)
            danmuPresenter.detachView();
        App.removeDestroyActivity("player");
        emptyRecyclerView(recyclerView);
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
                    // 弹幕URL
                    String danmuUrl = jsonObject.getString("ggdmapi");
                    getDanmu(danmuUrl);
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
            application.showToastMsg("开始播放下一集", DialogXTipEnum.DEFAULT);
            clickIndex++;
            changePlayUrl(clickIndex);
        } else {
            player.releaseDanMu();
            application.showToastMsg("全部播放完毕", DialogXTipEnum.SUCCESS);
            if (dramasItems.size() == 0) return;
            if (!drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void showOrHideChangeView() {
        player.preVideo.setVisibility(hasPreVideo ? View.VISIBLE : View.GONE);
        player.nextVideo.setVisibility(hasNextVideo ? View.VISIBLE : View.GONE);
    }

    @Override
    public void getPosition(long position, long duration) {

    }

    @Override
    public void queryDamu(String queryDanmuTitle, String queryDanmuDrama) {

    }

    @Override
    public void setOrientation(int type) {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            this.setRequestedOrientation(type);
            ViewGroup.LayoutParams episodesViewLayoutParams = episodesView.getLayoutParams();
            ViewGroup.LayoutParams configViewLayoutParams = configView.getLayoutParams();
            int widthInDp = 0;
            switch (type) {
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    widthInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Utils.isPad() ? 150 * 2: 200, getResources().getDisplayMetrics());
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
                    widthInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  Utils.isPad() ? 250 * 2 : 400, getResources().getDisplayMetrics());
                    break;
            }
            episodesViewLayoutParams.width = widthInDp;
            episodesView.setLayoutParams(episodesViewLayoutParams);
            configViewLayoutParams.width = widthInDp;
            configView.setLayoutParams(configViewLayoutParams);
        }, 500);
    }

    private BaseDanmakuParser createParser(boolean json, InputStream stream) {
        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }
        ILoader loader = DanmakuLoaderFactory.create(json ? DanmakuLoaderFactory.TAG_ACFUN : DanmakuLoaderFactory.TAG_BILI);
        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = json ? new DanmakuJsonParser() : new DanmukuXmlParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    @Override
    public void successDanmuJson(List<DanmuDataBean> danmuDataBeanList) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            // JSON格式弹幕信息解析示例
            if (player.loadError) return;
            try {
                player.danmuInfoView.setText("已加载"+ danmuDataBeanList.size() + "条弹幕");
                player.danmuInfoView.setVisibility(View.VISIBLE);
                String json = JSON.toJSONString(danmuDataBeanList);
                LogUtil.logInfo("弹幕信息", json);
                InputStream result = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
                player.danmakuParser = createParser(true, result);
                player.createDanmu();
                if (player.danmakuView.isPrepared()) {
                    player.danmakuView.restart();
                }
                player.danmakuView.prepare(player.danmakuParser, player.danmakuContext);
                if (player.seekToInAdvance > 0) {
                    new Handler().postDelayed(() -> {
                        // 一秒后定位弹幕时间为用户上次观看位置
                        player.seekDanmu(player.seekToInAdvance);
                    }, 1000);
                }
            } catch (Exception e) {
                application.showToastMsg(e.getMessage(), DialogXTipEnum.ERROR);
            }
        });
    }

    @Override
    public void successDanmuXml(String content) {

    }

    @Override
    public void errorDanmu(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> application.showToastMsg(msg, DialogXTipEnum.ERROR));
    }
}
