package my.project.moviesbox.view;

import static org.fourthline.cling.support.model.TransportState.NO_MEDIA_PRESENT;
import static org.fourthline.cling.support.model.TransportState.PAUSED_PLAYBACK;
import static org.fourthline.cling.support.model.TransportState.PLAYING;
import static org.fourthline.cling.support.model.TransportState.STOPPED;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cast.dlna.dmc.DLNACastManager;
import com.android.cast.dlna.dmc.OnDeviceRegistryListener;
import com.android.cast.dlna.dmc.control.DeviceControl;
import com.android.cast.dlna.dmc.control.OnDeviceControlListener;
import com.android.cast.dlna.dmc.control.ServiceActionCallback;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportState;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import kotlin.Unit;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.UpnpDevicesAdapter;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: UpnpActivity
  * @描述: 投屏操作视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:09
  * @版本: 1.0
 */
public class UpnpActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener  {
    private final static int LOCAL_PORT = 8899;
    private final static String LOCAL_ADDRESS = "http://127.0.0.1:%s%s";
    @BindView(R.id.video_url)
    TextView videoUrlView;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private UpnpDevicesAdapter adapter;
    private List<Device> clingDevices = new ArrayList<>();
    private Device currentDevice = null;
    @BindView(R.id.pause)
    Button pauseBtn;
    @BindView(R.id.tv_selected)
    TextView mTVSelected;
    @BindView(R.id.seekbar_progress)
    SeekBar mSeekProgress;
    @BindView(R.id.seekbar_volume)
    SeekBar mSeekVolume;
    @BindView(R.id.duration)
    TextView durationText;
    private boolean isLocalVideo;
    private String playUrl; // 视频播放地址
    private long duration; // 视屏长度
    private DeviceControl deviceControl;
    private TransportState currentState = NO_MEDIA_PRESENT;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_upnp;
    }

    @Override
    protected void init() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.dimAmount = 0.6f;
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = (int) Math.round(outMetrics.widthPixels / 1.2);
        attributes.height = (int) Math.round(outMetrics.heightPixels / 1.2);
        getWindow().setAttributes(attributes);
        playUrl = getIntent().getExtras().getString("playUrl");
        duration = getIntent().getExtras().getLong("duration");
        isLocalVideo = playUrl.startsWith("/storage/");
        initView();
        hideNavBar();
    }

    @Override
    protected void initBeforeView() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentDevice != null)
            DLNACastManager.INSTANCE.disconnectDevice(currentDevice);
        if (positionHandler != null)
            positionHandler.stop();
        DLNACastManager.INSTANCE.unbindCastService(this);
        if (isLocalVideo)
            DLNACastManager.INSTANCE.stopLocalHttpServer();
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected void retryListener() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        DLNACastManager.INSTANCE.bindCastService(this);
    }

    private void initView() {
        mSeekProgress.setMax((int) duration);
        durationText.setText("--:--/--:--");
        // 设置最大音量
        mSeekVolume.setMax(100);
        mSeekProgress.setEnabled(false);
        mSeekVolume.setEnabled(false);
        mSeekProgress.setOnSeekBarChangeListener(this);
        mSeekVolume.setOnSeekBarChangeListener(this);
        adapter = new UpnpDevicesAdapter(clingDevices);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Device item = clingDevices.get(position);
            if (Utils.isNullOrEmpty(item)) {
                return;
            }
            if (currentDevice != null)
                DLNACastManager.INSTANCE.disconnectDevice(currentDevice);
            currentDevice = item;
            String selectedDeviceName = String.format(getString(R.string.upnpDeviceSelected), item.getDetails().getFriendlyName());
            deviceControl = DLNACastManager.INSTANCE.connectDevice(item, new OnDeviceControlListener() {
                @Override
                public void onConnected(@NonNull Device<?, ?, ?> device) {
                    application.showToastMsg("连接成功", DialogXTipEnum.SUCCESS);
                }

                @Override
                public void onDisconnected(@NonNull Device<?, ?, ?> device) {
//                    application.showToastMsg("连接失败", DialogXTipEnum.ERROR);
                }

                @Override
                public void onEventChanged(@NonNull EventedValue<?> eventedValue) {

                }

                @Override
                public void onAvTransportStateChanged(@NonNull TransportState transportState) {

                }

                @Override
                public void onRendererVolumeChanged(int i) {

                }

                @Override
                public void onRendererVolumeMuteChanged(boolean b) {

                }
            });
            mTVSelected.setText(selectedDeviceName);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DLNACastManager.INSTANCE.registerDeviceListener(new OnDeviceRegistryListener() {
            @Override
            public void onDeviceAdded(@NonNull Device<?, ?, ?> device) {
                runOnUiThread(() -> {
                    if (!clingDevices.contains(device)) {
                        clingDevices.add(device);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onDeviceRemoved(@NonNull Device<?, ?, ?> device) {
                runOnUiThread(() -> {
                    if (clingDevices.contains(device)) {
                        clingDevices.remove(device);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @OnClick({R.id.play, R.id.pause, R.id.stop, R.id.exit})
    public void onClick(View view) {
        if (!Utils.isFastClick()) return;
        Utils.setVibration(view);
        switch (view.getId()) {
            case R.id.play:
                if (deviceControl == null) return;
                deviceControl.getVolume(new ServiceActionCallback<>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        if (integer != null)
                            mSeekVolume.setProgress(integer);
                    }

                    @Override
                    public void onFailure(@NonNull String s) {

                    }
                });
                if (isLocalVideo) {
                    File file = new File(playUrl);
                    DLNACastManager.INSTANCE.startLocalHttpServer(LOCAL_PORT, true);
                    playUrl = String.format(LOCAL_ADDRESS, LOCAL_PORT, file.getAbsolutePath());
                }
                deviceControl.setAVTransportURI(playUrl, "haha", new ServiceActionCallback<>() {
                    @Override
                    public void onSuccess(Unit unit) {
                        LogUtil.logInfo("play success", null);
                        positionHandler.start(0);
                        mSeekProgress.setEnabled(true);
                        mSeekVolume.setEnabled(true);
                        currentState = PLAYING;
                    }

                    @Override
                    public void onFailure(@NonNull String s) {
                        LogUtil.logInfo("play fail", s);
                        application.showToastMsg("无法播放", DialogXTipEnum.ERROR);
                        mSeekProgress.setEnabled(false);
                        mSeekVolume.setEnabled(false);
                        currentState = NO_MEDIA_PRESENT;
                    }
                });
                break;
            case R.id.pause:
                if (deviceControl == null) return;
                if (currentState == PLAYING) {
                    deviceControl.pause(new ServiceActionCallback<Unit>() {
                        @Override
                        public void onSuccess(Unit unit) {
                            positionHandler.stop();
                            currentState = PAUSED_PLAYBACK;
                            pauseBtn.setText("继续播放");
                        }

                        @Override
                        public void onFailure(@NonNull String s) {

                        }
                    });
                } else {
                    deviceControl.play("1", new ServiceActionCallback<Unit>() {
                        @Override
                        public void onSuccess(Unit unit) {
                            positionHandler.start(0);
                            currentState = PLAYING;
                            pauseBtn.setText("暂停");
                        }

                        @Override
                        public void onFailure(@NonNull String s) {

                        }
                    });
                }
                break;
            case R.id.stop:
                if (deviceControl == null) return;
                deviceControl.stop(new ServiceActionCallback<Unit>() {
                    @Override
                    public void onSuccess(Unit unit) {
                        if (isFinishing()) return;
                        if (isLocalVideo)
                            DLNACastManager.INSTANCE.stopLocalHttpServer();
                        positionHandler.stop();
                        application.showToastMsg("已停止投屏", DialogXTipEnum.ERROR);
                        currentState = STOPPED;
                        pauseBtn.setText("暂停");
                        durationText.setText("--:--/--:--");
                        mSeekProgress.setProgress(0);
                        mSeekProgress.setEnabled(false);
                        mSeekVolume.setProgress(0);
                        mSeekVolume.setEnabled(false);
                    }

                    @Override
                    public void onFailure(@NonNull String s) {
                    }
                });
                break;
            case R.id.exit:
                finish();
                break;
        }
    }

    private CircleMessageHandler positionHandler = new CircleMessageHandler(1000, () -> deviceControl.getPositionInfo(new ServiceActionCallback<>() {
        @Override
        public void onSuccess(PositionInfo result) {
            if (isFinishing()) return;
            if (duration == 0L) {
                duration = result.getTrackDurationSeconds() * 1000;
            }
            durationText.setText(String.format("%s/%s", getStringTime(result.getTrackElapsedSeconds() * 1000), getStringTime(duration)));
            Long progress = result.getTrackElapsedSeconds() * 1000;
            mSeekProgress.setProgress(progress.intValue());
        }

        @Override
        public void onFailure(String msg) {
            if (isFinishing()) return;
            pauseBtn.setText("暂停");
            durationText.setText("--:--/--:--");
        }
    }));

    private String getStringTime(long timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.US);
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = totalSeconds / 60 % 60;
        long hours = totalSeconds / 3600;

        if (hours == 0) {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.seekbar_progress: // 进度
                int currentProgress = seekBar.getProgress(); // 转为毫秒
                deviceControl.seek(currentProgress, new ServiceActionCallback<>() {
                    @Override
                    public void onSuccess(Unit unit) {
                        mSeekProgress.setProgress(currentProgress);
                    }

                    @Override
                    public void onFailure(@NonNull String s) {

                    }
                });
                break;
            case R.id.seekbar_volume:   // 音量
                int currentVolume = seekBar.getProgress();
                deviceControl.setVolume(currentVolume, new ServiceActionCallback<>() {
                    @Override
                    public void onSuccess(Unit unit) {
                        mSeekVolume.setProgress(currentVolume);
                    }

                    @Override
                    public void onFailure(@NonNull String s) {

                    }
                });
                break;
        }
    }

    private class CircleMessageHandler extends Handler {
        private static final int MSG = 101;
        private final long duration;
        private final Runnable runnable;

        public CircleMessageHandler(long duration, Runnable runnable) {
            super(Looper.getMainLooper());
            this.duration = duration;
            this.runnable = runnable;
        }

        @Override
        public void handleMessage(Message msg) {
            runnable.run();
            sendEmptyMessageDelayed(MSG, duration);
        }

        public void start(long delay) {
            stop();
            sendEmptyMessageDelayed(MSG, delay);
        }

        public void stop() {
            removeMessages(MSG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}