package my.project.moviesbox.adapter;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: TextListAdapter
  * @描述: 文本类数据列表适配器
  * @作者: Li Z
  * @日期: 2024/1/26 14:52
  * @版本: 1.0
 */
public class TextListAdapter extends BaseQuickAdapter<TextDataBean.Item, BaseViewHolder> {
    private Context context;

    public TextListAdapter(Context context, List<TextDataBean.Item> list) {
        super(R.layout.item_text, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, TextDataBean.Item item) {
        TextView indexView = helper.getView(R.id.index);
        helper.setText(R.id.index, item.getIndex());
        switch (item.getIndex()) {
            case "1":
                indexView.setBackground(context.getDrawable(R.drawable.rank_one));
                break;
            case "2":
                indexView.setBackground(context.getDrawable(R.drawable.rank_two));
                break;
            case "3":
                indexView.setBackground(context.getDrawable(R.drawable.rank_three));
                break;
            default:
                indexView.setBackground(context.getDrawable(R.drawable.rank_other));
                break;
        }
        TextView titleView = helper.getView(R.id.title);
        titleView.setText(item.getTitle());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleView.getLayoutParams();
        String content = "";
        if (!Utils.isNullOrEmpty(item.getEpisodes()) && !Utils.isNullOrEmpty(item.getContent()))
            content = item.getEpisodes() + " | " + item.getContent();
        else if (!Utils.isNullOrEmpty(item.getEpisodes()))
            content = item.getEpisodes();
        else if (!Utils.isNullOrEmpty(item.getContent()))
            content = item.getContent();
        if (Utils.isNullOrEmpty(content)) {
            // 设置垂直居中
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            titleView.setLayoutParams(params);
            helper.setGone(R.id.content, true);
        } else {
            // 移除垂直居中
            params.removeRule(RelativeLayout.CENTER_VERTICAL);
            titleView.setLayoutParams(params);
            helper.setText(R.id.content, item.getContent());
            helper.setVisible(R.id.content, true);
        }
    }
}