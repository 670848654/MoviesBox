package my.project.moviesbox.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DirectoryAdapter
  * @描述: 清单列表适配器
  * @作者: Li Z
  * @日期: 2024/11/11 9:24
  * @版本: 1.0
 */
public class DirectoryAdapter extends BaseQuickAdapter<TDirectory, BaseViewHolder> {
    /**
     * 是否设置背景
     */
    private final boolean setBackground;
    public DirectoryAdapter(boolean setBackground, List<TDirectory> list) {
        super(R.layout.item_directory, list);
        this.setBackground = setBackground;
    }

    public boolean isEven(int position) {
        return position % 2 == 0;
    }

    @Override
    protected void convert(BaseViewHolder helper, TDirectory item) {
        Context context = Utils.getContext();
        helper.setGone(R.id.edit, !item.isShowConfigBtn());
        helper.setGone(R.id.delete, !item.isShowConfigBtn());
        helper.setText(R.id.title, item.getName());
        if (setBackground)
            helper.getView(R.id.root).setBackground(context.getDrawable(isEven(helper.getBindingAdapterPosition()) ? R.drawable.tag_bg_style_night : R.drawable.tag_bg_style_light));
    }
}
