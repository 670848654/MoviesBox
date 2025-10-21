package my.project.moviesbox.config;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.io.File;
import java.util.HashMap;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;
import my.project.moviesbox.R;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.config
  * @类名: JZExoPlayer
  * @描述: ExoPlayer播放内核
  * @作者: Li Z
  * @日期: 2024/1/22 19:47
  * @版本: 1.0
 */
public class JZExoPlayer extends JZMediaInterface implements Player.EventListener, VideoListener {
    private SimpleExoPlayer simpleExoPlayer;
    private Runnable callback;
    private String TAG = "JZMediaExo";
    private long previousSeek = 0;

    public JZExoPlayer(Jzvd jzvd) {
        super(jzvd);
    }

    @Override
    public void start() {
        simpleExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void prepare() {
        Log.e(TAG, "prepare");
        Context context = jzvd.getContext();
        release();

        // 不再使用子线程
        mMediaHandlerThread = null;
        mMediaHandler = new Handler(Looper.getMainLooper());
        handler = new Handler(Looper.getMainLooper());
        // 直接执行
        initExoPlayerOnMainThread(context);
    }

    private void initExoPlayerOnMainThread(Context context) {
        DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(allocator)
                .setBufferDurationsMs(360000, 600000, 1000, 5000) // min/max buffer
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .setPrioritizeTimeOverSizeThresholds(false)
                .build();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();

        // 2. Create the player
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        simpleExoPlayer = new SimpleExoPlayer.Builder(context, renderersFactory)
                .setLoadControl(loadControl)
                .setBandwidthMeter(bandwidthMeter)
                .setLooper(Looper.getMainLooper()) // 强制使用主线程Looper
                .build();
        String currUrl = jzvd.jzDataSource.getCurrentUrl().toString();
        MediaSource videoSource;
        HashMap<String, String> headerMap = jzvd.jzDataSource.headerMap;
        DefaultHttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(Util.getUserAgent(context, context.getString(R.string.app_name)))
                        .setConnectTimeoutMs(15000)
                        .setReadTimeoutMs(15000)
                        .setAllowCrossProtocolRedirects(true)
                        .setDefaultRequestProperties(headerMap); // 替代 getDefaultRequestProperties()

        // 包装成 DefaultDataSourceFactory
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, null, httpDataSourceFactory);

        if (currUrl.contains(".m3u8")) {
            if (currUrl.startsWith("file")) // 播放本地M3U8
                videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.fromFile(new File(currUrl.replace("file:/", "")))));
            else // 播放网络M3U8
                videoSource = new HlsMediaSource.Factory(httpDataSourceFactory)
                        .setAllowChunklessPreparation(false)  // 禁止直接跳 segment
                        .createMediaSource(Uri.parse(currUrl));
        } else
            // 播放网络/本地视频
            videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(currUrl.startsWith("file") ? Uri.fromFile(new File(currUrl.replace("file:/", ""))) : Uri.parse(currUrl));
        simpleExoPlayer.addVideoListener(this);

        Log.e(TAG, "URL Link = " + currUrl);

        simpleExoPlayer.addListener(this);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build();
        simpleExoPlayer.setAudioAttributes(audioAttributes, false); // 👈 这句很关键！

        Boolean isLoop = jzvd.jzDataSource.looping;
        if (isLoop) {
            simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        } else {
            simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        }
        simpleExoPlayer.prepare(videoSource);
        simpleExoPlayer.setPlayWhenReady(true);
        callback = new onBufferingUpdate();

        if (jzvd.textureView != null) {
            SurfaceTexture surfaceTexture = jzvd.textureView.getSurfaceTexture();
            if (surfaceTexture != null) {
                simpleExoPlayer.setVideoSurface(new Surface(surfaceTexture));
            }
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        handler.post(() -> jzvd.onVideoSizeChanged(width, height));
    }

    @Override
    public void onRenderedFirstFrame() {
        Log.e(TAG, "onRenderedFirstFrame");
    }

    @Override
    public void pause() {
        simpleExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public boolean isPlaying() {
        return simpleExoPlayer.getPlayWhenReady();
    }

    @Override
    public void seekTo(long time) {
        if (Utils.isNullOrEmpty(simpleExoPlayer))
            return;
        if (time != previousSeek) {
            simpleExoPlayer.seekTo(time);
            previousSeek = time;
            jzvd.seekToInAdvance = time;
        }
    }

    @Override
    public void release() {
        if (mMediaHandler != null && simpleExoPlayer != null) {//不知道有没有妖孽
//            HandlerThread tmpHandlerThread = mMediaHandlerThread;
            ExoPlayer tmpMediaPlayer = simpleExoPlayer;
            JZMediaInterface.SAVED_SURFACE = null;

            mMediaHandler.post(() -> {
                tmpMediaPlayer.release();//release就不能放到主线程里，界面会卡顿
//                tmpHandlerThread.quit();
            });
            simpleExoPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        try {
            if (simpleExoPlayer != null)
                return simpleExoPlayer.getCurrentPosition();
            else return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long getDuration() {
        if (simpleExoPlayer != null)
            return simpleExoPlayer.getDuration();
        else return 0;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        simpleExoPlayer.setVolume(leftVolume);
        simpleExoPlayer.setVolume(rightVolume);
    }

    @Override
    public void setSpeed(float speed) {
        PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1.0F);
        simpleExoPlayer.setPlaybackParameters(playbackParameters);
    }

    /*@Override
    public void onTimelineChanged(final Timeline timeline, Object manifest, final int reason) {
        Log.e(TAG, "onTimelineChanged");
//        JZMediaPlayer.instance().mainThreadHandler.post(() -> {
//                if (reason == 0) {
//
//                    JzvdMgr.getCurrentJzvd().onInfo(reason, timeline.getPeriodCount());
//                }
//        });
    }*/

    /*@Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }*/

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.e(TAG, "onLoadingChanged");
    }

    @Override
    public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
        Log.e(TAG, "onPlayerStateChanged" + playbackState + "/ready=" + playWhenReady);
        handler.post(() -> {
            switch (playbackState) {
                case Player.STATE_IDLE: {
                }
                break;
                case Player.STATE_BUFFERING: {
                    handler.post(callback);
                }
                break;
                case Player.STATE_READY: {
                    if (playWhenReady) {
                        jzvd.state = Jzvd.STATE_PREPARED;
                        jzvd.onStatePlaying();
                    } else {
                    }
                }
                break;
                case Player.STATE_ENDED: {
                    jzvd.onCompletion();
                }
                break;
            }
        });
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG, "onPlayerError" + error.toString());
        handler.post(() -> jzvd.onError(1000, 1000));
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {
        handler.post(() -> jzvd.onSeekComplete());
    }

    @Override
    public void setSurface(Surface surface) {
        simpleExoPlayer.setVideoSurface(surface);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (SAVED_SURFACE == null) {
            SAVED_SURFACE = surface;
            prepare();
        } else {
            jzvd.textureView.setSurfaceTexture(SAVED_SURFACE);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class onBufferingUpdate implements Runnable {
        @Override
        public void run() {
            if (simpleExoPlayer != null) {
                final int percent = simpleExoPlayer.getBufferedPercentage();
                handler.post(() -> jzvd.setBufferProgress(percent));
                if (percent < 100) {
                    handler.postDelayed(callback, 300);
                } else {
                    handler.removeCallbacks(callback);
                }
            }
        }
    }
}