package my.project.moviesbox.adapter;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DetailsListAdapter
  * @描述: 影视详情中的多季、推荐列表适配器（展开）
  * @作者: Li Z
  * @日期: 2024/10/22 14:33
  * @版本: 1.0
 */
public class DetailsExpandListItemAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    public DetailsExpandListItemAdapter(List<MultiItemEntity> data) {
        super(data);
        addItemType(VodItemStyleEnum.STYLE_1_1_DOT_4.getType(), VodItemStyleEnum.STYLE_1_1_DOT_4.getLayoutId());
        addItemType(VodItemStyleEnum.STYLE_16_9.getType(), VodItemStyleEnum.STYLE_16_9.getLayoutId());
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        DetailsDataBean.Recommend recommend = (DetailsDataBean.Recommend) item;
        String imgUrl = recommend.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        String episodes = recommend.getEpisodes();
        if (Utils.isNullOrEmpty(episodes))
            helper.getView(R.id.episodesTag).setVisibility(View.GONE);
        else {
            helper.getView(R.id.episodesTag).setVisibility(View.VISIBLE);
            helper.setText(R.id.episodesTag, episodes);
        }
        String topLeftTag = recommend.getTopLeftTag();
        if (Utils.isNullOrEmpty(topLeftTag))
            helper.getView(R.id.topLeftTag).setVisibility(View.GONE);
        else {
            helper.getView(R.id.topLeftTag).setVisibility(View.VISIBLE);
            helper.setText(R.id.topLeftTag, topLeftTag);
        }
        Utils.setDefaultImage(recommend.getImg(), recommend.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title), false, false);
        helper.setText(R.id.title, recommend.getTitle());
    }
}