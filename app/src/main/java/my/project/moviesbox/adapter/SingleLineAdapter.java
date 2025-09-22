package my.project.moviesbox.adapter;

import android.text.Html;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;

/**
 * @author Li
 * @version 1.0
 * @description: 单条数据适配器
 * @date 2025/8/1 15:00
 */
public class SingleLineAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public SingleLineAdapter(List<String> list) {
        super(R.layout.item_single_line, list);
    }

    @Override
    protected void convert(BaseViewHolder helper, String title) {
        helper.setText(R.id.title, Html.fromHtml(title));
    }
}