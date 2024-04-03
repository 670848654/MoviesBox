package my.project.moviesbox.adapter;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: WeekAdapter
  * @描述: 星期时间表数据ITEM适配器 [一般用于动漫站点使用，简单的参数设置]
  * @作者: Li Z
  * @日期: 2024/1/22 17:27
  * @版本: 1.0
 */
public class WeekAdapter extends BaseQuickAdapter<WeekDataBean.WeekItem, BaseViewHolder> {
    private Context context;
    @LayoutRes
    private int layout;

    public WeekAdapter(Context context, @LayoutRes int layout, List<WeekDataBean.WeekItem> data) {
        super(layout, data);
        this.layout = layout;
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, WeekDataBean.WeekItem item) {
        helper.setText(R.id.title, item.getTitle());
        helper.setText(R.id.episodes, item.getEpisodes());
        if (layout == WeekDataBean.ITEM_TYPE_0 || layout == WeekDataBean.ITEM_TYPE_1) {
            String imgUrl = item.getImgUrl();
            ImageView imageView = helper.getView(R.id.img);
            imageView.setTag(R.id.imageid, imgUrl);
            Utils.setDefaultImage(item.getImgUrl(), item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
            helper.setVisible(R.id.episodesLayout, true);
        }
    }
}
