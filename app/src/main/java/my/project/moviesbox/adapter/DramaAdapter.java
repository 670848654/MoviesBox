package my.project.moviesbox.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.utils.DarkModeUtils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DramaAdapter
  * @描述: 影视播放列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:38
  * @版本: 1.0
 */
public class DramaAdapter extends BaseQuickAdapter<DetailsDataBean.DramasItem, BaseViewHolder> {
    private Context context;

    public DramaAdapter(Context context, @Nullable List<DetailsDataBean.DramasItem> data) {
        super(R.layout.item_desc_drama, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, DetailsDataBean.DramasItem item) {
        helper.setText(R.id.title, item.getTitle());
        if (item.isSelected())
            helper.setTextColor(R.id.title, context.getColor(R.color.pink200));
        else
            helper.setTextColor(R.id.title, DarkModeUtils.isDarkMode(context) ? context.getColor(R.color.night_text_color) : context.getColor(R.color.light_text_color ));
    }
}