package my.project.moviesbox.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.VipVideoDataBean;
import my.project.moviesbox.utils.DarkModeUtils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/21 16:48
 */
public class VipVideoAdapter extends BaseQuickAdapter<VipVideoDataBean.DramasItem, BaseViewHolder> {
    private Context context;

    public VipVideoAdapter(Context context, @Nullable List<VipVideoDataBean.DramasItem> data) {
        super(R.layout.item_desc_drama, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, VipVideoDataBean.DramasItem item) {
        helper.setText(R.id.title, item.getTitle());
        if (item.isSelected())
            helper.setTextColor(R.id.title, context.getColor(R.color.pink200));
        else
            helper.setTextColor(R.id.title, DarkModeUtils.isDarkMode(context) ? context.getColor(R.color.night_text_color) : context.getColor(R.color.light_text_color));
    }
}