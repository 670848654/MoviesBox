package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_PLAYER_KERNEL;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.jzvd.Jzvd;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.MenuAdapter;
import my.project.moviesbox.adapter.VipVideoAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.bean.MenuBean;
import my.project.moviesbox.config.JZExoPlayer;
import my.project.moviesbox.config.JZMediaIjk;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.custom.DanmakuJsonParser;
import my.project.moviesbox.custom.DanmukuXmlParser;
import my.project.moviesbox.custom.JZPlayer;
import my.project.moviesbox.custom.TextViewAnimator;
import my.project.moviesbox.databinding.ActivityPlayerBinding;
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
import my.project.moviesbox.view.base.BaseMvpActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: ParsingInterfacesPlayerActivity
  * @描述: VIP影视解析播放视图<p>自用</p>
  * @作者: Li Z
  * @日期: 2024/2/23 15:38
  * @版本: 1.0
 */
public class VipParsingInterfacesPlayerActivity extends BaseMvpActivity<ParsingInterfacesModel, ParsingInterfacesContract.View, ParsingInterfacesPresenter, ActivityPlayerBinding> implements
        JZPlayer.CompleteListener,
        JZPlayer.TouchListener,
        JZPlayer.ShowOrHideChangeViewListener,
        JZPlayer.OnProgressListener,
        JZPlayer.PlayingListener,
        JZPlayer.PauseListener,
        JZPlayer.ActivityOrientationListener,
        ParsingInterfacesContract.View,
        JZPlayer.FlipListener,
        DanmuContract.View,
        JZPlayer.SpeedListener,
        JZPlayer.DisplayListener {
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_FORWARD = "ACTION_FORWARD";
    public static final String ACTION_REWIND = "ACTION_REWIND";
    protected String videoTitle, dramaTitle, url;
    protected boolean playNextVideo;
    protected int clickIndex; // 当前点击剧集
    protected boolean hasPreVideo = false;
    protected boolean hasNextVideo = false;
    private String danmuUrl; // 弹幕接口
    private String dmid; // 弹幕ID
    private boolean loadDlDanmu; // 是否加载独立弹幕
    protected VipVideoAdapter adapter;
    protected String[] speedsStrItems = Utils.getArray(R.array.fast_forward_item);
    protected int[] speedsIntItems = Utils.getIntArray(R.array.fast_forward_set_item);
    protected int userSpeed = 2;
    private List<VipVideoDataBean.DramasItem> dramasItems = new ArrayList<>();
    protected DanmuPresenter danmuPresenter = new DanmuPresenter(this);
    private final List<MenuBean> speedMenuBeanList = new ArrayList<>();
    private final List<MenuBean> displayMenuBeanList = new ArrayList<>();

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityPlayerBinding inflateBinding(LayoutInflater inflater) {
        return ActivityPlayerBinding.inflate(inflater);
    }

    private LinearLayout episodesView;
    private LinearLayout configView;
    private JZPlayer player;
    private DrawerLayout drawerLayout;
    private LinearLayout otherView;
    private MaterialSwitch hideProgressSc;
    private MaterialSwitch autoplayNextVideoSwitch;
    private RecyclerView recyclerView; // 剧集列表
    private TextView speedTextView;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        episodesView = binding.episodesView;
        configView = binding.configView;
        player = binding.player;
        drawerLayout = binding.drawerLayout;
        otherView = binding.otherView;
        hideProgressSc = binding.hideProgress;
        autoplayNextVideoSwitch = binding.autoPlayNextVideo;
        recyclerView = binding.rvList;
        speedTextView = binding.speed;
    }

    @Override
    public void initClickListeners() {
        binding.speedConfig.setOnClickListener(v -> setDefaultSpeed());
    }

    @Override
    protected ParsingInterfacesPresenter createPresenter() {
        return new ParsingInterfacesPresenter(this);
    }

    @Override
    protected void loadData() {
        /*if (url.contains("?url")) {
            // 需要二次解析
            Matcher matcher = VipParsingInterfacesActivity.URL_PATTERN.matcher(url);
            if (matcher.find()) {
                url = matcher.group();
                mPresenter.parser(VipParsingInterfacesActivity.OLD_API, url);
            } else
                mPresenter.parser(VipParsingInterfacesActivity.OLD_API, url);
        }
        else*/
        play(url);
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        videoTitle = bundle.getString("videoTitle");
        dramaTitle = bundle.getString("dramaTitle");
        danmuUrl = bundle.getString("danmuUrl");
        dmid = bundle.getString("dmid");
        dramasItems = (List<VipVideoDataBean.DramasItem>) bundle.getSerializable("list");
        initPlayerView();
        initNavConfigView();
        initUserConfig();
        setPreNextData();
        setAdapter();
        initMenuList();
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
        player.changePlayerKernel.setVisibility(View.GONE);
        otherView.setVisibility(View.GONE);
        player.configView.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });
        player.setListener(this, this, this, this, this, this, this, this, this, this, this);
        player.backButton.setOnClickListener(v -> {
            Utils.setVibration(v);
            finish();
        });
        player.preVideo.setOnClickListener(v -> {
            clickIndex--;
            Utils.setVibration(v);
            changePlayUrl(clickIndex);
        });
        player.nextVideo.setOnClickListener(v -> {
            clickIndex++;
            Utils.setVibration(v);
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
        player.danmuConfigView.setVisibility(View.VISIBLE);
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
        player.isVideoPrepared = false;
        player.isDanmakuPrepared = false;
        player.setUp(playUrl, Utils.isNullOrEmpty(videoTitle) ? dramaTitle : videoTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accept", "*/*");
        headers.put("accept-encoding", "gzip, deflate, br, zstd");
        headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.put("origin", "https://jx.xmflv.com");
        headers.put("priority", "u=1, i");
        headers.put("sec-ch-ua", "\"Not;A=Brand\";v=\"99\", \"Microsoft Edge\";v=\"139\", \"Chromium\";v=\"139\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "cross-site");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0");
        player.jzDataSource.headerMap = headers;
        player.startVideo();
        player.startButton.performClick();//响应点击事件
        if (!Utils.isNullOrEmpty(danmuUrl))
            getDanmu();
    }


    /**
     * 获取弹幕
     */
    private void getDanmu() {
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
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW); // 横向排布
        layoutManager.setFlexWrap(FlexWrap.WRAP);         // 换行
        layoutManager.setJustifyContent(JustifyContent.FLEX_START); // 起始对齐
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initMenuList() {
        speedMenuBeanList.add(new MenuBean("倍速x0.5", 0.5f, false));
        speedMenuBeanList.add(new MenuBean("倍速x1.0", 1f, true));
        speedMenuBeanList.add(new MenuBean("倍速x1.25", 1.25f, false));
        speedMenuBeanList.add(new MenuBean("倍速x1.5", 1.5f, false));
        speedMenuBeanList.add(new MenuBean("倍速x1.75", 1.75f, false));
        speedMenuBeanList.add(new MenuBean("倍速x2.0", 2.0f, false));
        speedMenuBeanList.add(new MenuBean("倍速x2.5", 2.5f, false));
        speedMenuBeanList.add(new MenuBean("倍速x3.0", 3.0f, false));

        displayMenuBeanList.add(new MenuBean(getString(R.string.adaptiveScale), 0, true));
        displayMenuBeanList.add(new MenuBean(getString(R.string.stretchFullScreen), 1, false));
        displayMenuBeanList.add(new MenuBean(getString(R.string.cropFullScreen), 2, false));
        displayMenuBeanList.add(new MenuBean(getString(R.string.originalSize), 3, false));
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

    private void setDefaultSpeed() {
        Utils.showSingleChoiceAlert(this,
                null,
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<RemoteAction> createPipActions() {
        List<RemoteAction> actions = new ArrayList<>();
        int userSetSpeed = SharedPreferencesUtils.getUserSetSpeed();
        // 快退按钮
        Icon rewindIcon = Icon.createWithResource(this, R.drawable.round_fast_rewind_24); // 自定义图标
        PendingIntent rewindIntent = PendingIntent.getBroadcast(
                this, 1, new Intent(ACTION_REWIND).setPackage(getPackageName()), PendingIntent.FLAG_IMMUTABLE);
        actions.add(new RemoteAction(rewindIcon, String.format("快退%s秒", userSetSpeed), String.format("快退%s秒", userSetSpeed), rewindIntent));

        // 播放/暂停按钮
        Icon playPauseIcon = Icon.createWithResource(this,
                Jzvd.CURRENT_JZVD.state == Jzvd.STATE_PLAYING ? R.drawable.round_pause_24 : R.drawable.round_play_arrow_24);
        PendingIntent playPauseIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(ACTION_PLAY_PAUSE).setPackage(getPackageName()), PendingIntent.FLAG_IMMUTABLE);
        actions.add(new RemoteAction(playPauseIcon, "播放/暂停", "播放/暂停", playPauseIntent));

        // 快进按钮
        Icon forwardIcon = Icon.createWithResource(this, R.drawable.round_fast_forward_24);
        PendingIntent forwardIntent = PendingIntent.getBroadcast(
                this, 2, new Intent(ACTION_FORWARD).setPackage(getPackageName()), PendingIntent.FLAG_IMMUTABLE);
        actions.add(new RemoteAction(forwardIcon, String.format("快进%s秒", userSetSpeed), String.format("快进%s秒", userSetSpeed), forwardIntent));
        return actions;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private final BroadcastReceiver pipReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (player == null) return;
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_PLAY_PAUSE:
                    if (player.state == Jzvd.STATE_PLAYING) {
                        player.onStatePause();
                        player.mediaInterface.pause();
                    } else {
                        player.onStatePlaying();
                        player.mediaInterface.start();
                    }
                    updatePipActions();
                    break;
                case ACTION_FORWARD:
                    player.setFast(true);
                    break;
                case ACTION_REWIND:
                    player.setFast(false);
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePipActions() {
        if (isInPictureInPictureMode()) {
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setActions(createPipActions());
            Rational safeRatio = BasePlayerActivity.getSafeAspectRatio(player.videoWidth, player.videoHeight);
            pipBuilder.setAspectRatio(safeRatio);
            setPictureInPictureParams(pipBuilder.build());
        }
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
        PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
        pipBuilder.setActions(createPipActions());
        Rational safeRatio = BasePlayerActivity.getSafeAspectRatio(player.videoWidth, player.videoHeight);
        pipBuilder.setAspectRatio(safeRatio);
        enterPictureInPictureMode(pipBuilder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REWIND);
        filter.addAction(ACTION_FORWARD);
        filter.addAction(ACTION_PLAY_PAUSE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(pipReceiver, filter);
        }
        if (danmuPresenter != null)
            danmuPresenter.registerEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            unregisterReceiver(pipReceiver);
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
                    R.drawable.round_warning_24,
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
                    // 弹幕URL
                    danmuUrl = jsonObject.getString("ggdmapi");
                    // 独立弹幕库
                    /*String dmid = jsonObject.getString("dmid");
                    danmuUrl = danmuUrl.split("&")[0] + "&id=" + dmid;*/
                    play(videoUrl);
                } else
                    Utils.showAlert(this,
                            R.drawable.round_warning_24,
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
                        R.drawable.round_warning_24,
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
//            application.showToastMsg("开始播放下一集", DialogXTipEnum.DEFAULT);
            TextViewAnimator.showMultipleWithFade(player.tipsView, "开始播放下一集", 300, 1000);
            clickIndex++;
            changePlayUrl(clickIndex);
        } else {
            player.releaseDanMu();
//            application.showToastMsg("全部播放完毕", DialogXTipEnum.SUCCESS);
            TextViewAnimator.showMultipleWithFade(player.tipsView, "全部播放完毕", 300, 1000);
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
//                player.danmuInfoView.setText("已加载"+ danmuDataBeanList.size() + "条弹幕");
                player.danmuInfoView.setText("弹幕接口响应正常");
                player.danmuInfoView.setVisibility(View.VISIBLE);
                String json = JSON.toJSONString(danmuDataBeanList);
                LogUtil.logInfo("弹幕信息", json);
                InputStream result = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
                player.danmakuParser = createParser(true, result);
                player.createDanmu();
                player.danmakuContext.setFTDanmakuVisibility(player.showTopDanmaku);
                player.danmakuContext.setFBDanmakuVisibility(player.showBottomDanmaku);
                player.danmakuContext.setR2LDanmakuVisibility(player.showRollDanmaku);
                player.danmakuContext.setMaximumVisibleSizeInScreen(player.damuShowCount);
                player.danmakuContext.mGlobalFlagValues.updateFilterFlag();
                if (player.danmakuView.isPrepared()) {
                    player.danmakuView.restart();
                }
                player.danmakuView.prepare(player.danmakuParser, player.danmakuContext);
                /*if (player.seekToInAdvance > 0) {
                    new Handler().postDelayed(() -> {
                        // 一秒后定位弹幕时间为用户上次观看位置
                        player.seekDanmu(player.seekToInAdvance);
                    }, 1000);
                }*/
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
        if (!Utils.isNullOrEmpty(dmid) && !loadDlDanmu) {
            errorDanmuInfo(msg + ",请求独立弹幕库");
            danmuUrl = "https://dmku.hls.one/?ac=dm&id="+dmid;
            getDanmu();
        } else
            errorDanmuInfo(msg);
    }

    @Override
    public void netErrorDanmu(String msg) {
        if (isFinishing()) return;
        if (player.loadError) return;
        errorDanmuInfo(msg);
    }

    private void errorDanmuInfo(String msg) {
        runOnUiThread(() -> {
            try {
                application.showToastMsg(msg, DialogXTipEnum.WARNING);
                player.danmuInfoView.setText("点击重新获取弹幕数据");
                player.danmuInfoView.setClickable(true);
                player.danmuInfoView.setOnClickListener(v -> getDanmu());
            } catch (Exception e) {
                application.showToastMsg(e.getMessage(), DialogXTipEnum.ERROR);
            }
        });
    }

    private int flipValue = 1;
    @Override
    public void setFlip() {
        flipValue = flipValue == 1 ? -1 : 1;
        // 镜像翻转
        player.getCustomTextureView().setScaleX(flipValue);
        TextViewAnimator.showMultipleWithFade(player.tipsView, (isFlipped() ? "镜像翻转开启" : "镜像翻转关闭"), 300, 1000);
    }

    public boolean isFlipped() {
        return flipValue == -1;
    }
    @Override
    public void setSpeed(MenuBean.MenuEnum menuEnum) {
        showMenuPopupView(menuEnum, speedMenuBeanList);
    }

    @Override
    public void setDisplay(MenuBean.MenuEnum menuEnum) {
        showMenuPopupView(menuEnum, displayMenuBeanList);
    }

    /**
     * 显示菜单弹窗
     * @param menuEnum
     * @param menuBeanList
     */
    public void showMenuPopupView(MenuBean.MenuEnum menuEnum, List<MenuBean> menuBeanList) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu_selection, null);
        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MenuAdapter adapter = new MenuAdapter(this, menuBeanList);
        recyclerView.setAdapter(adapter);
        PopupWindow popupWindow = new PopupWindow(popupView,
                Utils.dpToPx(this, 160),
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setAnimationStyle(R.style.PopupRightAnim);
        popupWindow.showAtLocation(player.tipsView.getRootView(), Gravity.END, 0, 0);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            MenuBean menuBean = (MenuBean) adapter1.getData().get(position);
            switch (menuEnum) {
                case SPEED:
                    if (player.currentSpeedIndex == position) return;
                    speedMenuBeanList.get(player.currentSpeedIndex).setSelected(false);
                    adapter.notifyItemChanged(player.currentSpeedIndex);
                    player.currentSpeedIndex = position;
                    menuBean.setSelected(true);
                    adapter.notifyItemChanged(position);
                    player.speedRet = (Float) menuBean.getValue();
                    player.mediaInterface.setSpeed((Float) menuBean.getValue());
                    player.tvSpeedView.setText(menuBean.getTitle());
                    TextViewAnimator.showMultipleWithFade(player.tipsView, menuBean.getTitle(), 300, 1000);
                    break;
                case DISPLAY:
                    if (player.displayIndex == position) return;
                    menuBeanList.get(player.displayIndex).setSelected(false);
                    adapter.notifyItemChanged(player.displayIndex);
                    player.displayIndex = position;
                    menuBean.setSelected(true);
                    Jzvd.setVideoImageDisplayType((Integer) menuBean.getValue());
                    String displayText = menuBean.getTitle();
                    player.displayView.setText(displayText);
                    TextViewAnimator.showMultipleWithFade(player.tipsView, displayText, 300, 1000);
                    break;
            }
            popupWindow.dismiss();
        });
    }
}
