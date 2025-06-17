package my.project.moviesbox.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

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

            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResult(saveSuccess);
                }
            });
        });
    }

    /**
     * 保存图片到本地，支持网络 URL 和 Base64 编码
     * @param imageUrl 图片地址（URL 或 Base64）
     * @param savePath 保存路径
     * @return 是否保存成功
     */
    private static boolean saveImageToLocal(String imageUrl, String savePath) {
        if (Utils.isNullOrEmpty(imageUrl))
            return false;

        File file = new File(savePath);

        if (imageUrl.startsWith("data:image/")) {
            // Base64 编码处理
            try {
                // 截取 base64 数据
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);

                // 转 Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap == null) return false;

                // 写入文件
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                return true;
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            // 网络图片处理
            FutureTarget<Bitmap> futureTarget = Glide.with(Utils.getContext())
                    .asBitmap()
                    .load(Utils.getGlideUrl(imageUrl))
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

            try {
                Bitmap bitmap = futureTarget.get();

                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                return true;
            } catch (ExecutionException | InterruptedException | IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                Glide.with(Utils.getContext()).clear(futureTarget);
            }
        }
    }

    public interface SaveImageCallback {
        void onResult(boolean success);
    }
}