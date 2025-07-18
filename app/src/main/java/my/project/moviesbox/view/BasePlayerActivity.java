package my.project.moviesbox.view;

import static cn.jzvd.Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER;
import static cn.jzvd.Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT;
import static cn.jzvd.Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP;
import static cn.jzvd.Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_FAVORITE;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_HISTORY;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_PLAYER_KERNEL;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DramaAdapter;
import my.project.moviesbox.adapter.MenuAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.bean.MenuBean;
import my.project.moviesbox.config.JZExoPlayer;
import my.project.moviesbox.config.JZMediaIjk;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.custom.AutoLineFeedLayoutManager;
import my.project.moviesbox.custom.DanmakuJsonParser;
import my.project.moviesbox.custom.DanmukuXmlParser;
import my.project.moviesbox.custom.JZPlayer;
import my.project.moviesbox.custom.TextViewAnimator;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.enums.VideoUrlChangeEnum;
import my.project.moviesbox.event.RefreshDownloadEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DanmuDataBean;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.presenter.DanmuPresenter;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.presenter.VideoPresenter;
import my.project.moviesbox.service.DLNAService;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.StatusBarUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoAlertUtils;
import my.project.moviesbox.utils.VideoUtils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: BasePlayerActivity
  * @描述: 播放器视图基类
  * @作者: Li Z
  * @日期: 2024/2/4 16:34
  * @版本: 1.0
 */
