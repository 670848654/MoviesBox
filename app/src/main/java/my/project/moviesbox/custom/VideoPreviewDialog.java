package my.project.moviesbox.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import my.project.moviesbox.R;
import my.project.moviesbox.config.JZMediaIjk;

/**
 * @author Li
 * @version 1.0
 * @description: 视频预览弹窗封装
 * @date 2025/6/25 10:24
 */
public class VideoPreviewDialog extends BottomSheetDialog {
    private JzvdStd jzvdStd;


    public VideoPreviewDialog(@NonNull Context context, @NonNull String title, @NonNull String videoUrl) {
        super(context);
        initView(context, title, videoUrl);
    }

    private void initView(Context context, String title, String videoUrl) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_video_preview, null);
        setContentView(view);
        TextView titleView = view.findViewById(R.id.title);
        titleView.setText(title);
        jzvdStd = view.findViewById(R.id.jz_video);
        ImageView closeBtn = view.findViewById(R.id.btn_close);

        // 设置播放器静音播放
        jzvdStd.setUp(videoUrl, "", Jzvd.SCREEN_NORMAL);
        jzvdStd.setMediaInterface(JZMediaIjk.class);
        jzvdStd.startVideo();
        Jzvd.SAVE_PROGRESS = false;

        // 屏蔽部分交互 UI
        disableAllControls(jzvdStd);

        // 关闭按钮
        closeBtn.setOnClickListener(v -> dismiss());

        setOnDismissListener(dialog -> Jzvd.releaseAllVideos());
    }

    private void disableAllControls(JzvdStd player) {
        Jzvd.WIFI_TIP_DIALOG_SHOWED = true;
        player.titleTextView.setVisibility(View.GONE);
        player.backButton.setVisibility(View.GONE);
        player.batteryTimeLayout.setVisibility(View.GONE);
        player.fullscreenButton.setVisibility(View.GONE);
//        player.bottomContainer.setVisibility(View.GONE);
//        player.currentTimeTextView.setVisibility(View.GONE);
//        player.totalTimeTextView.setVisibility(View.GONE);
        player.topContainer.setVisibility(View.GONE);
        player.startButton.setVisibility(View.GONE);
//        player.progressBar.setVisibility(View.GONE);
//        player.bottomProgressBar.setVisibility(View.GONE);
        // 禁止点击播放区域
        player.setOnTouchListener((v, event) -> true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        View view = getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (view != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(view);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Jzvd.releaseAllVideos(); // 释放资源
    }
}