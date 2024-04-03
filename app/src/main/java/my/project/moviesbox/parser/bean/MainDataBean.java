package my.project.moviesbox.parser.bean;

import androidx.annotation.DrawableRes;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.DetailsActivity;

/**
  * @包名: my.project.moviesbox.parser.bean
  * @类名: MainDataBean
  * @描述: APP首页数据实体
  * @作者: Li Z
  * @日期: 2024/1/23 22:12
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainDataBean implements MultiItemEntity, Serializable {
    public static final int TAG_LIST = 0; // 首页TAG
    public static final int BANNER_LIST = 1; // 轮播
    public static final int ITEM_LIST = 2; // 剧集列表
    private String title; // 标题
    private String url; // 地址
    private boolean hasMore; // 是否有更多
    private String more; // 更多跳转地址
    private Class openMoreClass = ClassificationVodListActivity.class; // 打开更多时的视图class 默认为分类视图
    private int dataType; // 布局
    private int vodItemType = Item.ITEM_TYPE_0; // item布局类型
    private List<Item> items; // 子列表数据
    private List<Tag> tags; // 头部分类数据

    @Override
    public int getItemType() {
        return dataType;
    }

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: MainDataBean.Tag
      * @描述: 头部分类数据列表实体
      * @日期: 2024/1/23 22:12
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tag implements Serializable {
        private String title; // 标题
        private String url; // 地址
        @DrawableRes
        private int img; // 图标 预留
        private Class openClass; // 打开的class

        public Tag(String title, String url, Class openClass) {
            this.title = title;
            this.url = url;
            this.openClass = openClass;
        }

        @Override
        public String toString() {
            return JSONObject.toJSONString(this);
        }
    }

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: MainDataBean.Item
      * @描述: 首页banner/视频数据列表通用实体
      * @作者: Li Z
      * @日期: 2024/1/23 22:12
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {
        // 如需要更多布局类型 需要自行定义实现，默认仅提供两种
        public static final int ITEM_TYPE_0 = 0; // 布局类型 宽<高[类似：1:1.4]
        public static final int ITEM_TYPE_1 = 1; // 布局类型 宽>高[类似：16:9]
        private String title; // 剧集标题
        private String img; // 图片地址
        private String url; // 访问地址
        private String episodes; // 集数
        private String episodesUrl; // 集数跳转地址 -> 一般为播放地址
        private Class openClass = DetailsActivity.class; // 一般为详情界面

        @Override
        public String toString() {
            return JSONObject.toJSONString(this);
        }
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
