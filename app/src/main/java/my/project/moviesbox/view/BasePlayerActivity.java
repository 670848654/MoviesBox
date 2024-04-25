package my.project.moviesbox.view;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.custom.AutoLineFeedLayoutManager;
import my.project.moviesbox.custom.DanmakuJsonParser;
import my.project.moviesbox.custom.DanmukuXmlParser;
import my.project.moviesbox.custom.JZPlayer;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.event.RefreshDownloadEvent;
import my.project.moviesbox.event.RefreshEvent;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.presenter.DanmuPresenter;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.presenter.VideoPresenter;
import my.project.moviesbox.service.DLNAService;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.StatusBarUtil;
import my.project.moviesbox.utils.Utils;
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
        JZPlayer.ShowOrHideChangeViewListener,  JZPlayer.OnProgressListener, JZPlayer.PlayingListener, JZPlayer.PauseListener, JZPlayer.OnQueryDanmuListener, JZPlayer.ActivityOrientationListener, DanmuContract.View {
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
    protected AlertDialog alertDialog; // 弹窗
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
    protected DanmuPresenter danmuPresenter;
    protected List<String> nextPlayUrl = new ArrayList<>(); // 下一集播放地址
    protected static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    protected static final long RETRY_DELAY_MILLIS = 3000; // 等待3秒重试
    protected int retryCount = 0; // 重试次数

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
    }

    /**
     * 是否是本地视频
     * @return
     */
    protected abstract boolean isLocalVideo();

    /**
     * 设置上/下一集数据
     */
    protected abstract void setPreNextData();

    private void initNavConfigView() {
        //设置右面的侧滑菜单只能通过代码来打开
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    protected void initPlayerPreNextTag() {
        hasPreVideo = clickIndex != 0;
        setPreNextData();
    }

    private void initPlayerView() {
        Jzvd.SAVE_PROGRESS = false;
        player.configView.setOnClickListener(v -> setDrawerOpen(GravityCompat.START));
        player.openDrama.setOnClickListener(view -> setDrawerOpen(GravityCompat.END));
        player.selectDramaView.setOnClickListener(view -> setDrawerOpen(GravityCompat.END));
        player.setListener(this, this, this, this, this, this, this, this, this, this);
//        player.WIFI_TIP_DIALOG_SHOWED = true;
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
        if (isLocalVideo())
            player.danmuView.setVisibility(View.GONE);
        player.tvSpeedView.setVisibility(gtSdk23() ? View.VISIBLE : View.GONE);
        player.pipView.setOnClickListener(v -> startPic());
        player.playingShow();
        playVideo();
    }

    private void setDrawerOpen(int drawerGravity) {
        if (!Utils.isFastClick()) return;
        if (drawerLayout.isDrawerOpen(drawerGravity))
            drawerLayout.closeDrawer(drawerGravity);
        else drawerLayout.openDrawer(drawerGravity);
    }

    protected abstract void playVideo();

    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new AutoLineFeedLayoutManager());
        setAdapter();
    }

    protected abstract void setAdapter();

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

    private void setUserSpeedConfig(int userSet) {
        SharedPreferencesUtils.setUserSetSpeed(speedsIntItems[userSet]);
        speedTextView.setText(speedsStrItems[userSet]);
        userSpeed = userSet;
    }

    @OnClick({R.id.speed_config, R.id.player_config, R.id.browser_config})
    public void configBtnClick(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.speed_config:
                setDefaultSpeed();
                break;
            case R.id.player_config:
                Utils.selectVideoPlayer(this, url);
                break;
            case R.id.browser_config:
                Utils.viewInChrome(this, dramaUrl);
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

    protected void changePlayUrl(int position) {
        player.releaseDanMu();
        player.danmuInfoView.setVisibility(View.GONE);
        dramaAdapter.getData().get(position).setSelected(true);
        dramaAdapter.notifyDataSetChanged();
        if (isLocalVideo()) {
            changeLocalPlayUrl(position);
            return;
        }
        retryCount = 0;
        clickIndex = position;
        initPlayerPreNextTag();
        DetailsDataBean.DramasItem bean = setVodDramas(position);
        Jzvd.releaseAllVideos();

        saveProgress();
        dramaUrl = bean.getUrl();
        dramaTitle = bean.getTitle();
        player.playingShow();
        if (Utils.isNullOrEmpty(nextPlayUrl))
            changeVideo(bean.getTitle());
        else {
            if (nextPlayUrl.size() == 1)
                play(nextPlayUrl.get(0));
            else
                VideoUtils.showMultipleVideoSources(this,
                        nextPlayUrl,
                        (dialog, index) -> play(nextPlayUrl.get(index)),
                        null,
                        true);
        }
    }

    protected void changeLocalPlayUrl(int position) {
        saveProgress();
        clickIndex = position;
        initPlayerPreNextTag();
        DetailsDataBean.DramasItem bean = setVodDramas(position);
        Jzvd.releaseAllVideos();
        downloadDataId = bean.getDownloadDataId();
        toPlay(bean.getUrl(), bean.getTitle());
    }

    protected abstract DetailsDataBean.DramasItem setVodDramas(int position);

    protected abstract void changeVideo(String title);

    private void saveProgress() {
        boolean completed = Utils.videoHasComplete(playPosition, videoDuration);
        boolean gt2second = playPosition > 2000;
        long witchPosition = completed ? 0 : (gt2second ? playPosition : 0);
        if (isLocalVideo()) {
            TDownloadDataManager.updateDownloadDataProgressById( witchPosition, videoDuration, downloadDataId);
            EventBus.getDefault().post(new RefreshDownloadEvent(downloadDataId, witchPosition, videoDuration));
        } else
            THistoryManager.updateHistory(vodId, dramaUrl, witchPosition, videoDuration);
    }

    /**
     * 播放视频
     * @param playUrl
     */
    protected void play(String playUrl) {
        nextPlayUrl = new ArrayList<>();
        hideNavBar();
        TFavoriteManager.updateFavorite(dramaUrl, dramaTitle, vodId);
        TVideoManager.addVideoHistory(vodId, dramaUrl, nowSource, dramaTitle);
        Jzvd.releaseAllVideos();
        player.setUp(playUrl, vodTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, VideoUtils.getUserPlayerKernel());
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
            danmuPresenter = new DanmuPresenter(this, getDanmuParams());
            danmuPresenter.loadDanmu();
        }
    }

    /**
     * 获取弹幕参数配置 需自己实现
     * @return
     */
    protected abstract String[] getDanmuParams();

    protected void toPlay(String path, String dramaTitle) {
        player.playingShow();
        player.localVideoPath = path;
        String localFilePath = Uri.fromFile(new File(path)).toString();
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

    protected abstract void initCustomData();

    protected abstract void setActivityName();

    protected abstract void setBundleData(Bundle bundle);

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

    @Override
    protected void onStop() {
        saveProgress();
        super.onStop();
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
        PictureInPictureParams builder = new PictureInPictureParams.Builder().build();
        enterPictureInPictureMode(builder);
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
            application.showToastMsg("已定位到上次观看位置" + JZUtils.stringForTime(userSavePosition));
            hasPosition = false;
        }
        if (!isLocalVideo() && hasNextVideo && nextPlayUrl.size() == 0)
            // 不是本地视频播放且存在下一集 播放当前视频时获取下一集播地址
            getNextPlayUrl();
    }

    protected abstract void getNextPlayUrl();

    @Override
    public void pause() {
        saveProgress();
    }

    @Override
    public void complete() {
        saveProgress();
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
    public void touch() {
        hideNavBar();
    }

    @Override
    public void finish() {
        if (null != videoPresenter) videoPresenter.detachView();
        player.releaseAllVideos();
        super.finish();
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
                application.showToastMsg(e.getMessage());
            }
        });
    }

    @Override
    public void successDanmuJson(JSONObject danmus) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            // JSON格式弹幕信息解析示例
            if (player.loadError) return;
            try {
                JSONArray jsonArray = danmus.getJSONObject("data").getJSONArray("data");
                int total = danmus.getJSONObject("data").getInteger("total");
                if (total == 0)
                    application.showToastMsg("未能查询到当前番剧弹幕信息，请检查番剧名称或集数名称是否异常，使用手动查询弹幕功能尝试~");
                else
                    application.showToastMsg("查询弹幕API成功，共"+total+"条弹幕~");
                player.danmuInfoView.setText("已加载"+ danmus.getJSONObject("data").getInteger("total") + "条弹幕！");
                player.danmuInfoView.setVisibility(View.VISIBLE);
                InputStream result = new ByteArrayInputStream(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
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
                application.showToastMsg(e.getMessage());
            }
        });
    }

    @Override
    public void errorDanmu(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> application.showToastMsg(msg));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, DLNAService.class));
        if (!isLocalVideo()) {
            EventBus.getDefault().post(new RefreshEvent(1));
            EventBus.getDefault().post(new RefreshEvent(2));
        }
        player.releaseDanMu();
        player.releaseAllVideos();
        player.danmakuView = null;
        if (danmuPresenter != null)
            danmuPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void queryDamu(String queryDanmuTitle, String queryDanmuDrama) {
        player.releaseDanMu();
        player.danmuInfoView.setVisibility(View.GONE);
        danmuPresenter = new DanmuPresenter(this, getDanmuParams());
        danmuPresenter.loadDanmu();
    }

    @Override
    public void setOrientation(int type) {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            setRequestedOrientation(type);
        }, 500);
    }
}
