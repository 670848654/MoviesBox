package my.project.moviesbox.adapter;

import android.widget.ImageView;

import androidx.annotation.IdRes;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: VodListAdapter
  * @描述: 影视列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 17:15
  * @版本: 1.0
 */
public class VodListAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> implements LoadMoreModule {
    /**
     * @方法名称: VodListAdapter
     * @方法描述: 构造方法
     * @日期: 2024/1/22 17:15
     * @作者: Li Z
     * @param data {@link VodDataBean}
     * @返回: 
     */
    public VodListAdapter(List<MultiItemEntity> data) {
        super(data);
        addItemType(VodItemStyleEnum.STYLE_1_1_DOT_4.getType(), VodItemStyleEnum.STYLE_1_1_DOT_4.getLayoutId());
        addItemType(VodItemStyleEnum.STYLE_16_9.getType(), VodItemStyleEnum.STYLE_16_9.getLayoutId());
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        VodDataBean vodDataBean = (VodDataBean) item;
        String imgUrl = vodDataBean.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        /*if (item.getItemType() == VodItemStyleEnum.STYLE_16_9.getType()) {
            Utils.setImgViewBlurBg(vodDataBean.getImg(), imageView);
        } else*/
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Utils.setDefaultImage(vodDataBean.getImg(), vodDataBean.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        helper.setText(R.id.title, vodDataBean.getTitle());
        setTagInfo(helper, vodDataBean.getTopLeftTag(), R.id.topLeftTag);
        setTagInfo(helper, vodDataBean.getEpisodesTag(), R.id.episodesTag);
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
