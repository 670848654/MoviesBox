package my.project.moviesbox.view;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.databinding.ActivityImagePreviewBinding;
import my.project.moviesbox.photoView.PhotoView;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 图片预览视图
 * @date 2025/6/26 16:39
 */
public class ImagePreviewActivity extends BaseActivity<ActivityImagePreviewBinding> implements View.OnSystemUiVisibilityChangeListener {
    private PhotoView photoView;
    private String tempFilePath = null;

    @Override
    protected void initBeforeView() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityImagePreviewBinding inflateBinding(LayoutInflater inflater) {
        return ActivityImagePreviewBinding.inflate(inflater);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        photoView = binding.photoView;
    }

    @Override
    public void initClickListeners() {

    }

    @Override
    protected void init() {
        String imagePath = getIntent().getStringExtra("image_path");
        boolean isTempFile = getIntent().getBooleanExtra("is_temp_file", false);
        if (imagePath != null) {
            GlideApp.with(this)
                    .load(isTempFile ? new File(imagePath) : Utils.getGlideUrl(imagePath))
                    .dontAnimate() // 防止Glide动画和共享元素动画冲突
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, @androidx.annotation.Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }
                    })
                    .into(photoView);
            // 延迟共享元素动画
            supportPostponeEnterTransition();
        }
        // 点击图片关闭页面
        photoView.setOnClickListener(v -> finishAfterTransition());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        new Handler().postDelayed(() ->
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION), 500);

    }

    @Override
    protected void setConfigurationChanged() {

    }

    /**
     * 点击重试抽象方法
     *
     * @return
     */
    @Override
    protected void retryListener() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tempFilePath != null) {
            File file = new File(tempFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
