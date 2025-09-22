package my.project.moviesbox.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

/**
 * @author Li
 * @version 1.0
 * @description: 自动轮播
 * @date 2025/9/20 14:13
 */
public class BannerAutoPlayHelper {

    private int position = 0;
    private Handler handler;
    private Runnable runnable;
    private int interval = 5000;
    private boolean isRunning = false;
    private boolean isTouched = false; // 标记用户是否触摸
    private boolean isAutoScroll = false; // 标记当前滚动是自动还是手动
    private RecyclerView recyclerView;
    private SnapHelper snapHelper;

    public BannerAutoPlayHelper(RecyclerView recyclerView, SnapHelper snapHelper) {
        this.recyclerView = recyclerView;
        this.snapHelper = snapHelper;
        handler = new Handler(Looper.getMainLooper());

        runnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || recyclerView.getAdapter() == null || isTouched) return;

                int itemCount = recyclerView.getAdapter().getItemCount();
                int nextPos = position + 1;
                if (nextPos >= itemCount) nextPos = 0;

                position = nextPos;
                isAutoScroll = true; // 标记这是自动滚动
                recyclerView.smoothScrollToPosition(position);

                handler.postDelayed(this, interval);
            }
        };

        // 生命周期绑定
        recyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                start();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                stop();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isTouched = true;       // 用户手动滑动
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isAutoScroll) {
                        // 只有手动滑动才同步 position
                        View centerView = snapHelper.findSnapView(recyclerView.getLayoutManager());
                        if (centerView != null) {
                            position = recyclerView.getLayoutManager().getPosition(centerView);
                        }
                    }
                    isTouched = false;
                    isAutoScroll = false;  // 自动滚动完成
                }
            }
        });
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.postDelayed(runnable, interval);
        }
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(runnable);
    }
}