package my.project.moviesbox.adapter;

import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.HeroCarouselStrategy;
import com.google.android.material.carousel.UncontainedCarouselStrategy;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.config.MultiItemEnum;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
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
    private boolean isPortrait;
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
    public HomeAdapter(Context context, List data, boolean isPortrait, OnItemClick onItemClick) {
        super(data);
        this.context = context;
        this.isPortrait = isPortrait;
        this.onItemClick = onItemClick;
        /**
         * 添加视图类型
         * R.layout.item_home_header为首页CHIP列表布局 {@link MultiItemEnum#TAG_LIST}
         * R.layout.item_home_header为首页CHIP列表布局（弹出menu） {@link MultiItemEnum#DROP_DOWN_TAG}
         * R.layout.item_home_banner为首页轮播列表布局 {@link MultiItemEnum#BANNER_LIST}
         * R.layout.item_home_list为首页通用列表布局 {@link MultiItemEnum#ITEM_LIST} {@link MultiItemEnum#VOD_LIST}
         */
        addItemType(MultiItemEnum.TAG_LIST.getType(), R.layout.item_home_header);
        addItemType(MultiItemEnum.DROP_DOWN_TAG.getType(), R.layout.item_home_header);
        addItemType(MultiItemEnum.BANNER_LIST.getType(), R.layout.item_home_banner);
        addItemType(MultiItemEnum.ITEM_LIST.getType(), R.layout.item_home_list);
        addItemType(MultiItemEnum.VOD_LIST.getType(), R.layout.item_home_list);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        MainDataBean mainDataBean;
        if (helper.getItemViewType() == MultiItemEnum.TAG_LIST.getType()) {
            // 分类布局
            mainDataBean = (MainDataBean) item;
            ChipGroup chipGroup = helper.getView(R.id.chip_group);
            chipGroup.removeAllViews();
            List<MainDataBean.Tag> tags = mainDataBean.getTags();
            LayoutInflater inflater = LayoutInflater.from(context);
            for (MainDataBean.Tag tag : tags) {
                if (Utils.isNullOrEmpty(tag))
                    continue;
                /*Chip chip = new Chip(context);
                chip.setEnsureMinTouchTargetSize(false);*/
                Chip chip = (Chip) inflater.inflate(R.layout.chip_template, chipGroup, false);
                String title = tag.getTitle();
                char firstChar = title.charAt(0);
                String newStr = "<font color='#f48fb1'><strong>"+firstChar+"</strong></font>" + title.substring(1);
                chip.setText(Html.fromHtml(newStr));
                chip.setOnClickListener(view -> {
                    Utils.setVibration(view);
                    onItemClick.onTagClick(tag);
                });
                chipGroup.addView(chip);
            }
        } else if (helper.getItemViewType() == MultiItemEnum.DROP_DOWN_TAG.getType()) {
            // 首页下拉菜单布局
            mainDataBean = (MainDataBean) item;
            List<MainDataBean.DropDownTag> dropDownTags = mainDataBean.getDropDownTags();
            ChipGroup chipGroup = helper.getView(R.id.chip_group);
            chipGroup.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(context);
            for (MainDataBean.DropDownTag dropDownTag : dropDownTags) {
                if (Utils.isNullOrEmpty(dropDownTag))
                    continue;
                boolean hasMenu = dropDownTag.isHasMenu();
                /*Chip chip = new Chip(context);
                chip.setEnsureMinTouchTargetSize(false);*/
                Chip chip = (Chip) inflater.inflate(R.layout.chip_drop_down_template, chipGroup, false);
                chip.setCloseIconVisible(hasMenu);
                String title = dropDownTag.getTitle();
                char firstChar = title.charAt(0);
                String newStr = "<font color='#f48fb1'><strong>"+firstChar+"</strong></font>" + title.substring(1);
                chip.setText(Html.fromHtml(newStr));
                chip.setOnClickListener(view -> {
                    Utils.setVibration(view);
                    if (hasMenu)
                        onItemClick.onDropDownTagClick(dropDownTag.getDropDownMenus(), chip);
                    else
                        onItemClick.onDropDownTagNoMenusClick(dropDownTag, view);
                });
                chipGroup.addView(chip);
            }
        }
        else if (helper.getItemViewType() == MultiItemEnum.BANNER_LIST.getType()) {
            // 轮播布局
            mainDataBean = (MainDataBean) item;
            List<MainDataBean.Item> bannerItem = mainDataBean.getItems();
            RecyclerView bannerRecyclerView = helper.getView(R.id.rv_list);
            HomeBannerAdapter homeBannerAdapter = new HomeBannerAdapter(bannerItem);
            homeBannerAdapter.setOnItemClickListener((adapter, view, position) -> {
                Utils.setVibration(view);
                onItemClick.onVideoClick(bannerItem.get(position));
            });
            bannerRecyclerView.setAdapter(homeBannerAdapter);
            CarouselLayoutManager layoutManager;
            if (Utils.isPad()) {
                // 平板模式下数量小于3时不使用滚动策略
                if (bannerItem.size() > 3)
                    layoutManager = new CarouselLayoutManager(new HeroCarouselStrategy());
                else
                    layoutManager = new CarouselLayoutManager(new UncontainedCarouselStrategy());
            } else
                layoutManager = new CarouselLayoutManager(new HeroCarouselStrategy());
            bannerRecyclerView.setLayoutManager(layoutManager);
            snapHelper.attachToRecyclerView(bannerRecyclerView);
            if (bannerItem.size() > 3)
                // 自动轮播绑定
                new BannerAutoPlayHelper(bannerRecyclerView, snapHelper);
        } else if (helper.getItemViewType() == MultiItemEnum.ITEM_LIST.getType() ||
                helper.getItemViewType() == MultiItemEnum.VOD_LIST.getType()) {
            // 列表布局
            RecyclerView recyclerView = helper.getView(R.id.rv_list);
            mainDataBean = (MainDataBean) item;
            List<MainDataBean.Item> items = mainDataBean.getItems();
            if (Utils.isNullOrEmpty(mainDataBean.getTitle()))
                helper.getView(R.id.moreLayout).setVisibility(View.GONE);
            else {
                helper.setText(R.id.title, mainDataBean.getTitle());
                helper.getView(R.id.moreLayout).setVisibility(View.VISIBLE);
            }
            if (mainDataBean.isHasMore())
                helper.getView(R.id.moreBtn).setVisibility(View.VISIBLE);
            else
                helper.getView(R.id.moreBtn).setVisibility(View.GONE);
            if (helper.getItemViewType() == MultiItemEnum.ITEM_LIST.getType()) {
                // 横向列表
                recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                switch (mainDataBean.getVodItemType()) {
                    case STYLE_1_1_DOT_4:
                        homeItemAdapter = new HomeItemAdapter(R.layout.item_home_data_type_0, items);
                        break;
                    case STYLE_16_9:
                        homeItemAdapter = new HomeItemAdapter(R.layout.item_home_data_type_1, items);
                        break;
                }
            } else if (helper.getItemViewType() == MultiItemEnum.VOD_LIST.getType()) {
                // 纵向列表
                int spanCount;
                if (mainDataBean.getVodItemType() == STYLE_16_9) {
                    spanCount = ParserInterfaceFactory.getParserInterface().setVodList16_9ItemSize(Utils.isPad(), isPortrait, false);
                } else {
                    spanCount = ParserInterfaceFactory.getParserInterface().setVodListItemSize(Utils.isPad(), isPortrait, false);
                }
                recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));
                switch (mainDataBean.getVodItemType()) {
                    case STYLE_1_1_DOT_4:
                        homeItemAdapter = new HomeItemAdapter(R.layout.item_home_data_type_grid_0, items);
                        break;
                    case STYLE_16_9:
                        homeItemAdapter = new HomeItemAdapter(R.layout.item_home_data_type_grid_1, items);
                        break;
                }
            }
            homeItemAdapter.setOnItemClickListener((adapter, view, position) -> {
                Utils.setVibration(view);
                onItemClick.onVideoClick(items.get(position));
            }
            );
            homeItemAdapter.setOnItemLongClickListener((adapter, view, position) -> {
                onItemClick.onVideoLongClick(items.get(position));
                return true;
            }
            );
            mainDataBean.setHomeItemAdapter(homeItemAdapter);
            recyclerView.setPadding(0,0,0, 10);
            recyclerView.setAdapter(homeItemAdapter);
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
         * 下拉菜单点击接口
         * @param dropDownMenus
         */
        void onDropDownTagClick(List<MainDataBean.DropDownTag.DropDownMenu> dropDownMenus, View view);

        /**
         * 没有下拉菜单时
         * @param dropDownTag
         */
        void onDropDownTagNoMenusClick(MainDataBean.DropDownTag dropDownTag, View view);

        /**
         * ITEM点击接口
         * @param data
         */
        void onVideoClick(MainDataBean.Item data);

        /**
         * ITEM长按点击接口
         * @param data
         */
        void onVideoLongClick(MainDataBean.Item data);
    }
}
