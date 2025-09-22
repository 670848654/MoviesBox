package my.project.moviesbox.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import cn.jzvd.JZDataSource;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import my.project.moviesbox.R;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.config.JZMediaIjk;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.DetailsActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 视频预览弹窗封装
 * @date 2025/6/25 10:24
 */
public class VideoPreviewDialog extends BottomSheetDialog {
    private JzvdStd jzvdStd;


    public VideoPreviewDialog(@NonNull Context context, @NonNull String title, @NonNull String detailUrl, @NonNull String videoUrl, String videoPoster) {
        super(context);
        initView(context, title, detailUrl, videoUrl, videoPoster);
    }

    private void initView(Context context, String title, String detailUrl, String videoUrl, String videoPoster) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_video_preview, null);
        setContentView(view);
        TextView titleView = view.findViewById(R.id.title);
//        titleView.setText(title);
        TextViewDrawableHelper.setDrawableLeftWithText(
                context,
                titleView,
                R.drawable.round_movie_filter_24,
                R.color.pinka200,
                "[视频预览] " + title
        );
        Button detailBtn = view.findViewById(R.id.detail);
        detailBtn.setOnClickListener(v -> {
            Utils.setVibration(v);
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putString("url", detailUrl);
            context.startActivity(new Intent(context, DetailsActivity.class).putExtras(bundle));
        });
        jzvdStd = view.findViewById(R.id.jz_video);
        ImageView closeBtn = view.findViewById(R.id.btn_close);

        // 设置播放器静音播放
        JZDataSource jzDataSource = new JZDataSource(videoUrl, "");
        jzDataSource.headerMap = ParserInterfaceFactory.getParserInterface().setPreviewPlayerHeaders();
        jzvdStd.setUp(jzDataSource, Jzvd.SCREEN_NORMAL, JZMediaIjk.class);
        jzvdStd.posterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        loadImage(context, videoPoster, jzvdStd.posterImageView);
        jzvdStd.startVideo();
        Jzvd.SAVE_PROGRESS = false;

        // 屏蔽部分交互 UI
        disableAllControls(jzvdStd);

        // 关闭按钮
        closeBtn.setOnClickListener(v -> dismiss());

        setOnDismissListener(dialog -> Jzvd.releaseAllVideos());
    }

    private static void loadImage(Context context, String source, ImageView imageView) {
        if (source == null) return;

        if (source.startsWith("http")) {
            // 网络图片
            GlideApp.with(context).load(Utils.getGlideUrl(source)).into(imageView);
        } else if (source.startsWith("data:image")) {
            // Base64 图片
            String base64Data = source.substring(source.indexOf(",") + 1);
            byte[] decodedString = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(context).load(decodedByte).into(imageView);
        }
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