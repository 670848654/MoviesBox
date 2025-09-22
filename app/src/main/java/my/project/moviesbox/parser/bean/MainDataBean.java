package my.project.moviesbox.parser.bean;

import androidx.annotation.DrawableRes;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.adapter.HomeItemAdapter;
import my.project.moviesbox.parser.config.ItemStyleEnum;
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
    private String title; // 标题
    private String url; // 地址
    private boolean hasMore; // 是否有更多
    private String more; // 更多跳转地址
    private Class openMoreClass = ClassificationVodListActivity.class; // 打开更多时的视图class 默认为分类视图
    private int dataType; // 布局
    private ItemStyleEnum vodItemType = ItemStyleEnum.STYLE_1_1_DOT_4; // item布局类型
    private List<Item> items; // 子列表数据
    private List<Tag> tags; // 头部分类数据
    private List<DropDownTag> dropDownTags; // 下拉菜单数据
    private HomeItemAdapter homeItemAdapter;

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

        public Tag(String title) {
            this.title = title;
        }

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
      * @类名: MainDataBean
      * @描述: 用于首页下拉菜单列表实体
      * @作者: Li Z
      * @日期: 2024/9/27 14:08
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropDownTag implements Serializable {
        private String title; // 标题
        private int img; // 图标 预留
        private String url;
        private Class openClass; // 打开的class
        private boolean hasMenu = true; // 是否有列表数据，没有则不显示箭头
        private List<DropDownMenu> dropDownMenus;

        /**
         * 用于没有下拉菜单的构造方法
         * @param title
         * @param url
         * @param openClass
         */
        public DropDownTag(String title, String url, Class openClass) {
            this.title = title;
            this.url = url;
            this.openClass = openClass;
            this.hasMenu = false;
        }

        /**
         * 用于有下拉菜单的构造方法
         * @param title
         * @param dropDownMenus
         */
        public DropDownTag(String title, List<DropDownMenu> dropDownMenus) {
            this.title = title;
            this.dropDownMenus = dropDownMenus;
        }

        /**
         * 下拉菜单列表实体
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DropDownMenu {
            private String title;
            private String url;
            private Class openClass; // 打开的class
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
        private String title; // 剧集标题
        private String img; // 图片地址
        private String base64Img; // 图片解密后Base64字符串数组
        private String url; // 访问地址
        private String episodes; // 集数
        private String episodesUrl; // 集数跳转地址 -> 一般为播放地址
        private String previewUrl; // 视频预览地址
        private Class openClass = DetailsActivity.class; // 一般为详情界面
        private String topLeftTag; // 左上角TAG

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
