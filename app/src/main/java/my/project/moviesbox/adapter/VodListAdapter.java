package my.project.moviesbox.adapter;

import android.widget.ImageView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: VodListAdapter
  * @描述: 影视列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 17:15
  * @版本: 1.0
 */
public class VodListAdapter extends BaseQuickAdapter<VodDataBean.Item, BaseViewHolder> implements LoadMoreModule {
    /**
     * @方法名称: VodListAdapter
     * @方法描述: 构造方法
     * @日期: 2024/1/22 17:15
     * @作者: Li Z
     * @param layout 当前源设定的布局ID 需实现{@link ParserInterface#setVodListItemType}
     * @param data {@link VodDataBean.Item}
     * @返回: 
     */
    public VodListAdapter(@LayoutRes int layout, List<VodDataBean.Item> data) {
        super(layout, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, VodDataBean.Item item) {
        String imgUrl = item.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        Utils.setDefaultImage(item.getImg(), item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        helper.setText(R.id.title, item.getTitle());
        setTagInfo(helper, item.getTopLeftTag(), R.id.topLeftTag);
        setTagInfo(helper, item.getEpisodesTag(), R.id.episodesTag);
    }

    private void setTagInfo(BaseViewHolder helper, String title, @IdRes int id) {
        if (Utils.isNullOrEmpty(title))
            helper.setGone(id, true);
        else {
            helper.setText(id, title);
            helper.setVisible(id, true);
        }
    }
}
