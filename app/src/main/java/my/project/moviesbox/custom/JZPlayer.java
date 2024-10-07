package my.project.moviesbox.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.HashMap;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import master.flame.danmaku.BuildConfig;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.LocalVideoDLNAServer;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.UpnpActivity;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: JZPlayer
  * @描述: 自定义JZPlayer
  * @作者: Li Z
  * @日期: 2024/1/22 20:05
  * @版本: 1.0
 */
public class JZPlayer extends JzvdStd {
    float starX, startY;
    private Activity context;
    private CompleteListener listener;
    private TouchListener touchListener;
    private ShowOrHideChangeViewListener showOrHideChangeViewListener;
    private OnProgressListener onProgressListener;
    private PlayingListener playingListener;
    private PauseListener pauseListener;
    private OnQueryDanmuListener onQueryDanmuListener; // 手动查询弹幕点击监听 删除
    private ActivityOrientationListener activityOrientationListener;
//    private LoadingIndicator loadingView;
    private ImageView leftBLock, rightBlock;
    private boolean locked = false;
    public ImageView fastForward, quickRetreat;
    public TextView openDrama, preVideo, nextVideo, displayView, tvSpeedView, selectDramaView, changePlayerKernel; // queryDanmuView 手动查询弹幕View 删除
    public Button pipView, airplayView, configView;
    public int currentSpeedIndex = 1;
    public float speedRet = 1.0f;
    public boolean isLocalVideo;
    public String localVideoPath;
    private LocalVideoDLNAServer localVideoDLNAServer;
    public int displayIndex = 1;
    private boolean longPressing = false;
    private RelativeLayout longPressBgView;
    private boolean isLongClick = false;
    // 弹幕
    public boolean openDamuConfig; // 是否开启弹幕
    public boolean hasDanmuConfig; // 站点是否支持弹幕
    public DanmakuView danmakuView;
    public DanmakuContext danmakuContext;
    public BaseDanmakuParser danmakuParser;
    public ImageView danmuView;
    public TextView danmuInfoView;
    public String queryDanmuTitle = "";
    public boolean open_danmu = true;
    public boolean loadError = false;
    private String[] speeds = Utils.getArray(R.array.speed_item);
    private ParserInterface parserInterface = ParserInterfaceFactory.getParserInterface();

    public JZPlayer(Context context) { super(context); }

    public JZPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Activity activity, CompleteListener listener,
                            TouchListener touchListener, ShowOrHideChangeViewListener showOrHideChangeViewListener,
                            OnProgressListener onProgressListener, PlayingListener playingListener, PauseListener pauseListener,
                            OnQueryDanmuListener onQueryDanmuListener, ActivityOrientationListener activityOrientationListener) {
        this.context = activity;
        this.listener = listener;
        this.touchListener = touchListener;
        this.onProgressListener = onProgressListener;
        this.playingListener = playingListener;
        this.pauseListener = pauseListener;
        this.showOrHideChangeViewListener = showOrHideChangeViewListener;
        this.onQueryDanmuListener = onQueryDanmuListener;
        this.activityOrientationListener = activityOrientationListener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_jz_layout_std;
    }

    @Override
    public void init(Context context) {
        super.init(context);
        backButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        // 获取自定义添加的控件
//        loadingView = findViewById(R.id.progress);
        leftBLock = findViewById(R.id.left_lock);
        leftBLock.setOnClickListener(this);
        rightBlock = findViewById(R.id.right_lock);
        rightBlock.setOnClickListener(this);
        quickRetreat = findViewById(R.id.quick_retreat);
        quickRetreat.setOnClickListener(this);
        fastForward = findViewById(R.id.fast_forward);
        fastForward.setOnClickListener(this);
        configView = findViewById(R.id.config);
        tvSpeedView = findViewById(R.id.tvSpeed);
        tvSpeedView.setOnClickListener(this);
        airplayView = findViewById(R.id.airplay);
        airplayView.setOnClickListener(this);
        changePlayerKernel = findViewById(R.id.change_player_kernel);
        openDrama = findViewById(R.id.open_drama_list);
        preVideo = findViewById(R.id.pre_video);
        nextVideo = findViewById(R.id.next_video);
        displayView = findViewById(R.id.display);
        displayView.setOnClickListener(this);
        selectDramaView = findViewById(R.id.select_drama);
        danmuInfoView = findViewById(R.id.danmu_info);
        pipView = findViewById(R.id.pip);
        danmuView = findViewById(R.id.danmu);
        danmuView.setOnClickListener(this);
        /*queryDanmuView = findViewById(R.id.query_danmu);
        queryDanmuView.setOnClickListener(this);*/
        // 弹幕相关
        // queryDanmuView.setVisibility(INVISIBLE);
        danmuView.setVisibility(openDamuConfig && hasDanmuConfig ? VISIBLE : INVISIBLE);
        danmuInfoView.setVisibility(openDamuConfig && hasDanmuConfig ? VISIBLE : INVISIBLE);
        danmakuView = findViewById(R.id.jz_danmu);
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, Utils.isPad() ? 10 : 5); // 滚动弹幕最大显示5行,可设置多种类型限制行数
        maxLinesPair.put(BaseDanmaku.TYPE_FIX_TOP, Utils.isPad() ? 10 : 5);
        danmakuContext = DanmakuContext.create();
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(Utils.isPad() ? 1.3f : 1f)
                .setCacheStuffer(new SpannedCacheStuffer(), null) // 设置弹幕填充器
