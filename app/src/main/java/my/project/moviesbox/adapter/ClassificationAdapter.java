package my.project.moviesbox.adapter;

import android.view.HapticFeedbackConstants;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.custom.AutoLineFeedLayoutManager;
import my.project.moviesbox.parser.bean.ClassificationDataBean;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: ClassificationAdapter
  * @描述: 分类列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:31
  * @版本: 1.0
 */
public class ClassificationAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private OnItemClick onItemClick;
    private ClassificationItemAdapter classificationItemAdapter;
    public ClassificationAdapter(List<MultiItemEntity> data, OnItemClick onItemClick) {
        super(data);
        this.onItemClick = onItemClick;
        addItemType(ClassificationDataBean.ITEM_TYPE, R.layout.item_classification);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case ClassificationDataBean.ITEM_TYPE:
                RecyclerView recyclerView = helper.getView(R.id.rv_list);
                ClassificationDataBean classificationDataBean = (ClassificationDataBean) item;
                List<ClassificationDataBean.Item> items = classificationDataBean.getItemList();
                helper.setText(R.id.title, classificationDataBean.getClassificationTitle());
                recyclerView.setLayoutManager(new AutoLineFeedLayoutManager());
                classificationItemAdapter = new ClassificationItemAdapter(items);
                classificationItemAdapter.setOnItemClickListener((adapter, view, position) -> {
                    // 先将该组的分类点击样式清除
                    for (ClassificationDataBean.Item ci : items) {
                        ci.setSelected(false);
                    }
                    // 设置当前点击的chip为选中
                    items.get(position).setSelected(true);
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    adapter.notifyDataSetChanged();
                    onItemClick.onChipClick(classificationDataBean.getIndex(), items.get(position).getTitle(), items.get(position).getUrl());
                });
                recyclerView.setPadding(0,0,0, 10);
                recyclerView.setAdapter(classificationItemAdapter);
                break;
        }
    }

    /**
      * @包名: my.project.moviesbox.adapter
      * @类名: ClassificationAdapter
      * @描述: 分类CHIP点击事件接口
      * @作者: Li Z
      * @日期: 2024/1/22 14:24
      * @版本: 1.0
     */
    public interface OnItemClick {
        /**
         * 分类CHIP点击实现
         * @param index 当前分类所属组下标
         * @param title 分类标题
         * @param url 分类的URL
         */
        void onChipClick(int index, String title, String url);
    }
}
