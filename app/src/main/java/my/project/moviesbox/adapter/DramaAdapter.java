package my.project.moviesbox.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jzvd.JZUtils;
import my.project.moviesbox.R;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DramaAdapter
  * @描述: 影视播放列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:38
  * @版本: 1.0
 */
public class DramaAdapter extends BaseQuickAdapter<DetailsDataBean.DramasItem, BaseViewHolder> implements LoadMoreModule {
    private Context context;
    /**
     * 是否是本地文件列表数据
     */
    private boolean isLocalFileList;
    // 缓存视频时长（单位：毫秒）
    private final Map<String, Long> durationCache = new ConcurrentHashMap<>();
    // LruCache 缓存视频帧截图
    private static final LruCache<String, Bitmap> frameCache;

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8; // 分配 1/8 内存
        frameCache = new LruCache<>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024; // KB
            }
        };
    }
    // 单线程池，避免同时过多任务
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DramaAdapter(Context context, boolean isLocalFileList, @Nullable List<DetailsDataBean.DramasItem> data) {
        super(isLocalFileList ? R.layout.item_local_file_item : R.layout.item_desc_drama, data);
        this.context = context;
        this.isLocalFileList = isLocalFileList;
    }

    @Override
    protected void convert(final BaseViewHolder helper, DetailsDataBean.DramasItem item) {
        if (isLocalFileList) {
            String defaultImg = item.getVideoImgUrl();
            String imgUrl = item.getUrl();
            ImageView imageView = helper.getView(R.id.img);
            imageView.setTag(R.id.imageid, imgUrl);
            helper.setText(R.id.title, item.getVideoTitle());
            String videoNumber = item.getTitle();
            helper.setText(R.id.number, videoNumber);
            // 先显示已有的时长（缓存或对象里）
            long videoDuration = getCachedDuration(item);
            helper.setText(R.id.time, JZUtils.stringForTime(videoDuration));
            // 异步更新时长
            if (videoDuration == 0)
                loadVideoDurationAsync(helper, item);
            helper.setText(R.id.file_size, item.getVideoFileSize() != 0 ? Utils.getNetFileSizeDescription(item.getVideoFileSize()) : "");
            helper.getView(R.id.img_box).setBackground(null);
            // 默认显示开头10秒的缩略图
            long duration = item.getVideoDuration(); // 单位：毫秒
            // 如果视频时长小于或等于 10 秒，用 1 秒位置
            long frameTimeMicros = (duration <= 10_000 && duration > 0) ? 1_000_000L : 10_000_000L;
            // 加载视频截图
            loadVideoScreenshot(context, imgUrl, defaultImg, imageView, frameTimeMicros);
//            Utils.loadVideoScreenshot(context, item.getUrl(), imgUrl, helper.getView(R.id.img), frameTimeMicros);
            if (item.isSelected()) {
                helper.setTextColor(R.id.title, context.getColor(R.color.pink200));
                helper.setTextColor(R.id.number, context.getColor(R.color.pink200));
                helper.setTextColor(R.id.file_size, context.getColor(R.color.pink200));
            } else {
                helper.setTextColor(R.id.title, context.getColor(R.color.white));
                helper.setTextColor(R.id.number, context.getColor(R.color.white));
                helper.setTextColor(R.id.file_size, context.getColor(R.color.white));
            }
        } else {
            helper.setText(R.id.title, item.getTitle());
            if (item.isSelected())
                helper.setTextColor(R.id.title, context.getColor(R.color.pink200));
            else
                helper.setTextColor(R.id.title, DarkModeUtils.isDarkMode(context) ? context.getColor(R.color.night_text_color) : context.getColor(R.color.light_text_color ));
        }
    }

    /**
     * 从缓存或对象中获取视频时长
     */
    private long getCachedDuration(DetailsDataBean.DramasItem item) {
        if (item.getVideoDuration() > 0)
            return item.getVideoDuration();
        return durationCache.getOrDefault(item.getUrl(), 0L);
    }

    /**
     * 异步加载视频时长
     */
    private void loadVideoDurationAsync(BaseViewHolder helper, DetailsDataBean.DramasItem item) {
        String url = item.getUrl();

        // 如果缓存里已经有了，直接返回
        if (durationCache.containsKey(url)) return;

        executor.execute(() -> {
            long duration = 0;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(url);
                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (durationStr != null) {
                    duration = Long.parseLong(durationStr);
                    item.setVideoDuration(duration);
                    durationCache.put(url, duration);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { retriever.release(); } catch (Exception ignored) {}
            }

            long finalDuration = duration;
            if (finalDuration > 0) {
                // 回到主线程更新
                new Handler(Looper.getMainLooper()).post(() -> {
                    helper.setText(R.id.time, JZUtils.stringForTime(finalDuration));
                });
            }
        });
    }

    /**
     * 带缓存的视频截图加载
     */
    private void loadVideoScreenshot(final Context context, String uri, String defaultImg, ImageView imageView, long frameTimeMicros) {
        // 如果缓存里有，直接显示
        Bitmap cachedBitmap = frameCache.get(uri);
        if (cachedBitmap != null) {
            if (uri.equals(imageView.getTag(R.id.imageid))) {
                imageView.setImageBitmap(cachedBitmap);
            }
            return;
        }

        // 获取 ImageView 实际宽度（未测量时延迟执行）
        imageView.post(() -> {
            int width = imageView.getWidth();
            if (width <= 0) width = 200; // 兜底
            int height = (int) (width * 16f / 9f);

            RequestOptions options = new RequestOptions()
                    .frame(frameTimeMicros)
                    .override(width, height)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.LOW);

            GlideApp.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(options)
                    .dontAnimate()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(
                                @NonNull Bitmap resource,
                                @Nullable Transition<? super Bitmap> transition
                        ) {
                            if (resource.getAllocationByteCount() > 100 && uri.equals(imageView.getTag(R.id.imageid))) {
                                frameCache.put(uri, resource);
                                imageView.setImageBitmap(resource);
                                Utils.setImageViewAnim(imageView);
                            } else {
                                onLoadFailed(null);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            imageView.setImageDrawable(null);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            if (!uri.equals(imageView.getTag(R.id.imageid))) return;
                            GlideApp.with(context)
                                    .load(defaultImg)
                                    .into(imageView);
                        }
                    });
        });
    }
}