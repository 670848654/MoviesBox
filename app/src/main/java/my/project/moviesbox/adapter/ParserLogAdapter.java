package my.project.moviesbox.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.bean.ParserLogBean;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: SourceAdapter
  * @描述: 解析日志数据列表适配器
  * @作者: Li Z
  * @日期: 2025/2/24 15:23
  * @版本: 1.0
 */
public class ParserLogAdapter extends BaseQuickAdapter<ParserLogBean, BaseViewHolder> {

    public ParserLogAdapter(List<ParserLogBean> list) {
        super(R.layout.item_parser_log, list);
    }

    @Override
    protected void convert(BaseViewHolder helper, ParserLogBean item) {
        helper.setText(R.id.date_time, item.getDateTime());
        ExpandableTextView expandableTextView = helper.getView(R.id.content);
        expandableTextView.setContent(item.getContent());
        expandableTextView.setNeedExpend(true);
    }
}
