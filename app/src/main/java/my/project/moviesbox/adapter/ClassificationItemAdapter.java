package my.project.moviesbox.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.chip.Chip;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.ClassificationDataBean;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: ClassificationItemAdapter
  * @描述: 分类列表CHIP组适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:32
  * @版本: 1.0
 */
public class ClassificationItemAdapter extends BaseQuickAdapter<ClassificationDataBean.Item, BaseViewHolder> {
    public ClassificationItemAdapter(List<ClassificationDataBean.Item> data) {
        super(R.layout.item_classification_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ClassificationDataBean.Item item) {
        Chip chip = helper.getView(R.id.chip);
        chip.setChecked(item.isSelected());
        chip.setText(item.getTitle());
        chip.setEnsureMinTouchTargetSize(false);
    }
}
