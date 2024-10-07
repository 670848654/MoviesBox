package my.project.moviesbox.adapter;

import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DetailsListAdapter
  * @描述: 影视详情中的多季、推荐列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:33
  * @版本: 1.0
 */
public class DetailsListItemAdapter extends BaseQuickAdapter<DetailsDataBean.Recommend, BaseViewHolder> {

    public DetailsListItemAdapter(@LayoutRes int layoutId, @Nullable List<DetailsDataBean.Recommend> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, DetailsDataBean.Recommend item) {
        String imgUrl = item.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        Utils.setDefaultImage(item.getImg(), item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        helper.setText(R.id.title, item.getTitle());
    }
}