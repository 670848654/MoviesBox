package my.project.moviesbox.config;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;
import my.project.moviesbox.utils.Utils;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
  * @包名: my.project.moviesbox.config
  * @类名: JZMediaIjk
  * @描述: IjkPlayer播放内核
  * @作者: Li Z
  * @日期: 2024/1/22 19:48
  * @版本: 1.0
 */
public class JZMediaIjk extends JZMediaInterface implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {
    IjkMediaPlayer ijkMediaPlayer;

    public JZMediaIjk(Jzvd jzvd) {
        super(jzvd);
    }

    @Override
    public void start() {
        if (ijkMediaPlayer != null) ijkMediaPlayer.start();
    }

    @Override
    public void prepare() {

        release();
        mMediaHandlerThread = new HandlerThread("JZVD");
        mMediaHandlerThread.start();
        mMediaHandler = new Handler(mMediaHandlerThread.getLooper());//主线程还是非主线程，就在这里
        handler = new Handler();

        mMediaHandler.post(() -> {

            ijkMediaPlayer = new IjkMediaPlayer();

            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            ////1为硬解 0为软解
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            //使用opensles把文件从java层拷贝到native层
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            //视频格式
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            //跳帧处理（-1~120）。CPU处理慢时，进行跳帧处理，保证音视频同步
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            //0为一进入就播放,1为进入时不播放
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
            ////域名检测
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
            //最大缓冲大小,单位kb
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 4024 * 1024);
            //某些视频在SeekTo的时候，会跳回到拖动前的位置，这是因为视频的关键帧的问题，通俗一点就是FFMPEG不兼容，视频压缩过于厉害，seek只支持关键帧，出现这个情况就是原始的视频文件中i 帧比较少
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
            //是否重连
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
            //http重定向https
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
            //设置seekTo能够快速seek到指定位置并播放
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
            //播放前的探测Size，默认是1M, 改小一点会出画面更快
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10);
            //1变速变调状态 0变速不变调状态
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "file,http,https,tcp,tls,crypto");

            ijkMediaPlayer.setOnPreparedListener(JZMediaIjk.this);
            ijkMediaPlayer.setOnVideoSizeChangedListener(JZMediaIjk.this);
            ijkMediaPlayer.setOnCompletionListener(JZMediaIjk.this);
            ijkMediaPlayer.setOnErrorListener(JZMediaIjk.this);
            ijkMediaPlayer.setOnInfoListener(JZMediaIjk.this);
            ijkMediaPlayer.setOnBufferingUpdateListener(JZMediaIjk.this);
            ijkMediaPlayer.setOnSeekCompleteListener(JZMediaIjk.this);
            ijkMediaPlayer.setOnTimedTextListener(JZMediaIjk.this);

            try {
                ijkMediaPlayer.setDataSource(jzvd.jzDataSource.getCurrentUrl().toString());
                HashMap<String, String> hashMap = jzvd.jzDataSource.headerMap;
                if (hashMap != null) {
                    for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, key, value);
                    }
                }

                ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                ijkMediaPlayer.setScreenOnWhilePlaying(true);
                ijkMediaPlayer.prepareAsync();

                ijkMediaPlayer.setSurface(new Surface(jzvd.textureView.getSurfaceTexture()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void pause() {
        ijkMediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        return ijkMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        if (!Utils.isNullOrEmpty(ijkMediaPlayer))
            ijkMediaPlayer.seekTo(time);
    }

    @Override
    public void release() {
        if (mMediaHandler != null && mMediaHandlerThread != null && ijkMediaPlayer != null) {//不知道有没有妖孽
            HandlerThread tmpHandlerThread = mMediaHandlerThread;
            IjkMediaPlayer tmpMediaPlayer = ijkMediaPlayer;
            JZMediaInterface.SAVED_SURFACE = null;

            mMediaHandler.post(() -> {
                tmpMediaPlayer.setSurface(null);
                tmpMediaPlayer.release();
                tmpHandlerThread.quit();
            });
            ijkMediaPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        return ijkMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (ijkMediaPlayer == null) return 0;
        return ijkMediaPlayer.getDuration();
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        ijkMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void setSpeed(float speed) {
        ijkMediaPlayer.setSpeed(speed);
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        handler.post(() -> jzvd.onPrepared());
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        handler.post(() -> jzvd.onVideoSizeChanged(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight()));
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        handler.post(() -> jzvd.onError(what, extra));
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        handler.post(() -> jzvd.onInfo(what, extra));
        return false;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, final int percent) {
        handler.post(() -> jzvd.setBufferProgress(percent));
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        handler.post(() -> jzvd.onSeekComplete());
    }

    @Override
    public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {

    }

    @Override
    public void setSurface(Surface surface) {
        ijkMediaPlayer.setSurface(surface);
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

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        handler.post(() -> jzvd.onCompletion());
    }
}