public abstract class BasePlayerActivity extends BaseActivity implements JZPlayer.CompleteListener, JZPlayer.TouchListener,
        JZPlayer.ShowOrHideChangeViewListener,  JZPlayer.OnProgressListener, JZPlayer.PlayingListener, JZPlayer.PauseListener, JZPlayer.OnQueryDanmuListener, JZPlayer.ActivityOrientationListener, JZPlayer.FlipListener, DanmuContract.View, JZPlayer.SpeedListener, JZPlayer.DisplayListener {
    @BindView(R.id.episodes_view)
    LinearLayout episodesView;
    @BindView(R.id.config_view)
    LinearLayout configView;
    @BindView(R.id.player)
    JZPlayer player; // 播放器
    @BindView(R.id.rv_list)
    RecyclerView recyclerView; // 剧集列表
    @BindView(R.id.drawer_layout) // 侧滑抽屉
    DrawerLayout drawerLayout;
    @BindView(R.id.hide_progress)
    MaterialSwitch hideProgressSc; // 播放时隐藏进度条
    @BindView(R.id.other_view)
    LinearLayout otherView; // 其他视图
    protected String
            vodTitle, // 影视名称
            dramaTitle, // 观看集数
            url, // 播放地址
            dramaUrl; // 源地址
    protected DramaAdapter dramaAdapter; // 集数适配器
    @BindView(R.id.speed)
    TextView speedTextView; // 快进设置文本
    @BindArray(R.array.fast_forward_item)
    protected String[] speedsStrItems; // 快进选项
    @BindArray(R.array.fast_forward_set_item)
    protected int[] speedsIntItems; // 倍速选项
    protected int userSpeed = 2; // 倍数值
    protected int clickIndex; // 当前点击剧集
    protected boolean hasPreVideo = false; // 是否有上一集
    protected boolean hasNextVideo = false; // 是否有下一集
    protected String vodId; // 影视ID
    protected long playPosition; // 当前播放进度
    protected long videoDuration; // 视频总长度
    protected boolean hasPosition = false; // 是否存在历史播放进度
    protected long userSavePosition = 0; // 历史播放进度
    @BindView(R.id.auto_play_next_video)
    MaterialSwitch autoPlayNextVideoSwitch; // 是否开启自动播放下一集控件
    protected boolean playNextVideo; // 是否开启自动播放下一集
    protected List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>(); // 影视剧集集合
    protected VideoPresenter videoPresenter;
    protected int nowSource = 0; // 当前源
    protected List<TDownloadDataWithFields> downloadDataBeans = new ArrayList<>(); // 本地剧集集合
    protected String localFilePath; // 本地视频路劲
    protected String downloadDataId; // 集数下载ID
    protected DanmuPresenter danmuPresenter = new DanmuPresenter(this);
    protected List<DialogItemBean> nextPlayUrl = new ArrayList<>(); // 下一集播放地址
    protected static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    protected static final long RETRY_DELAY_MILLIS = 3000; // 等待3秒重试
    protected int retryCount = 0; // 重试次数
    protected VideoAlertUtils videoAlertUtils;
    private final List<MenuBean> speedMenuBeanList = new ArrayList<>();
    private final List<MenuBean> displayMenuBeanList = new ArrayList<>();

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {}

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_player;
    }

    @Override
    protected void init() {
        player.isLocalVideo = isLocalVideo();
        setActivityName();
        Bundle bundle = getIntent().getExtras();
        setBundleData(bundle);
        initCustomData();
        initAdapter();
        initPlayerPreNextTag();
        initPlayerView();
        initNavConfigView();
        initUserConfig();
        initMenuList();
        videoAlertUtils = new VideoAlertUtils(this);
    }

    /**
     * 是否是本地视频
     * @return
     */
    protected abstract boolean isLocalVideo();

    /**
     * 自定义属性初始化抽象方法
     */
    protected abstract void initCustomData();

    protected abstract void setActivityName();

    protected abstract void setBundleData(Bundle bundle);

    /**
     * 设置抽屉
     */
    private void initNavConfigView() {
        //设置右面的侧滑菜单只能通过代码来打开
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * 设置上/下一集数据抽象方法
     */
    protected abstract void setPreNextData();

    /**
     * 设置上一集/下一集按钮
     */
    protected void initPlayerPreNextTag() {
        if (dramasItems.size() > 0) {
            hasPreVideo = clickIndex != 0;
            setPreNextData();
        }
    }

    /**
     * 初始化播放器相关事件
     */
    private void initPlayerView() {
        if (dramasItems.size() == 0)
            player.selectDramaView.setVisibility(View.GONE);
        player.configView.setOnClickListener(v -> setDrawerOpen(GravityCompat.START));
        player.openDrama.setOnClickListener(view -> setDrawerOpen(GravityCompat.END));
        player.selectDramaView.setOnClickListener(view -> setDrawerOpen(GravityCompat.END));
        player.setListener(this, this, this, this, this, this, this, this, this, this, this, this);
        player.backButton.setOnClickListener(v -> {
            Utils.setVibration(v);
            finish();
        });
        player.preVideo.setOnClickListener(v -> {
            clickIndex--;
            Utils.setVibration(v);
            changePlayUrl(VideoUrlChangeEnum.PRE, clickIndex);
        });
        player.nextVideo.setOnClickListener(v -> {
            clickIndex++;
            Utils.setVibration(v);
            changePlayUrl(VideoUrlChangeEnum.NEXT, clickIndex);
        });
        player.openDamuConfig = SharedPreferencesUtils.getUserSetOpenDanmu();
        player.hasDanmuConfig = SourceEnum.hasDanmuConfigBySource(SharedPreferencesUtils.getDefaultSource());
        if (isLocalVideo()) {
            player.danmuView.setVisibility(View.INVISIBLE);
            player.danmuInfoView.setVisibility(View.INVISIBLE);
        } else if (player.openDamuConfig && player.hasDanmuConfig)  {
            // 开启了弹幕开关且该源支持弹幕
            player.danmuView.setVisibility(View.VISIBLE);
            player.danmuInfoView.setVisibility(View.VISIBLE);
        }
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
        player.tvSpeedView.setVisibility(gtSdk23() ? View.VISIBLE : View.GONE);
        player.pipView.setOnClickListener(v -> startPic());
        player.playingShow();
        playVideo();
    }

    /**
     * 设置抽屉打开方式
     * @param drawerGravity
     */
    private void setDrawerOpen(int drawerGravity) {
        if (!Utils.isFastClick()) return;
        if (drawerLayout.isDrawerOpen(drawerGravity))
            drawerLayout.closeDrawer(drawerGravity);
        else drawerLayout.openDrawer(drawerGravity);
    }

    /**
     * 播放视频抽象方法
     */
    protected abstract void playVideo();

    /**
     * 设置剧集列表适配器
     */
    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new AutoLineFeedLayoutManager());
        setAdapter();
    }

    /**
     * 设置剧集适配器数据抽象方法
     */
    protected abstract void setAdapter();

    /**
     * 初始化用户设置
     */
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
        autoPlayNextVideoSwitch.setChecked(playNextVideo);
        autoPlayNextVideoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setUserAutoPlayNextVideo(isChecked);
            playNextVideo = isChecked;
        });
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

        displayMenuBeanList.add(new MenuBean(getString(R.string.adaptiveScale), VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER, true));
        displayMenuBeanList.add(new MenuBean(getString(R.string.stretchFullScreen), VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT, false));
        displayMenuBeanList.add(new MenuBean(getString(R.string.cropFullScreen), VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP, false));
        displayMenuBeanList.add(new MenuBean(getString(R.string.originalSize), VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL, false));
    }

    /**
     * 设置用户保存的快进、快退时长
     * @param index 下标
     */
    private void setUserSpeedConfig(int index) {
        SharedPreferencesUtils.setUserSetSpeed(speedsIntItems[index]);
        speedTextView.setText(speedsStrItems[index]);
        player.setFastQuickText(speedsStrItems[index]);
        userSpeed = index;
    }

    /**
     * 播放器设置点击事件
     * @param relativeLayout
     */
    @OnClick({R.id.speed_config, R.id.player_config, R.id.browser_config})
    public void configBtnClick(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.speed_config: // 设置快进/快退时长
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
                break;
            case R.id.player_config: // 使用外部播放器播放
                Utils.selectVideoPlayer(this, url);
                break;
            case R.id.browser_config: // 访问源网站
                Utils.viewInChrome(this, dramaUrl);
                break;
        }
    }

    /**
     * 切换集数
     * @param changeEnum 切换集数点击事件枚举
     * @param position 集数下标
     */
    protected void changePlayUrl(VideoUrlChangeEnum changeEnum, int position) {
        player.releaseDanMu();
        player.danmuInfoView.setVisibility(View.GONE);
        dramaAdapter.getData().get(position).setSelected(true);
        dramaAdapter.notifyItemChanged(position);
        // 本地视频换集 或 不需要解析的直接播放
        if (isLocalVideo()) {
            changeLocalPlayUrl(position);
            return;
        }
        // 网络视频换集
        retryCount = 0;
        clickIndex = position;
        initPlayerPreNextTag();
        Jzvd.releaseAllVideos();
        saveProgress();
        player.playingShow();
        if (!parserInterface.playUrlNeedParser()) {
            // 如果不需要解析则直接播放
            playNetworkVideo(dramaAdapter.getData().get(position).getUrl());
            return;
        }
        DetailsDataBean.DramasItem bean= getItemByPosition(changeEnum, position);
        dramaUrl = bean.getUrl();
        dramaTitle = bean.getTitle();
        switch (changeEnum) {
            case CLICK:
            case PRE:
                parseVideoUrl(dramaTitle);
                break;
            case NEXT:
                if (Utils.isNullOrEmpty(nextPlayUrl))
                    parseVideoUrl(dramaTitle);
                else
                    handleNextPlayUrl(nextPlayUrl);
                break;
        }
    }

    /**
     * 播放下一集
     * @param nextPlayUrl 下一集播放地址集合
     */
    private void handleNextPlayUrl(List<DialogItemBean> nextPlayUrl) {
        if (nextPlayUrl.size() == 1)
            playNetworkVideo(nextPlayUrl.get(0).getUrl());
        else
           alertDialog = videoAlertUtils.showMultipleVideoSources(
                    nextPlayUrl,
                    (adapter, view, position) -> {
                       playNetworkVideo(nextPlayUrl.get(position).getUrl());
                       alertDialog.dismiss();
                   },
                    true);
    }

    /**
     * 本地视频换集
     * @param position
     */
    protected void changeLocalPlayUrl(int position) {
        saveProgress();
        clickIndex = position;
        initPlayerPreNextTag();
        DetailsDataBean.DramasItem bean = getItemByPosition(VideoUrlChangeEnum.CLICK, position);
        Jzvd.releaseAllVideos();
        downloadDataId = bean.getDownloadDataId();
        playLocalVideo(bean.getUrl(), bean.getTitle());
    }

    /**
     * 获取当前点击播放集数数据抽象方法
     * @param changeEnum 切换集数点击事件枚举
     * @param position 集数下标
     * @return
     */
    protected abstract DetailsDataBean.DramasItem getItemByPosition(VideoUrlChangeEnum changeEnum, int position);

    /**
     * 解析视频地址抽象方法
     * @param dramaTitle 集数
     */
    protected abstract void parseVideoUrl(String dramaTitle);

    /**
     * 保存进度
     */
    private void saveProgress() {
        boolean completed = Utils.videoHasComplete(playPosition, videoDuration);
        boolean gt2second = playPosition > 2000;
        long witchPosition = completed ? 0 : (gt2second ? playPosition : 0);
        if (isLocalVideo()) {
            LogUtil.logInfo("witchPosition", witchPosition+"");
            LogUtil.logInfo("videoDuration", videoDuration+"");
            LogUtil.logInfo("downloadDataId", downloadDataId+"");
            TDownloadDataManager.updateDownloadDataProgressById(witchPosition, videoDuration, downloadDataId);
            EventBus.getDefault().post(new RefreshDownloadEvent(downloadDataId, witchPosition, videoDuration));
        } else
            THistoryManager.updateHistory(vodId, dramaUrl, witchPosition, videoDuration);
    }

    /**
     * 播放视频
     * @param videoUrl
     */
    protected void playNetworkVideo(String videoUrl) {
        nextPlayUrl = new ArrayList<>();
        hideNavBar();
        TFavoriteManager.updateFavorite(dramaUrl, dramaTitle, vodId);
        TVideoManager.addVideoHistory(vodId, dramaUrl, nowSource, dramaTitle);
        Jzvd.releaseAllVideos();
        player.setUp(videoUrl, vodTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, VideoUtils.getUserPlayerKernel());
        userSavePosition = THistoryManager.getPlayPosition(vodId, dramaUrl);
        player.seekToInAdvance = userSavePosition;//跳转到指定的播放进度
        hasPosition = userSavePosition > 0;
        player.startVideo();
        player.startButton.performClick();//响应点击事件
        getDanmu();
    }

    /**
     * 获取弹幕
     */
    private void getDanmu() {
        if (isLocalVideo()) // 本地视频不支持弹幕
            return;
        if (player.openDamuConfig && player.hasDanmuConfig) {
            new Thread(() -> danmuPresenter.loadDanmu(getDanmuParams())).start();
        }
    }

    /**
     * 获取弹幕参数配置 需自己实现
     * @return
     */
    protected abstract String[] getDanmuParams();

    /**
     * 播放本地视频
     * @param localVideoPath 本地视频地址
     * @param dramaTitle 集数
     */
    protected void playLocalVideo(String localVideoPath, String dramaTitle) {
        player.playingShow();
        player.localVideoPath = localVideoPath;
        String localFilePath = Uri.fromFile(new File(localVideoPath)).toString();
        try {
            localFilePath = URLDecoder.decode(localFilePath, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.setUp(localFilePath, vodTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, VideoUtils.getUserPlayerKernel());
        userSavePosition = TDownloadDataManager.queryDownloadDataProgressById(downloadDataId);
        player.seekToInAdvance = userSavePosition;//跳转到指定的播放进度
        hasPosition = userSavePosition > 0;
        player.startVideo();
        player.startButton.performClick();//响应点击事件
        getDanmu();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else finish();
    }

    /**
     * 是否为分屏模式
     *
     * @return
     */
    public boolean inMultiWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return this.isInMultiWindowMode();
        else
            return false;
    }

    /**
     * 画中画
     * @param isInPictureInPictureMode True if the activity is in picture-in-picture mode.
     * @param newConfig The new configuration of the activity with the state
     *                  {@param isInPictureInPictureMode}.
     */
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

    /**
     * 窗口变化
     * @param isInMultiWindowMode True if the activity is in multi-window mode.
     * @param newConfig The new configuration of the activity with the state
     *                  {@param isInMultiWindowMode}.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        if (isInMultiWindowMode)
            Jzvd.goOnPlayOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        hideNavBar();
        if (!inMultiWindow()) Jzvd.goOnPlayOnResume();
    }

    @Override
    protected void onStop() {
        saveProgress();
        super.onStop();
        if (danmuPresenter != null)
            danmuPresenter.unregisterEventBus();
        if (null != videoPresenter)
            videoPresenter.unregisterEventBus();
    }

    public void startPic() {
        if (gtSdk26())
            new Handler().postDelayed(this::enterPicInPic, 500);
    }

    /**
     * Android 8.0 画中画
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enterPicInPic() {
        PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
//        pipBuilder.setAspectRatio(new Rational(player.videoWidth, player.videoHeight));
        Rational safeRatio = getSafeAspectRatio(player.videoWidth, player.videoHeight);
        pipBuilder.setAspectRatio(safeRatio);
        enterPictureInPictureMode(pipBuilder.build());
    }

    public static Rational getSafeAspectRatio(int width, int height) {
        final float MIN_RATIO = 0.418410f;
        final float MAX_RATIO = 2.39f;
        if (width <= 0 || height <= 0) return new Rational(16, 9); // fallback
        float ratio = (float) width / height;
        // 修正比例在合法范围内
        if (ratio < MIN_RATIO) {
            ratio = MIN_RATIO;
        } else if (ratio > MAX_RATIO) {
            ratio = MAX_RATIO;
        }
        // 转为 Rational（例如 1.78 → 178 : 100）
        int scale = 1000; // 精度控制
        int w = (int) (ratio * scale);
        int h = scale;
        return new Rational(w, h);
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    public void playing() {
        if (hasPosition) {
            application.showToastMsg("已定位到上次观看位置" + JZUtils.stringForTime(userSavePosition), DialogXTipEnum.DEFAULT);
            hasPosition = false;
        }
        if (!isLocalVideo() && parserInterface.playUrlNeedParser() && hasNextVideo && nextPlayUrl.size() == 0)
            // 不是本地视频播放 且 需要解析 且 存在下一集 且 下一集未获取
            // 播放当前视频时获取下一集播地址
            getNextPlayUrl();
    }

    /**
     * 获取下一集播放地址抽象方法
     */
    protected abstract void getNextPlayUrl();

    @Override
    public void pause() {
        saveProgress();
    }

    @Override
    public void complete() {
        saveProgress();
        if (hasNextVideo && playNextVideo) {
            application.showToastMsg("开始播放下一集", DialogXTipEnum.DEFAULT);
            clickIndex++;
            changePlayUrl(VideoUrlChangeEnum.NEXT, clickIndex);
        } else {
            player.releaseDanMu();
            application.showToastMsg("全部播放完毕", DialogXTipEnum.SUCCESS);
            if (!drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void touch() {
        hideNavBar();
    }

    @Override
    public void showOrHideChangeView() {
        player.preVideo.setVisibility(hasPreVideo ? View.VISIBLE : View.GONE);
        player.nextVideo.setVisibility(hasNextVideo ? View.VISIBLE : View.GONE);
    }

    @Override
    public void getPosition(long position, long duration) {
        playPosition = position;
        videoDuration = duration;
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
    public void successDanmuXml(String content) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (player.loadError) return;
            try {
                player.danmuInfoView.setText("弹幕接口响应正常");
                player.danmuInfoView.setVisibility(View.VISIBLE);
                InputStream result = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                player.danmakuParser = createParser(false, result);
                player.createDanmu();
                if (player.danmakuView.isPrepared()) {
                    player.danmakuView.restart();
                }
                player.danmakuView.prepare(player.danmakuParser, player.danmakuContext);
                if (!player.open_danmu) {
                    player.hideDanmmu();
                }
                if (userSavePosition > 0) {
                    new Handler().postDelayed(() -> {
                        // 一秒后定位弹幕时间为用户上次观看位置
                        player.seekDanmu(userSavePosition);
                    }, 1000);
                }
            } catch (Exception e) {
                application.showToastMsg(e.getMessage(), DialogXTipEnum.ERROR);
            }
        });
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
                if (!player.open_danmu) {
                    player.hideDanmmu();
                }
                if (userSavePosition > 0) {
                    new Handler().postDelayed(() -> {
                        // 一秒后定位弹幕时间为用户上次观看位置
                        player.seekDanmu(userSavePosition);
                    }, 1000);
                }
            } catch (Exception e) {
                application.showToastMsg(e.getMessage(), DialogXTipEnum.ERROR);
            }
        });
    }

    @Override
    public void errorDanmu(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> application.showToastMsg(msg, DialogXTipEnum.ERROR));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (danmuPresenter != null)
            danmuPresenter.registerEventBus();
        if (null != videoPresenter)
            videoPresenter.registerEventBus();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, DLNAService.class));
        if (!isLocalVideo()) {
            EventBus.getDefault().post(REFRESH_FAVORITE);
            EventBus.getDefault().post(REFRESH_HISTORY);
        }
        player.releaseDanMu();
        Jzvd.releaseAllVideos();
        player.danmakuView = null;
        if (danmuPresenter != null)
            danmuPresenter.detachView();
        if (null != videoPresenter)
            videoPresenter.detachView();
        App.removeDestroyActivity("player");
        emptyRecyclerView(recyclerView);
        if (videoAlertUtils != null)
            videoAlertUtils.release();
        super.onDestroy();
    }

    @Override
    public void queryDamu(String queryDanmuTitle, String queryDanmuDrama) {
        player.releaseDanMu();
        player.danmuInfoView.setVisibility(View.GONE);
        danmuPresenter.loadDanmu(getDanmuParams());
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
