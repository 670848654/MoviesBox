package my.project.moviesbox.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/8/11 11:27
 */
public class TextViewDrawableHelper {
    /**
     * 给 TextView 左边加一个垂直居中的 Drawable 图标（带颜色）
     * @param context Context
     * @param textView 目标 TextView
     * @param drawableResId Drawable 资源 id
     * @param colorResId 颜色资源 id
     * @param text 文本内容
     */
    public static void setDrawableLeftWithText(@NonNull Context context, @NonNull TextView textView,
                                               int drawableResId, int colorResId, @NonNull String text) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable == null) {
            textView.setText(text);
            return;
        }
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorResId));
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        SpannableString spannable = new SpannableString("  " + text); // 留两个空格放图标
        CenteredImageSpan imageSpan = new CenteredImageSpan(drawable);
        spannable.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
    }


    /**
     * 垂直居中的 ImageSpan 实现
     */
    public static class CenteredImageSpan extends ImageSpan {
        public CenteredImageSpan(Drawable drawable) {
            super(drawable);
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom,
                         @NonNull Paint paint) {

            Drawable drawable = getDrawable();
            canvas.save();

            int drawableHeight = drawable.getIntrinsicHeight();
            int fontHeight = paint.getFontMetricsInt().bottom - paint.getFontMetricsInt().top;
            int centerY = y + paint.getFontMetricsInt().descent - fontHeight / 2;

            int transY = centerY - drawableHeight / 2;

            canvas.translate(x, transY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }
}
