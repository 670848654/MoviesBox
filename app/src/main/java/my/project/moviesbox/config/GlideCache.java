package my.project.moviesbox.config;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/29 8:54
 */
@GlideModule
public class GlideCache extends AppGlideModule {
    private String appRootPath = null;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //手机app路径
        appRootPath = context.getFilesDir().getPath();
        // 100 MB
        int diskCacheSizeBytes = 1024 * 1024 * 100;
        builder.setDiskCache(new DiskLruCacheFactory(appRootPath + "/GlideDisk", diskCacheSizeBytes));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient client = GlideUnsafeOkHttpClient.getUnsafeOkHttpClient();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}