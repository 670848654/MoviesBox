package my.project.moviesbox.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

/**
 * @author Li
 * @version 1.0
 * @description: 生成首文字Drawable
 * @date 2025/9/9 16:57
 */
public class TextDrawableUtil {

    public static Drawable createTextDrawable(Context context, String name, int sizeDp) {
        String text = "";
        if (name != null && name.length() > 0) {
            text = name.substring(0, 1); // 取第一个字
        }

        int sizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeDp, context.getResources().getDisplayMetrics());

        // 创建位图
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.parseColor("#607D8B")); // 灰蓝色背景
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bgPaint);

        // 文字
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(sizePx * 0.5f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float baseline = sizePx / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f;

        canvas.drawText(text, sizePx / 2f, baseline, textPaint);

        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
