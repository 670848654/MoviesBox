package my.project.moviesbox.adapter;

import android.widget.ImageView;

import androidx.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.database.entity.TFavoriteWithFields;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: FavoriteListAdapter
  * @描述: 收藏夹数据列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:39
  * @版本: 1.0
 */
public class FavoriteListAdapter extends BaseQuickAdapter<TFavoriteWithFields, BaseViewHolder> implements LoadMoreModule {
    public FavoriteListAdapter(@LayoutRes int layout, List<TFavoriteWithFields> list) {
        super(layout, list);
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    @Override
    protected void convert(BaseViewHolder helper, TFavoriteWithFields item) {
//        item.setRefreshCover(false);
        String imgUrl = item.getTFavorite().getVideoImgUrl();
        ImageView imageView = helper.getView(R.id.img);
        ImageView backgroundImageView = helper.getView(R.id.backgroundImageView);
        imageView.setTag(R.id.imageid, imgUrl);
        backgroundImageView.setTag(R.id.imageid, imgUrl);
        if (item.isBlurBg()) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Utils.setImgViewBlurBg(item.getTFavorite().getVideoImgUrl(), backgroundImageView);
        }
        else
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgUrl = item.isRefreshCover() ? null : imgUrl;
        Utils.setDefaultImage(imgUrl, item.getTFavorite().getVideoUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title), true);
        helper.setText(R.id.title, item.getVideoTitle());
        String lastPlayNumber = item.getTFavorite().getLastVideoUpdateNumber();
        if (Utils.isNullOrEmpty(lastPlayNumber))
            helper.setText(R.id.lastPlayNumber, Utils.getString(R.string.neverWatched));
        else {
            helper.setText(R.id.lastPlayNumber, String.format(Utils.getString(R.string.watchTo), lastPlayNumber));
        }
        helper.setVisible(R.id.lastPlayNumber, true);
//        helper.getView(R.id.new_view).setVisibility(item.getState() == 1 ? View.VISIBLE : View.GONE);
    }
}
