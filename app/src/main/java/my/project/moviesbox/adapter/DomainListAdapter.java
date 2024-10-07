package my.project.moviesbox.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.DomainDataBean;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: SourceAdapter
  * @描述: 开源相关数据列表适配器
  * @作者: Li Z
  * @日期: 2024/1/26 14:53
  * @版本: 1.0
 */
public class DomainListAdapter extends BaseQuickAdapter<DomainDataBean.Domain, BaseViewHolder> {

    public DomainListAdapter(List<DomainDataBean.Domain> list) {
        super(R.layout.item_domain, list);
    }

    @Override
    protected void convert(BaseViewHolder helper, DomainDataBean.Domain item) {
        helper.setText(R.id.title, item.getTitle());
    }
}
