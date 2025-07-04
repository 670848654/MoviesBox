package my.project.moviesbox.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/6/28 19:30
 */
public class ImageCache {
    public static class ImagePathResult {
        public final String path;
        public final boolean isTempFile;

        public ImagePathResult(String path, boolean isTempFile) {
            this.path = path;
            this.isTempFile = isTempFile;
        }
    }

    public static ImagePathResult prepareImagePath(Context context, String input, String fileName) {
        if (TextUtils.isEmpty(input)) return null;

        if (input.startsWith("http://") || input.startsWith("https://")) {
            return new ImagePathResult(input, false);
        }

        try {
            String base64Data = input.contains(",") ? input.substring(input.indexOf(",") + 1) : input;
            byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
            File file = new File(context.getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodedBytes);
            fos.flush();
            fos.close();
            return new ImagePathResult(file.getAbsolutePath(), true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
