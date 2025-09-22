package my.project.moviesbox.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.config.ItemStyleEnum;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: HomeItemAdapter
  * @描述: 首页影视列表ITEM适配器
  * @作者: Li Z
  * @日期: 2024/1/22 16:41
  * @版本: 1.0
 */
public class HomeItemAdapter extends BaseQuickAdapter<MainDataBean.Item, BaseViewHolder> {

    /**
     * @方法名称: HomeItemAdapter
     * @方法描述: 适配器构造方法
     * @日期: 2024/1/22 16:45
     * @作者: Li Z
     * @param layoutResId 布局ID 参考{@link ItemStyleEnum}
     * @param data 列表数据{@link MainDataBean.Item}
     * @返回:
     */
    public HomeItemAdapter(@LayoutRes int layoutResId, List<MainDataBean.Item> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MainDataBean.Item item) {
        String imgUrl = item.getImg();
        ImageView imageView = helper.getView(R.id.img);
        String base64 = item.getBase64Img();
        imageView.setTag(R.id.imageid, imgUrl);
        String episodes = item.getEpisodes();
        if (Utils.isNullOrEmpty(episodes))
            helper.getView(R.id.episodes).setVisibility(View.GONE);
        else {
            helper.getView(R.id.episodes).setVisibility(View.VISIBLE);
            helper.setText(R.id.update_time, episodes);
        }
        String topLeftTag = item.getTopLeftTag();
        if (Utils.isNullOrEmpty(topLeftTag))
            helper.getView(R.id.topLeftTag).setVisibility(View.GONE);
        else {
            helper.getView(R.id.topLeftTag).setVisibility(View.VISIBLE);
            helper.setText(R.id.topLeftTag, topLeftTag);
        }
        Utils.setDefaultImage(Utils.isNullOrEmpty(base64) ? imgUrl : base64, item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title), false, false);
        helper.setText(R.id.title, item.getTitle());
    }
}
