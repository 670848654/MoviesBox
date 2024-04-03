package my.project.moviesbox.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: CustomTextView
  * @描述: 使TextView获得焦点展示文本跑马灯效果
  * @作者: Li Z
  * @日期: 2024/1/22 20:04
  * @版本: 1.0
 */
public class CustomTextView extends AppCompatTextView {
    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
