package my.project.moviesbox.custom;

import android.view.View;
import android.widget.TextView;

/**
 * @author Li
 * @version 1.0
 * @description: textview动画工具类
 * @date 2025/6/27 10:54
 */
public class TextViewAnimator {
    /**
     * 显示 TextView 带淡入淡出动画，并在1秒后自动隐藏
     *
     * @param textView 要显示的 TextView
     * @param text 显示的文本
     * @param fadeDuration 淡入/淡出动画时长（ms）
     * @param showDuration 显示文字停留时长（ms）
     */
    public static void showMultipleWithFade(TextView textView, String text, long fadeDuration, long showDuration) {
        textView.setAlpha(0f);
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);

        // 淡入动画
        textView.animate()
                .alpha(1f)
                .setDuration(fadeDuration)
                .withEndAction(() -> {
                    // 延迟一段时间后执行淡出
                    textView.postDelayed(() -> {
                        textView.animate()
                                .alpha(0f)
                                .setDuration(fadeDuration)
                                .withEndAction(() -> textView.setVisibility(View.GONE))
                                .start();
                    }, showDuration);
                })
                .start();
    }
}
