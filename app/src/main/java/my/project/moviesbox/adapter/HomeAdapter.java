package my.project.moviesbox.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.HeroCarouselStrategy;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: HomeAdapter
  * @描述: 首页数据列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 16:40
  * @版本: 1.0
 */
public class HomeAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private Context context; // 上下文
    private OnItemClick onItemClick; // 首页相关控件点击事件接口
    private HomeItemAdapter homeItemAdapter; // 影视列表ITEM适配器

    private SnapHelper snapHelper = new CarouselSnapHelper();
    /**
     * @方法名称: HomeAdapter
     * @方法描述: 适配器构造方法
     * @日期: 2024/1/22 16:54
     * @作者: Li Z
     * @param context 上下文
     * @param data 列表数据 {@link MainDataBean}数据实体需要实现接口MultiItemEntity
     * @param onItemClick 点击事件 {@link OnItemClick}
     * @返回:
     */
    public HomeAdapter(Context context, List data, OnItemClick onItemClick) {
        super(data);
        this.context = context;
        this.onItemClick = onItemClick;
        /**
         * 添加视图类型
         * R.layout.item_home_header为首页CHIP列表布局 {@link MainDataBean.TAG_LIST}
         * R.layout.item_home_banner为首页轮播列表布局 {@link MainDataBean.BANNER_LIST}
         * R.layout.item_home_list为首页通用列表布局 {@link MainDataBean.ITEM_LIST}
         */
        addItemType(MainDataBean.TAG_LIST, R.layout.item_home_header);
        addItemType(MainDataBean.BANNER_LIST, R.layout.item_home_banner);
        addItemType(MainDataBean.ITEM_LIST, R.layout.item_home_list);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        MainDataBean mainDataBean;
        switch (helper.getItemViewType()) {
            case MainDataBean.TAG_LIST:
                // 分类布局
                mainDataBean = (MainDataBean) item;
                ChipGroup chipGroup = helper.getView(R.id.chip_group);
                chipGroup.removeAllViews();
                List<MainDataBean.Tag> tags = mainDataBean.getTags();
                for (MainDataBean.Tag tag : tags) {
                    if (Utils.isNullOrEmpty(tag))
                        continue;
                    Chip chip = new Chip(context);
                    String title = tag.getTitle();
                    char firstChar = title.charAt(0);
                    String newStr = "<font color='#f48fb1'><strong>"+firstChar+"</strong></font>" + title.substring(1);
                    chip.setText(Html.fromHtml(newStr));
                    chip.setOnClickListener(view -> onItemClick.onTagClick(tag));
                    chipGroup.addView(chip);
                }
                break;
            case MainDataBean.BANNER_LIST:
                // 轮播布局
                mainDataBean = (MainDataBean) item;
                List<MainDataBean.Item> bannerItem = mainDataBean.getItems();
                RecyclerView bannerRecyclerView = helper.getView(R.id.rv_list);
                HomeBannerAdapter homeBannerAdapter = new HomeBannerAdapter(bannerItem);
                homeBannerAdapter.setOnItemClickListener((adapter, view, position) ->
                        onItemClick.onVideoClick(bannerItem.get(position)
                        ));
                bannerRecyclerView.setAdapter(homeBannerAdapter);
                snapHelper.attachToRecyclerView(bannerRecyclerView);
                bannerRecyclerView.setLayoutManager(new CarouselLayoutManager(new HeroCarouselStrategy()));
                /*CarouselLayoutManager layoutManager = new CarouselLayoutManager();
                layoutManager.setCarouselAlignment(CarouselLayoutManager.ALIGNMENT_CENTER);
                layoutManager.setCarouselStrategy(new HeroCarouselStrategy());
                bannerRecyclerView.setLayoutManager(layoutManager);*/
                break;
           case MainDataBean.ITEM_LIST:
               // 列表布局
               RecyclerView recyclerView = helper.getView(R.id.rv_list);
               mainDataBean = (MainDataBean) item;
               List<MainDataBean.Item> items = mainDataBean.getItems();
               if (Utils.isNullOrEmpty(mainDataBean.getTitle()))
                   helper.getView(R.id.more).setVisibility(View.GONE);
                else {
                   helper.setText(R.id.title, mainDataBean.getTitle());
                   helper.getView(R.id.more).setVisibility(View.VISIBLE);
               }
               if (mainDataBean.isHasMore())
                   helper.getView(R.id.img).setVisibility(View.VISIBLE);
               else
                   helper.getView(R.id.img).setVisibility(View.GONE);
               recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
               switch (mainDataBean.getVodItemType()) {
                   case MainDataBean.Item.ITEM_TYPE_0:
                       homeItemAdapter = new HomeItemAdapter(R.layout.item_home_data_type_0, items);
                       break;
                   case MainDataBean.Item.ITEM_TYPE_1:
                       homeItemAdapter = new HomeItemAdapter(R.layout.item_home_data_type_1, items);
                       break;
               }
               homeItemAdapter.setOnItemClickListener((adapter, view, position) ->
                       onItemClick.onVideoClick(items.get(position)
                       ));
               recyclerView.setPadding(0,0,0, 10);
               recyclerView.setAdapter(homeItemAdapter);
               break;
        }
    }

    /**
      * @包名: my.project.moviesbox.adapter
      * @类名: HomeAdapter
      * @描述: 首页相关ITEM点击接口
      * @作者: Li Z
      * @日期: 2024/1/22 17:03
      * @版本: 1.0
     */
    public interface OnItemClick {
        /**
         * CHIP点击接口
         * @param tag
         */
        void onTagClick(MainDataBean.Tag tag);

        /**
         * 轮播ITEM点击接口
         * @param data
         */
        void onVideoClick(MainDataBean.Item data);
    }
}
