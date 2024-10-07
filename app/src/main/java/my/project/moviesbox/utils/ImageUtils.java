package my.project.moviesbox.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/8/14 16:14
 */
public class ImageUtils {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void saveImageToLocalAsync(String imageUrl, String localImgPath, SaveImageCallback callback) {
        executorService.submit(() -> {
            boolean saveSuccess = saveImageToLocal(imageUrl, localImgPath);

            // 使用 Handler 回到主线程
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResult(saveSuccess);
                }
            });
        });
    }

    /**
     * 保存图片到本地
     * @param imageUrl
     * @param savePath
     */
    private static boolean saveImageToLocal(String imageUrl, String savePath) {
        if (Utils.isNullOrEmpty(imageUrl))
            return false;
        File file = new File(savePath);
        // 使用 Glide 加载图片
        FutureTarget<Bitmap> futureTarget = Glide.with(Utils.getContext())
                .asBitmap()
                .load(Utils.getGlideUrl(imageUrl))
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        try {
            // 获取 Bitmap 对象
            Bitmap bitmap = futureTarget.get();

            // 将 Bitmap 写入文件
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            // 清除 Glide 缓存
            Glide.with(Utils.getContext()).clear(futureTarget);
        }
    }
    public interface SaveImageCallback {
        void onResult(boolean success);
    }
}
