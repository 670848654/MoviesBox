package my.project.moviesbox.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.bean.SourceBean;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: SourceAdapter
  * @描述: 开源相关数据列表适配器
  * @作者: Li Z
  * @日期: 2024/1/26 14:53
  * @版本: 1.0
 */
public class SourceAdapter extends BaseQuickAdapter<SourceBean, BaseViewHolder> {

    public SourceAdapter(List<SourceBean> list) {
        super(R.layout.item_source, list);
    }

    @Override
    protected void convert(BaseViewHolder helper, SourceBean item) {
        helper.setText(R.id.title, item.getTitle());
        helper.setText(R.id.author, item.getAuthor());
        helper.setText(R.id.desc, item.getDesc());
    }
}
