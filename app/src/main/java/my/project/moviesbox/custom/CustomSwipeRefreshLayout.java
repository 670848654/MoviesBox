package my.project.moviesbox.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: CustomSwipeRefreshLayout
  * @描述: 解决SwipeRefreshLayout与NestedScrollView滑动冲突
  * @作者: Li Z
  * @日期: 2024/1/26 14:57
  * @版本: 1.0
 */
public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    private float startX;
    private float startY;
    private float mTouchSlop;

    public CustomSwipeRefreshLayout(Context context) {
        super(context);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = Math.abs(ev.getX() - startX);
                float distanceY = Math.abs(ev.getY() - startY);
                if(distanceX > mTouchSlop && distanceX > distanceY){  //判断为横向滑动
                    return false;
                }

                break;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
