package my.project.moviesbox.custom;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: AutoLineFeedLayoutManager
  * @描述: RecyclerView 数据自动根据屏幕宽度自动换行实现类
  * @作者: Li Z
  * @日期: 2024/1/22 20:02
  * @版本: 1.0
  * @deprecated 改为 {@link com.google.android.flexbox.FlexboxLayoutManager}
 */
@Deprecated
public class AutoLineFeedLayoutManager extends RecyclerView.LayoutManager {

    public AutoLineFeedLayoutManager() {
        setAutoMeasureEnabled(true);//layoutmanager必须调用此方法设为true才能在onMesure时自动布局
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        int curLineWidth = 0, curLineTop = 0;//curLineWidth 累加item布局时的x轴偏移curLineTop 累加item布局时的x轴偏移
        int lastLineMaxHeight = 0;
        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            //获取每个item的布局参数，计算每个item的占用位置时需要加上margin
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            int height = getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;

            curLineWidth += width;//累加当前行已有item的宽度
            if (curLineWidth <= getWidth()) {//如果累加的宽度小于等于RecyclerView的宽度，不需要换行
                layoutDecorated(view, curLineWidth - width + params.leftMargin, curLineTop + params.topMargin, curLineWidth - params.rightMargin, curLineTop + height - params.bottomMargin);//布局item的真实位置
                //比较当前行多有item的最大高度，用于换行后计算item在y轴上的偏移量
                lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
            } else {//换行
                curLineWidth = width;
                if (lastLineMaxHeight == 0) {
                    lastLineMaxHeight = height;
                }
                //记录当前行top
                curLineTop += lastLineMaxHeight;

                layoutDecorated(view, params.leftMargin, curLineTop + params.topMargin, width - params.rightMargin, curLineTop + height - params.bottomMargin);
                lastLineMaxHeight = height;
            }
        }
    }
}