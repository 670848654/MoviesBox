package my.project.moviesbox.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.PositionInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.UpnpDevicesAdapter;
import my.project.moviesbox.cling.Intents;
import my.project.moviesbox.cling.control.ClingPlayControl;
import my.project.moviesbox.cling.control.callback.ControlCallback;
import my.project.moviesbox.cling.control.callback.ControlReceiveCallback;
import my.project.moviesbox.cling.entity.ClingDevice;
import my.project.moviesbox.cling.entity.ClingDeviceList;
import my.project.moviesbox.cling.entity.DLANPlayState;
import my.project.moviesbox.cling.entity.IDevice;
import my.project.moviesbox.cling.entity.IResponse;
import my.project.moviesbox.cling.listener.BrowseRegistryListener;
import my.project.moviesbox.cling.listener.DeviceListChangedListener;
import my.project.moviesbox.cling.service.ClingUpnpService;
import my.project.moviesbox.cling.service.manager.ClingManager;
import my.project.moviesbox.cling.service.manager.DeviceManager;
import my.project.moviesbox.cling.util.OtherUtils;
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
public class UpnpActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    /** 连接设备状态: 播放状态 */
    public static final int PLAY_ACTION = 1;
    /** 连接设备状态: 暂停状态 */
    public static final int PAUSE_ACTION = 2;
    /** 连接设备状态: 停止状态 */
    public static final int STOP_ACTION = 3;
    /** 连接设备状态: 转菊花状态 */
    public static final int TRANSITIONING_ACTION = 4;
    /** 投放失败 */
    public static final int ERROR_ACTION = 5;
    /** 获取进度 */
    public static final int GET_POSITION_INFO_ACTION = 6;

    private Context mContext;
    private Handler mHandler = new InnerHandler();
    @BindView(R.id.video_url)
    TextView videoUrlView;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private UpnpDevicesAdapter adapter;
    private List<ClingDevice> clingDevices = new ArrayList<>();
    @BindView(R.id.tv_selected)
    TextView mTVSelected;
    @BindView(R.id.seekbar_progress)
    SeekBar mSeekProgress;
    @BindView(R.id.seekbar_volume)
    SeekBar mSeekVolume;
    @BindView(R.id.duration)
    TextView durationText;
    private String refTimeText = "%s/%s";

    private BroadcastReceiver mTransportStateBroadcastReceiver;
    /**
     * 投屏控制器
     */
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();

    /** 用于监听发现设备 */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LogUtil.logInfo("mUpnpServiceConnection", "onServiceConnected");
            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();

            ClingManager clingUpnpServiceManager = ClingManager.getInstance();
            clingUpnpServiceManager.setUpnpService(beyondUpnpService);
            clingUpnpServiceManager.setDeviceManager(new DeviceManager());

            clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
            //Search on service created.
            clingUpnpServiceManager.searchDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            LogUtil.logInfo("mUpnpServiceConnection", "onServiceDisconnected");
            ClingManager.getInstance().setUpnpService(null);
            runOnUiThread(() -> {
                mTVSelected.setText(getString(R.string.upnpNotice));
                postHandler.post(positionRunnable);
                postHandler.removeCallbacksAndMessages(null);
                mSeekProgress.setProgress(0);
                durationText.setText(String.format(refTimeText, "00:00:00", OtherUtils.getStringTime((int) duration)));
                mSeekProgress.setEnabled(false);
                mSeekVolume.setEnabled(false);
            });
        }
    };
    private String playUrl; // 视频播放地址
    private long duration; // 视屏长度

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
        mContext = this;
        playUrl = getIntent().getExtras().getString("playUrl");
        duration = getIntent().getExtras().getLong("duration");
        initView();
        initListeners();
        bindServices();
        registerReceivers();
        hideNavBar();
    }

    @Override
    protected void initBeforeView() {

    }

    private void registerReceivers() {
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            registerReceiver(mTransportStateBroadcastReceiver, filter, RECEIVER_EXPORTED);
        else
            registerReceiver(mTransportStateBroadcastReceiver, filter);
    }


    private void bindServices() {
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(UpnpActivity.this, ClingUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        // Unbind UPnP service
        unbindService(mUpnpServiceConnection);
        // UnRegister Receiver
        unregisterReceiver(mTransportStateBroadcastReceiver);
        postHandler.removeCallbacksAndMessages(null);
        ClingManager.getInstance().destroy();
        ClingDeviceList.getInstance().destroy();
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected void retryListener() {

    }

    private void initView() {
        adapter = new UpnpDevicesAdapter(clingDevices);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            ClingDevice item = clingDevices.get(position);
            if (OtherUtils.isNull(item)) {
                return;
            }
            ClingManager.getInstance().setSelectedDevice(item);
            Device device = item.getDevice();
            if (OtherUtils.isNull(device)) {
                return;
            }
            String selectedDeviceName = String.format(getString(R.string.upnpDeviceSelected), device.getDetails().getFriendlyName());
            mTVSelected.setText(selectedDeviceName);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 片源的时间
        mSeekProgress.setMax((int) duration);
        durationText.setText(String.format(refTimeText, "00:00:00", OtherUtils.getStringTime((int) duration)));
        // 设置最大音量
        mSeekVolume.setMax(100);
        mSeekProgress.setEnabled(false);
        mSeekVolume.setEnabled(false);
    }

    private void initListeners() {
        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                runOnUiThread(() -> {
                    ClingDevice clingDevice = (ClingDevice) device;
                    if (!clingDevices.contains(clingDevice)) {
                        clingDevices.add(clingDevice);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                runOnUiThread(() -> {
                    ClingDevice clingDevice = (ClingDevice) device;
                    if (clingDevices.contains(clingDevice)) {
                        clingDevices.remove(clingDevice);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        mSeekProgress.setOnSeekBarChangeListener(this);
        mSeekVolume.setOnSeekBarChangeListener(this);
    }

    @OnClick({R.id.play, R.id.pause, R.id.stop, R.id.exit})
    public void onClick(View view) {
        if (!Utils.isFastClick()) return;
        switch (view.getId()) {
            case R.id.play:
                int currentState = mClingPlayControl.getCurrentState();
                /**
                 * 通过判断状态 来决定 是继续播放 还是重新播放
                 */
                if (currentState == DLANPlayState.STOP) {
                    mClingPlayControl.playNew(playUrl, new ControlCallback() {
                        @Override
                        public void success(IResponse response) {
                            LogUtil.logInfo("play success", null);
                            ClingManager.getInstance().registerAVTransport(mContext);
                            ClingManager.getInstance().registerRenderingControl(mContext);
                            mHandler.sendEmptyMessage(PLAY_ACTION);
                        }

                        @Override
                        public void fail(IResponse response) {
                            LogUtil.logInfo("play fail", null);
                            mHandler.sendEmptyMessage(ERROR_ACTION);
                        }
                    });
                } else {
                    mClingPlayControl.play(new ControlCallback() {
                        @Override
                        public void success(IResponse response) {
                            LogUtil.logInfo("play success", null);
                        }

                        @Override
                        public void fail(IResponse response) {
                            LogUtil.logInfo("play fail", null);
                            mHandler.sendEmptyMessage(ERROR_ACTION);
                        }
                    });
                }
                break;
            case R.id.pause:
                mClingPlayControl.pause(new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        LogUtil.logInfo("pause success", null);
                        mHandler.sendEmptyMessage(PLAY_ACTION);
                    }

                    @Override
                    public void fail(IResponse response) {
                        LogUtil.logInfo("pause fail", null);
                    }
                });
                break;
            case R.id.stop:
                mClingPlayControl.stop(new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        LogUtil.logInfo("stop success", null);
                        mHandler.sendEmptyMessage(STOP_ACTION);
                    }

                    @Override
                    public void fail(IResponse response) {
                        LogUtil.logInfo("stop fail", null);
                    }
                });
                break;
            case R.id.exit:
                finish();
                break;
        }
    }

    /******************* start progress changed listener *************************/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.seekbar_progress: // 进度
                int currentProgress = seekBar.getProgress(); // 转为毫秒
                mClingPlayControl.seek(currentProgress, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        durationText.setText(String.format(refTimeText, OtherUtils.getStringTime(currentProgress), OtherUtils.getStringTime((int) duration)));
                        LogUtil.logInfo("seek success", null);
                    }

                    @Override
                    public void fail(IResponse response) {
                        LogUtil.logInfo("seek fail", null);
                    }
                });
                break;
            case R.id.seekbar_volume:   // 音量
                int currentVolume = seekBar.getProgress();
                mClingPlayControl.setVolume(currentVolume, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        LogUtil.logInfo("volume success", null);
                    }

                    @Override
                    public void fail(IResponse response) {
                        LogUtil.logInfo("volume fail", null);
                    }
                });
                break;
        }
    }

    /******************* end progress changed listener *************************/
    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    handlePlayAction();
                    break;
                case PAUSE_ACTION:
                    handlePauseAction();
                    break;
                case STOP_ACTION:
                    handleStopAction();
                    break;
                case TRANSITIONING_ACTION:
                    LogUtil.logInfo("Execute TRANSITIONING_ACTION", null);
                    application.showToastMsg("正在连接");
                    break;
                case ERROR_ACTION:
                    handleErrorAction();
                    break;
            }
        }

        private void handlePlayAction() {
            LogUtil.logInfo("Execute PLAY_ACTION", null);
            mSeekProgress.setEnabled(true);
            mSeekVolume.setEnabled(true);
            mClingPlayControl.setCurrentState(DLANPlayState.PLAY);
            postHandler.post(positionRunnable);
        }

        private void handlePauseAction() {
            LogUtil.logInfo("Execute PAUSE_ACTION", null);
            mClingPlayControl.setCurrentState(DLANPlayState.PAUSE);

            postHandler.post(positionRunnable);
            postHandler.removeCallbacksAndMessages(null);
            mSeekProgress.setEnabled(false);
            mSeekVolume.setEnabled(false);
        }

        private void handleStopAction() {
            LogUtil.logInfo("Execute STOP_ACTION", null);
            mClingPlayControl.setCurrentState(DLANPlayState.STOP);

            postHandler.post(positionRunnable);
            postHandler.removeCallbacksAndMessages(null);

            mSeekProgress.setProgress(0);
            durationText.setText(String.format(refTimeText, "00:00:00", OtherUtils.getStringTime((int) duration)));
            mSeekProgress.setEnabled(false);
            mSeekVolume.setEnabled(false);
        }

        private void handleErrorAction() {
            LogUtil.logInfo("Execute ERROR_ACTION", null);
            application.showToastMsg("投放失败");
            postHandler.post(positionRunnable);
            postHandler.removeCallbacksAndMessages(null);
            mSeekProgress.setProgress(0);
            durationText.setText(String.format(refTimeText, "00:00:00", OtherUtils.getStringTime((int) duration)));
            mSeekProgress.setEnabled(false);
            mSeekVolume.setEnabled(false);
        }
    }

    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.logInfo("Receive playback intent:", action);
            switch (action) {
                case Intents.ACTION_PLAYING:
                    mHandler.sendEmptyMessage(PLAY_ACTION);
                    break;
                case Intents.ACTION_PAUSED_PLAYBACK:
                    mHandler.sendEmptyMessage(PAUSE_ACTION);
                    break;
                case Intents.ACTION_STOPPED:
                    mHandler.sendEmptyMessage(STOP_ACTION);
                    break;
                case Intents.ACTION_TRANSITIONING:
                    mHandler.sendEmptyMessage(TRANSITIONING_ACTION);
                    break;
            }
        }
    }
    Handler postHandler = new Handler();
    private int refreshPositionTime = 1000; // 刷新时长
    private Runnable positionRunnable = new Runnable() {
        @Override
        public void run() {
            mClingPlayControl.getPositionInfo(new ControlReceiveCallback() {
                @Override
                public void receive(IResponse response) {
                    if (response != null) {
                        runOnUiThread(() -> {
                            PositionInfo positionInfo = (PositionInfo) response.getResponse();
                            if (OtherUtils.getIntTime(positionInfo.getRelTime()) == 0) return;
                            mSeekProgress.setProgress(OtherUtils.getIntTime(positionInfo.getRelTime()));
                            durationText.setText(String.format(refTimeText, positionInfo.getRelTime(), positionInfo.getTrackDuration()));
                        });
                    }
                }

                @Override
                public void success(IResponse response) {

                }

                @Override
                public void fail(IResponse response) {

                }
            });
            postHandler.postDelayed(this, refreshPositionTime);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
        postHandler.post(positionRunnable);

    }

    @Override
    protected void onPause() {
        super.onPause();
        postHandler.removeCallbacksAndMessages(null);
    }
}