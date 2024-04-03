package my.project.moviesbox.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.bean.SettingAboutBean;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/27 9:33
 */
public class SettingAboutAdapter extends BaseQuickAdapter<SettingAboutBean, BaseViewHolder> {
    private Context context;
    public SettingAboutAdapter(Context context, List<SettingAboutBean> data) {
        super(R.layout.item_setting_about, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, SettingAboutBean item) {
        helper.setImageDrawable(R.id.icon, context.getDrawable(item.getIcon()));
        helper.setText(R.id.title, item.getTitle());
        if (Utils.isNullOrEmpty(item.getSubTitle()))
            helper.setGone(R.id.subTitle, true);
        else {
            helper.setText(R.id.subTitle, item.getSubTitle());
            helper.setGone(R.id.subTitle, false);
        }
        if (item.getEndIcon() != 0) {
            helper.setImageDrawable(R.id.endIcon, context.getDrawable(item.getEndIcon()));
            helper.setVisible(R.id.endIcon, true);
        } else
            helper.setGone(R.id.endIcon, true);
        helper.setGone(R.id.progress, true);
    }
}