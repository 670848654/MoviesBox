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
    private final OnItemClick onItemClick;

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
                // 是否多选
                boolean multipleChoices = classificationDataBean.isMultipleChoices();
                List<ClassificationDataBean.Item> items = classificationDataBean.getItemList();
                helper.setText(R.id.title, classificationDataBean.getClassificationTitle().replaceAll("：", "").replaceAll(":", ""));
                recyclerView.setLayoutManager(new AutoLineFeedLayoutManager());
                ClassificationItemAdapter classificationItemAdapter = new ClassificationItemAdapter(items);
                classificationItemAdapter.setOnItemClickListener((adapter, view, position) -> {
                    // 如果当前为选中状态
                    boolean selected = items.get(position).isSelected();
                    String title = items.get(position).getTitle();
                    String url = items.get(position).getUrl();
                    StringBuilder titleSb = new StringBuilder();
                    StringBuilder urlSb = new StringBuilder();
                    if (multipleChoices) {
                        // 多选
                        if (position == 0) {
                            // 如果选择的是“全部”，清除其他项的选择，并将“全部”设为选中
                            for (ClassificationDataBean.Item ci : items) {
                                if (ci.isSelected()) {
                                    ci.setSelected(false);
                                    adapter.notifyItemChanged(items.indexOf(ci));
                                }
                            }
                            items.get(0).setSelected(true);
                            adapter.notifyItemChanged(0);
                        } else {
                            // 如果选择的不是“全部”
                            items.get(0).setSelected(false); // 取消“全部”的选择
                            adapter.notifyItemChanged(0);
                            // 更新当前项的选择状态
                            items.get(position).setSelected(!selected);
                            adapter.notifyItemChanged(position);
                            // 检查是否没有任何项被选中，如果是则选择“全部”
                            if (!checkHasSelected(items)) {
                                items.get(0).setSelected(true);
                                adapter.notifyItemChanged(0);
                            }

                            // 获取选中的数据
                            for (ClassificationDataBean.Item ci : items) {
                                if (ci.isSelected()) {
                                    titleSb.append(ci.getTitle()).append(",");
                                    urlSb.append(ci.getUrl()).append(",");
                                }
                            }

                            // 删除最后一个逗号
                            if (titleSb.length() > 0) {
                                titleSb.deleteCharAt(titleSb.length() - 1);
                            }
                            if (urlSb.length() > 0) {
                                urlSb.deleteCharAt(urlSb.length() - 1);
                            }
                        }
                    } else {
                        // 单选
                        // 先将该组的分类点击样式清除
                        for (ClassificationDataBean.Item ci : items) {
                            if (ci.isSelected()) {
                                ci.setSelected(false);
                                adapter.notifyItemChanged(items.indexOf(ci));
                            }
                        }
                        if (position == 0) {
                            items.get(0).setSelected(true);
                            adapter.notifyItemChanged(0);
                        } else {
                            items.get(0).setSelected(false); // 取消“全部”的选择
                            adapter.notifyItemChanged(0);
                            // 更新当前项的选择状态
                            items.get(position).setSelected(!selected);
                            adapter.notifyItemChanged(position);
                            if (!selected) {
                                // 更新选中的标题和URL
                                titleSb.append(title);
                                urlSb.append(url);
                            }

                            // 检查是否没有任何项被选中，如果是则选择“全部”
                            if (!checkHasSelected(items)) {
                                items.get(0).setSelected(true);
                                adapter.notifyItemChanged(0);
                            }
                        }
                    }
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    onItemClick.onChipClick(classificationDataBean.getIndex(), titleSb.toString(), urlSb.toString());
                });
                recyclerView.setPadding(0,0,0, 10);
                recyclerView.setAdapter(classificationItemAdapter);
                break;
        }
    }

    private boolean checkHasSelected(List<ClassificationDataBean.Item> items) {
        boolean hasSelected = false;
        for (ClassificationDataBean.Item ci : items) {
            if (ci.isSelected()) {
                hasSelected = true;
                break;
            }
        }
      return hasSelected;
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
