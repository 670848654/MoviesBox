package my.project.moviesbox.adapter;

import static com.google.android.material.animation.AnimationUtils.lerp;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.carousel.MaskableFrameLayout;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: HomeBannerAdapter
  * @描述: 首页轮播ITEM列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 17:03
  * @版本: 1.0
 */
public class HomeBannerAdapter extends BaseQuickAdapter<MainDataBean.Item, BaseViewHolder>  {
    private boolean singleMode;

    public HomeBannerAdapter(@Nullable List<MainDataBean.Item> data) {
        super(R.layout.item_banner, data);
        singleMode = data != null && data.size() == 1;
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void convert(final BaseViewHolder helper, MainDataBean.Item item) {
        View itemView = helper.itemView;
        ViewGroup.LayoutParams params = itemView.getLayoutParams();

        if (singleMode) {
            params.width = Utils.isPad() ? dpToPx(320) : ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            params.width = dpToPx(Utils.isPad() ? 320 : 150);
        }

        itemView.setLayoutParams(params);

        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.img,  item.getImg());
        Utils.setDefaultImage(item.getImg(), imageView);
        TextView titleView = helper.getView(R.id.title);
        titleView.setText(item.getTitle());
        TextView episodesView = helper.getView(R.id.episodes);
        if (Utils.isNullOrEmpty(item.getEpisodes())) {
            episodesView.setVisibility(View.GONE);
        } else {
            episodesView.setText(item.getEpisodes());
            episodesView.setVisibility(View.VISIBLE);
        }
        MaskableFrameLayout maskableFrameLayout = helper.getView(R.id.carousel_item_container);
        maskableFrameLayout.setOnMaskChangedListener(maskRect -> {
            setTextView(titleView, maskRect);
            setTextView(episodesView, maskRect);
        });
    }

    @SuppressLint("RestrictedApi")
    private void setTextView(TextView textView, RectF maskRect) {
        textView.setTranslationX(maskRect.left);
        textView.setAlpha(lerp(1F, 0F, 0F, 80F, maskRect.left));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }
}
