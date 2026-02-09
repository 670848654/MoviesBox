package my.project.moviesbox.custom;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.flexbox.FlexboxLayoutManager;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: GridSpaceItemDecoration
  * @描述: RecyclerView item 间距均分
  * @来源: https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
  * @作者: Li Z
  * @日期: 2024/1/22 20:04
  * @版本: 1.0
 */
public class SmartGridSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int space;
    private final boolean includeEdge;
    private final BaseQuickAdapter<?, ?> adapter;

    public SmartGridSpacingDecoration(int space, boolean includeEdge, BaseQuickAdapter<?, ?> adapter) {
        this.space = space;
        this.includeEdge = includeEdge;
        this.adapter = adapter;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent,
                               RecyclerView.State state) {

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int position = parent.getChildAdapterPosition(view);

        if (layoutManager instanceof GridLayoutManager) {
            handleGrid(outRect, parent, position);
        }
        else if (layoutManager instanceof LinearLayoutManager) {
            handleLinear(outRect, (LinearLayoutManager) layoutManager, position);
        }
        else if (layoutManager instanceof FlexboxLayoutManager) {
            handleFlexbox(outRect);
        }
    }

    private void handleGrid(Rect outRect, RecyclerView parent, int position) {

        GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
        int spanCount = manager.getSpanCount();

        int headerCount = adapter.getHeaderLayoutCount();

        // 如果是 header
        if (position < headerCount) {
            if (includeEdge) {
                outRect.left = space;
                outRect.right = space;
                outRect.top = space;
            }
            return;
        }

        // 真实数据 position（去掉 header）
        int realPosition = position - headerCount;

        int column = realPosition % spanCount;

        if (includeEdge) {
            outRect.left = space - column * space / spanCount;
            outRect.right = (column + 1) * space / spanCount;

            if (realPosition < spanCount) {
                outRect.top = space;
            }

            outRect.bottom = space;

        } else {

            outRect.left = column * space / spanCount;
            outRect.right = space - (column + 1) * space / spanCount;

            if (realPosition >= spanCount) {
                outRect.top = space;
            }
        }
    }

    private void handleLinear(Rect outRect,
                              LinearLayoutManager manager,
                              int position) {

        if (manager.getOrientation() == RecyclerView.VERTICAL) {

            if (includeEdge && position == 0) {
                outRect.top = space;
            }

            outRect.bottom = space;

        } else {

            if (includeEdge && position == 0) {
                outRect.left = space;
            }

            outRect.right = space;
        }
    }

    private void handleFlexbox(Rect outRect) {
        // Flexbox 通常等距处理即可
        outRect.right = space;
        outRect.bottom = space;

        if (includeEdge) {
            outRect.left = space;
            outRect.top = space;
        }
    }
}
