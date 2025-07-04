package my.project.moviesbox.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.bean.MenuBean;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DirectoryAdapter
  * @描述: 清单列表适配器
  * @作者: Li Z
  * @日期: 2024/11/11 9:24
  * @版本: 1.0
 */
public class MenuAdapter extends BaseQuickAdapter<MenuBean, BaseViewHolder> {
    private Context context;

    public MenuAdapter(Context context, List<MenuBean> list) {
        super(R.layout.item_menu, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, MenuBean item) {
        helper.setTextColor(R.id.title, item.isSelected() ? context.getColor(R.color.pinka200 ) : context.getColor(R.color.white));
        helper.setText(R.id.title, item.getTitle());
    }
}