//                .setMaximumLines(maxLinesPair)
                .setMaximumLines(null)
                .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40);
        longPressBgView = findViewById(R.id.long_press_bg);
        LongPressEventView viewLongPress = findViewById(R.id.surface_container);
        viewLongPress.setLongPressEventListener(new LongPressEventView.LongPressEventListener() {
            @Override
            public void onLongClick(View v) {
                if (loadError || state != STATE_PLAYING) return;
                isLongClick = true;
                //震动反馈
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (mediaInterface != null) {
                    mediaInterface.setSpeed(2.0f);
                    longPressBgView.setVisibility(VISIBLE);
                }
                longPressing = true;
            }

            @Override
            public void onDisLongClick(View v) {
                if (loadError || state != STATE_PLAYING) return;
                if (mediaInterface != null) {
                    mediaInterface.setSpeed(speedRet);
                    longPressBgView.setVisibility(GONE);
                }
                longPressing = false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.left_lock:
            case R.id.right_lock:
                leftBLock.setTag(1);
                rightBlock.setTag(1);
                if (locked) {
                    // 已经上锁，再次点击解锁
                    changeUiToPlayingShow();
                    leftBLock.setImageResource(R.drawable.player_btn_locking);
                    rightBlock.setImageResource(R.drawable.player_btn_locking);
                    App.getInstance().showToastMsg(Utils.getString(R.string.turnOffScreenLock), DialogXTipEnum.DEFAULT);
                } else {
                    // 上锁
                    changeUiToPlayingClear();
                    leftBLock.setImageResource(R.drawable.player_btn_locking_pre);
                    rightBlock.setImageResource(R.drawable.player_btn_locking_pre);
                    App.getInstance().showToastMsg(Utils.getString(R.string.turnOnScreenLock), DialogXTipEnum.DEFAULT);
                }
                locked = !locked;
                break;
            case R.id.fast_forward:
                //总时间长度
                long duration = getDuration();
                //当前时间
                long currentPositionWhenPlaying = getCurrentPositionWhenPlaying();
                //快进（15S）
                long fastForwardProgress = currentPositionWhenPlaying + SharedPreferencesUtils.getUserSetSpeed() * 1000;
                if (duration > fastForwardProgress) mediaInterface.seekTo(fastForwardProgress);
                else mediaInterface.seekTo(duration);
                seekDanmu(currentPositionWhenPlaying);
                break;
            case R.id.quick_retreat:
                //当前时间
                long quickRetreatCurrentPositionWhenPlaying = getCurrentPositionWhenPlaying();
                //快退（15S）
                long quickRetreatProgress = quickRetreatCurrentPositionWhenPlaying - SharedPreferencesUtils.getUserSetSpeed() * 1000;
                if (quickRetreatProgress > 0) mediaInterface.seekTo(quickRetreatProgress);
                else mediaInterface.seekTo(0);
                seekDanmu(quickRetreatProgress);
                break;
            case R.id.tvSpeed:
                /*if (currentSpeedIndex == 7) currentSpeedIndex = 0;
                else currentSpeedIndex += 1;
                mediaInterface.setSpeed(getSpeedFromIndex(currentSpeedIndex));
                tvSpeed.setText(currentSpeedIndex == 1 ? "倍速" : "倍速X" + getSpeedFromIndex(currentSpeedIndex));*/
                showSpeedDialog();
                break;
            case R.id.airplay:
                if (!Utils.isWifi(context)) {
                    App.getInstance().showToastMsg(Utils.getString(R.string.castScreenNeedWlan), DialogXTipEnum.ERROR);
                    return;
                }
                if (isLocalVideo) {
                    // 本地投屏
                    if (localVideoDLNAServer != null) localVideoDLNAServer.stop();
                    localVideoDLNAServer = new LocalVideoDLNAServer(8080, localVideoPath);
                    try {
                        localVideoDLNAServer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Bundle bundle = new Bundle();
                String videoUrl = jzDataSource.getCurrentUrl().toString().replaceAll("\\\\", "");
                bundle.putString("playUrl", isLocalVideo ? Utils.getLocalIpAddress(context) : videoUrl);
                bundle.putLong("duration", getDuration());
                context.startActivity(new Intent(context, UpnpActivity.class).putExtras(bundle));
                break;
            case R.id.display:
                if (displayIndex == 4) displayIndex = 1;
                else displayIndex += 1;
                displayView.setText(getDisplayIndex(displayIndex));
                break;
            case R.id.danmu:
                if (danmakuView == null)
                    return;
                if (open_danmu) {
                    open_danmu = false;
                    // 关闭弹幕
                    danmuView.setImageDrawable(context.getDrawable(R.drawable.round_subtitles_off_24));
                    hideDanmmu();
                } else {
                    open_danmu = true;
                    // 打开弹幕
                    danmuView.setImageDrawable(context.getDrawable(R.drawable.round_subtitles_24));
                    showDanmmu();
                }
                break;
            /*case R.id.query_danmu:
                // 手动查询弹幕
                AlertDialog alertDialog;
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_query_danmu, null);
                TextInputLayout name = view.findViewById(R.id.name);
                TextInputLayout drama = view.findViewById(R.id.drama);
                name.getEditText().setText(queryDanmuTitle.isEmpty() ? (jzDataSource.title.contains("-") ? jzDataSource.title.split("-")[0].trim() : jzDataSource.title) : queryDanmuTitle);
//                drama.getEditText().setText(jzDataSource.title.contains("-") ? jzDataSource.title.split("-")[1].trim() : "");
                builder.setTitle("手动查询弹幕");
                builder.setPositiveButton("查询弹幕", null);
                builder.setNegativeButton("取消", null);
                alertDialog = builder.setView(view).create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v2 -> {
                    Utils.hideKeyboard(v2);
                    queryDanmuTitle = name.getEditText().getText().toString().trim();
                    String queryDanmuDrama = drama.getEditText().getText().toString().trim();
                    onQueryDanmuListener.queryDamu(queryDanmuTitle, queryDanmuDrama);
                    alertDialog.dismiss();
                });
                initTextInputLayout(alertDialog, name);
                initTextInputLayout(alertDialog, drama);
                break;*/
        }
    }

    private void initTextInputLayout(AlertDialog alertDialog, TextInputLayout textInputLayout) {
        textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (alertDialog == null) return;
                if (s.length() == 0) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void showSpeedDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
        builder.setTitle("倍速设置");
        builder.setSingleChoiceItems(speeds, currentSpeedIndex, (dialog, which) -> {
            currentSpeedIndex = which;
            mediaInterface.setSpeed(getSpeedFromIndex(currentSpeedIndex));
            tvSpeedView.setText(currentSpeedIndex == 1 ? Utils.getString(R.string.doubleSpeed) : Utils.getString(R.string.doubleSpeedX) + getSpeedFromIndex(currentSpeedIndex));
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private float getSpeedFromIndex(int index) {
        switch (index) {
            case 0:
                speedRet = 0.5f;
                break;
            case 1:
                speedRet = 1.0f;
                break;
            case 2:
                speedRet = 1.25f;
                break;
            case 3:
                speedRet = 1.5f;
                break;
            case 4:
                speedRet = 1.75f;
                break;
            case 5:
                speedRet = 2.0f;
                break;
            case 6:
                speedRet = 2.5f;
                break;
            case 7:
                speedRet = 3.0f;
                break;
        }
        return speedRet;
    }

    private String getDisplayIndex(int index) {
        switch (index) {
            case 1:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER);
                return Utils.getString(R.string.adaptiveScale);
            case 2:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT);
                return Utils.getString(R.string.stretchFullScreen);
            case 3:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP);
                return Utils.getString(R.string.cropFullScreen);
            case 4:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL);
                return Utils.getString(R.string.originalSize);
        }
        return null;
    }

    @Override
    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro, int posterImg, int bottomPro, int retryLayout) {
//        super.setAllControlsVisiblity(topCon, bottomCon, startBtn, loadingPro, posterImg, bottomPro, retryLayout);
        topContainer.setVisibility(topCon);
        bottomContainer.setVisibility(bottomCon);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
//        loadingView.setVisibility(loadingPro);
        posterImageView.setVisibility(posterImg);
        bottomProgressBar.setVisibility(bottomPro);
        mRetryLayout.setVisibility(retryLayout);
    }

    //这里是播放的时候点击屏幕出现的UI
    @Override
    public void changeUiToPlayingShow() {
        // 此处做锁屏功能的按钮显示，判断是否锁屏状态，并且需要注意当前屏幕状态
        if (!locked) {
            super.changeUiToPlayingShow();
            fastForward.setVisibility(VISIBLE);
            quickRetreat.setVisibility(VISIBLE);
            pipView.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? View.GONE : View.VISIBLE);
            configView.setVisibility(VISIBLE);
            airplayView.setVisibility(VISIBLE);
            fullscreenButton.setVisibility(GONE);
            showOrHideChangeViewListener.showOrHideChangeView();
        }
        if (screen == SCREEN_FULLSCREEN) {
            leftBLock.setVisibility(leftBLock.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            rightBlock.setVisibility(rightBlock.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    public void playingShow() {
        setAllControlsVisiblity(View.GONE, View.GONE, View.GONE,
                View.VISIBLE, View.GONE, View.GONE, View.GONE);
        leftBLock.setVisibility(GONE);
        rightBlock.setVisibility(GONE);
        fastForward.setVisibility(GONE);
        quickRetreat.setVisibility(GONE);
        pipView.setVisibility(GONE);
        configView.setVisibility(GONE);
        airplayView.setVisibility(GONE);
        preVideo.setVisibility(GONE);
        nextVideo.setVisibility(GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchListener.touch();
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                starX = event.getX();
                startY = event.getY();
                if (locked) {
                    return true;
                }
                break;
            // 用户滑动屏幕的操作，返回true来屏蔽音量、亮度、进度的滑动功能
            case MotionEvent.ACTION_MOVE:
                if (locked)
                    return true;
            case MotionEvent.ACTION_UP:
                if (locked) {
                    //&& Math.abs(Math.abs(event.getX() - starX)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()  && Math.abs(Math.abs(event.getY() - startY)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()
                    if (event.getX() == starX || event.getY() == startY) {
                        startDismissControlViewTimer();
                        onClickUiToggle();
                    }
                    return true;
                }
                break;
        }
        return super.onTouch(v, event);
    }

    @Override
    public void onClickUiToggle() {
        super.onClickUiToggle();
        if (isLongClick) {
            changeUiToPlayingClear();
            isLongClick = false;
        }
        if (!locked) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                leftBLock.setVisibility(View.VISIBLE);
                rightBlock.setVisibility(View.VISIBLE);
            } else {
                leftBLock.setVisibility(GONE);
                rightBlock.setVisibility(View.GONE);
            }
        } else {
            if ((int) leftBLock.getTag() == 1) {
                bottomProgressBar.setVisibility(GONE);
                leftBLock.setVisibility(View.VISIBLE);
                rightBlock.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void changeUiToPreparing() {
        super.changeUiToPreparing();
        leftBLock.setVisibility(View.INVISIBLE);
        rightBlock.setVisibility(INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        pipView.setVisibility(INVISIBLE);
        configView.setVisibility(INVISIBLE);
        airplayView.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
    }

    @Override
    public void onStatePreparingPlaying() {
        leftBLock.setVisibility(View.INVISIBLE);
        rightBlock.setVisibility(INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        pipView.setVisibility(INVISIBLE);
        configView.setVisibility(INVISIBLE);
        airplayView.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
        loadError = false;
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.resume();
        }
    }

    @Override
    public void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        if (locked) {
            bottomContainer.setVisibility(GONE);
            topContainer.setVisibility(GONE);
            startButton.setVisibility(GONE);
        }
    }

    //这里是播放的时候屏幕上面UI消失  只显示下面底部的进度条UI
    @Override
    public void changeUiToPlayingClear() {
        if (SharedPreferencesUtils.getUserSetHideProgress())
            setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                    View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);// 全屏播放时隐藏底部进度条
        else
            super.changeUiToPlayingClear();
        leftBLock.setVisibility(View.INVISIBLE);
        rightBlock.setVisibility(INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        pipView.setVisibility(INVISIBLE);
        configView.setVisibility(INVISIBLE);
        airplayView.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
    }

    // 点击暂停按钮执行的回调
    @Override
    public void onStatePause() {
        super.onStatePause();
        leftBLock.setVisibility(View.INVISIBLE);
        rightBlock.setVisibility(INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        pipView.setVisibility(INVISIBLE);
        configView.setVisibility(INVISIBLE);
        airplayView.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
        pauseListener.pause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    public void releaseDanMu() {
        if (danmakuView != null) danmakuView.release();
//        danmakuView = null;
    }

    @Override
    public void onStateError() {
        super.onStateError();
        loadError = true;
        if (danmakuView != null) danmakuView.release();
    }

    //这里是暂停的时候点击屏幕消失的UI,只显示下面底部的进度条UI
    @Override
    public void changeUiToPauseClear() {
        super.changeUiToPauseClear();
        leftBLock.setVisibility(View.INVISIBLE);
        rightBlock.setVisibility(INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        pipView.setVisibility(INVISIBLE);
        configView.setVisibility(INVISIBLE);
        airplayView.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
    }

    //这里是出错的UI
    @Override
    public void changeUiToError() {
        super.changeUiToError();
        leftBLock.setVisibility(View.INVISIBLE);
        rightBlock.setVisibility(INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        pipView.setVisibility(INVISIBLE);
        configView.setVisibility(INVISIBLE);
        airplayView.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
    }

    // 点击屏幕会出现所有控件，一定时间后消失的回调
    @Override
    public void dissmissControlView() {
//        super.dissmissControlView();
        if (state != STATE_NORMAL
                && state != STATE_ERROR
                && state != STATE_AUTO_COMPLETE) {
            post(() -> {
                bottomContainer.setVisibility(View.INVISIBLE);
                topContainer.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.INVISIBLE);

                leftBLock.setVisibility(View.INVISIBLE);
                rightBlock.setVisibility(INVISIBLE);
                fastForward.setVisibility(INVISIBLE);
                quickRetreat.setVisibility(INVISIBLE);
                pipView.setVisibility(INVISIBLE);
                configView.setVisibility(INVISIBLE);
                airplayView.setVisibility(INVISIBLE);
                preVideo.setVisibility(INVISIBLE);
                nextVideo.setVisibility(INVISIBLE);
                if (screen != SCREEN_TINY && !SharedPreferencesUtils.getUserSetHideProgress()) {
                    bottomProgressBar.setVisibility(View.VISIBLE);
                }
            });
        }
        // 需要在UI线程进行隐藏
        /*post(() -> {
            leftBLock.setVisibility(View.INVISIBLE);
            rightBlock.setVisibility(INVISIBLE);
            fastForward.setVisibility(INVISIBLE);
            quickRetreat.setVisibility(INVISIBLE);
            pipView.setVisibility(INVISIBLE);
            configView.setVisibility(INVISIBLE);
            airplayView.setVisibility(state == STATE_ERROR ? INVISIBLE : VISIBLE);
            if (SharedPreferencesUtils.getUserSetHideProgress())
                *//*bottomProgressBar.setVisibility(View.INVISIBLE);// 全屏播放时隐藏底部进度条*//*
            preVideo.setVisibility(INVISIBLE);
            nextVideo.setVisibility(INVISIBLE);
        });*/
    }

    public interface PlayingListener {
        void playing();
    }

    public interface PauseListener {
        void pause();
    }

    public interface CompleteListener {
        void complete();
    }

    public interface TouchListener {
        void touch();
    }

    public interface ShowOrHideChangeViewListener {
        void showOrHideChangeView();
    }

    public interface OnProgressListener {
        void getPosition(long position, long duration);
    }

    public interface OnQueryDanmuListener {
        void queryDamu(String queryDanmuTitle, String queryDanmuDrama);
    }

    public interface ActivityOrientationListener {
        void setOrientation(int type);
    }

    @Override
    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {
        super.setUp(jzDataSource, screen, mediaInterfaceClass);
        jzDataSource.headerMap = parserInterface.setPlayerHeaders();
        LogUtil.logInfo("setUp", JSONObject.toJSONString(jzDataSource.headerMap));
        batteryTimeLayout.setVisibility(GONE);
    }

    public void startPIP(){ changeUiToPlayingClear(); }

    @Override
    public void onCompletion() {
        super.onCompletion();
        listener.complete();
        if (danmakuView != null) {
            danmakuView.stop();
            danmakuView.clear();
            danmakuView.clearDanmakusOnScreen();
        }
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        playingListener.playing();
        loadError = false;
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.resume();
        }
    }

    @Override
    public void showWifiDialog() {
        Utils.showAlert(
                context,
                "",
                getResources().getString(R.string.tipsNotWifi),
                false,
                getResources().getString(R.string.tipsNotWifiConfirm),
                getResources().getString(R.string.tipsNotWifiCancel),
                "",
                (dialog, which) -> {
                    dialog.dismiss();
                    WIFI_TIP_DIALOG_SHOWED = true;
                    if (state == STATE_PAUSE)
                        startButton.performClick();
                    else
                        startVideo();
                },
                (dialog, which) -> {
                    dialog.dismiss();
                    releaseAllVideos();
                    ViewGroup vg = (ViewGroup) (JZUtils.scanForActivity(getContext())).getWindow().getDecorView();
                    vg.removeView(this);
                    if (mediaInterface != null) mediaInterface.release();
                    CURRENT_JZVD = null;
                },
                null
        );
    }

    @Override
    public void onSeekComplete() {
        super.onSeekComplete();
        seekDanmu(getCurrentPositionWhenPlaying());
    }

    public void seekDanmu(long time) {
        if (danmakuView != null) {
            danmakuView.clearDanmakusOnScreen();
            danmakuView.seekTo(time);
        }
    }

    @Override
    public void onProgress(int progress, long position, long duration) {
        super.onProgress(progress, position, duration);
        onProgressListener.getPosition(position, duration);
    }

    @Override
    public void reset() {
        super.reset();
        if (localVideoDLNAServer != null) localVideoDLNAServer.stop();
    }

    public void showDanmmu() {
        if (danmuView != null && openDamuConfig && hasDanmuConfig)
            danmakuView.show();
    }

    public void hideDanmmu() {
        if (danmuView != null && openDamuConfig && hasDanmuConfig)
            danmakuView.hide();
    }

    public void createDanmu() {
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                danmakuView.start();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                // 弹幕倍速设置
                timer.update(getCurrentPositionWhenPlaying());
            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuView.showFPS(BuildConfig.DEBUG);
        danmakuView.enableDanmakuDrawingCache(true);
    }

    @Override
    protected void touchActionMove(float x, float y) {
        Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
        float deltaX = x - mDownX;
        float deltaY = y - mDownY;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);
        if (screen == SCREEN_FULLSCREEN) {
            //拖动的是NavigationBar和状态栏
            if (mDownX > JZUtils.getScreenWidth(getContext()) || mDownY < JZUtils.getStatusBarHeight(getContext())) {
                return;
            }
            if (longPressing)
                return;
            if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                    cancelProgressTimer();
                    if (absDeltaX >= THRESHOLD) {
                        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                        // 否则会因为mediaplayer的状态非法导致App Crash
                        if (state != STATE_ERROR) {
                            mChangePosition = true;
                            mGestureDownPosition = getCurrentPositionWhenPlaying();
                        }
                    } else {
                        //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                        float halfLength = mScreenHeight * 0.5f;
                        if (Jzvd.FULLSCREEN_ORIENTATION == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                || Jzvd.FULLSCREEN_ORIENTATION == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                                || Jzvd.FULLSCREEN_ORIENTATION == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                                || Jzvd.FULLSCREEN_ORIENTATION == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT) {
                            halfLength = mScreenWidth * 0.5f;
                        }
                        if (mDownX < halfLength) {//左侧改变亮度
                            mChangeBrightness = true;
                            WindowManager.LayoutParams lp = JZUtils.getWindow(getContext()).getAttributes();
                            if (lp.screenBrightness < 0) {
                                try {
                                    mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                    Log.i(TAG, "current system brightness: " + mGestureDownBrightness);
                                } catch (Settings.SettingNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                mGestureDownBrightness = lp.screenBrightness * 255;
                                Log.i(TAG, "current activity brightness: " + mGestureDownBrightness);
                            }
                        } else {//右侧改变声音
                            mChangeVolume = true;
                            mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                            /*if (mAudioManager == null) {
                                mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                            }*/
                            mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        }
                    }
                }
            }
        }
        if (mChangePosition) {
            long totalTimeDuration = getDuration();
            if (PROGRESS_DRAG_RATE <= 0) {
                Log.d(TAG, "error PROGRESS_DRAG_RATE value");
                PROGRESS_DRAG_RATE = 1f;
            }
//            mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenWidth * PROGRESS_DRAG_RATE));
            mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenWidth * 4));
            if (mSeekTimePosition > totalTimeDuration)
                mSeekTimePosition = totalTimeDuration;
            String seekTime = JZUtils.stringForTime(mSeekTimePosition);
            String totalTime = JZUtils.stringForTime(totalTimeDuration);

            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        }
        if (mChangeVolume) {
            deltaY = -deltaY;
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
            //dialog中显示百分比
            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
            showVolumeDialog(-deltaY, volumePercent);
        }

        if (mChangeBrightness) {
            deltaY = -deltaY;
            int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
            WindowManager.LayoutParams params = JZUtils.getWindow(getContext()).getAttributes();
            if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                params.screenBrightness = 1;
            } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                params.screenBrightness = 0.01f;
            } else {
                params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
            }
            JZUtils.getWindow(getContext()).setAttributes(params);
            //dialog中显示百分比
            int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
            showBrightnessDialog(brightnessPercent);
//                        mDownY = y;
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        super.onVideoSizeChanged(width, height);
        if (height > 0 && height > 0) {
            if (height > width) {
                Log.e("PORTRAIT", "SCREEN_ORIENTATION_PORTRAIT");
                Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                activityOrientationListener.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                Log.e("LANDSCAPE", "SCREEN_ORIENTATION_SENSOR_LANDSCAPE");
                Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
                Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
                activityOrientationListener.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        }
    }
}
