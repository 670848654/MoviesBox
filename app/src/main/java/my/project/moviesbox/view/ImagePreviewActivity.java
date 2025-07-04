package my.project.moviesbox.view;

import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.photoView.PhotoView;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 图片预览视图
 * @date 2025/6/26 16:39
 */
public class ImagePreviewActivity extends BaseActivity implements View.OnSystemUiVisibilityChangeListener {
    @BindView(R.id.photoView)
    PhotoView photoView;
    private String tempFilePath = null;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_image_preview;
    }

    @Override
    protected void init() {
        String imagePath = getIntent().getStringExtra("image_path");
        boolean isTempFile = getIntent().getBooleanExtra("is_temp_file", false);
        if (imagePath != null) {
            if (isTempFile) {
                tempFilePath = imagePath;
                GlideApp.with(this)
                        .load(new File(imagePath))
                        .into(photoView);
            } else {
                GlideApp.with(this)
                        .load(Utils.getGlideUrl(imagePath))
                        .into(photoView);
            }
        }
        // 点击图片关闭页面
        photoView.setOnClickListener(v -> finish());
    }

    @Override
    protected void initBeforeView() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
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
