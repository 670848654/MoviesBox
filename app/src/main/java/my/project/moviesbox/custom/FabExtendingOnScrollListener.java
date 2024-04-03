package my.project.moviesbox.custom;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: FabExtendingOnScrollListener
  * @描述: 实现RecyclerView滑动隐藏/显示ExtendedFloatingActionButton
  * @作者: Li Z
  * @日期: 2024/1/22 20:04
  * @版本: 1.0
 */
public class FabExtendingOnScrollListener extends RecyclerView.OnScrollListener {

    private final ExtendedFloatingActionButton floatingActionButton;

    public FabExtendingOnScrollListener(ExtendedFloatingActionButton floatingActionButton) {
        this.floatingActionButton = floatingActionButton;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE
                && !floatingActionButton.isExtended()
                && recyclerView.computeVerticalScrollOffset() == 0) {
            floatingActionButton.extend();
        }
        super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy != 0 && floatingActionButton.isExtended()) {
            floatingActionButton.shrink();
        }
        super.onScrolled(recyclerView, dx, dy);
    }
}