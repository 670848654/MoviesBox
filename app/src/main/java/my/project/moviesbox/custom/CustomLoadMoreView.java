package my.project.moviesbox.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.loadmore.BaseLoadMoreView;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import my.project.moviesbox.R;

/**
  * @包名: my.project.moviesbox.custom
  * @类名: CustomLoadMoreView
  * @描述: 自定义loadMore视图
  * @作者: Li Z
  * @日期: 2024/1/22 20:03
  * @版本: 1.0
 */
public class CustomLoadMoreView extends BaseLoadMoreView {
    @Override
    public View getRootView(ViewGroup parent) {
        return  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_load_more_view, parent, false);
    }

    @Override
    public View getLoadingView(BaseViewHolder holder) {
        return holder.getView(R.id.load_more_loading_view);
    }

    @Override
    public View getLoadComplete(BaseViewHolder holder) {
        return holder.getView(R.id.load_more_load_end_view);
    }

    @Override
    public View getLoadEndView(BaseViewHolder holder) {
        return holder.getView(R.id.load_more_load_end_view);
    }

    @Override
    public View getLoadFailView(BaseViewHolder holder) {
        return holder.getView(R.id.load_more_load_fail_view);
    }
}
