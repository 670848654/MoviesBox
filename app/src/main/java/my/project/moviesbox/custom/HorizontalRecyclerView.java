package my.project.moviesbox.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: HorizontalRecyclerView
  * @描述: 横向滑动触摸事件分发
  * @作者: Li Z
  * @日期: 2024/2/10 18:18
  * @版本: 1.0
 */
public class HorizontalRecyclerView extends RecyclerView {
    public HorizontalRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public HorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private float startX, startY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = e.getX();
                startY = e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = e.getX() - startX;
                float dy = e.getY() - startY;
                // 如果是水平滑动，则拦截事件，否则不拦截
                if (Math.abs(dx) > Math.abs(dy)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.onInterceptTouchEvent(e);
    }
}
