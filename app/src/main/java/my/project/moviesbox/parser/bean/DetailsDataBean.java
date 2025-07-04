package my.project.moviesbox.parser.bean;

import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_1_1_DOT_4;

import androidx.annotation.DrawableRes;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.parser.config.ItemStyleEnum;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.view.BaseActivity;

/**
  * @包名: my.project.moviesbox.parser.bean
  * @类名: DetailsDataBean
  * @描述: 影视详情信息实体
  * @作者: Li Z
  * @日期: 2024/1/23 22:17
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailsDataBean implements Serializable {
    private String title;                               // 标题
    private String img;                                 // 图片
    private ItemStyleEnum imgStyle = STYLE_1_1_DOT_4;   // 图片样式
    private String url;                                 // 详情访问地址
    private List<String> tagTitles;                     // 分类标题
    private List<String> tagUrls;                       // 分类访问地址
    private String info;                                // 默认为其他 [可灵活赋值]
    private String score;                               // 默认为评分 [可灵活赋值]
    private String updateTime;                          // 默认为更新日期 [可灵活赋值]
    private String introduction;                        // 剧集详情
    /**
     * 保留字段 暂未实现
     * 是否有无更新
     * 0无更新 1有更新
     */
    private int state;
    /**
     * 数据源 {@link ParserInterfaceFactory}
     */
    private int source;
    private boolean hasDetailFab;                               // 是否显示右下角浮动按钮
    private DetailsFabBren detailsFabBren;
    private String vodId;                                       // 数据库ID
    private List<Dramas> dramasList = new ArrayList<>();        // 播放列表集合
    private List<Recommend> multiList = new ArrayList<>();      // 剧集多季集合 预留
    private ItemStyleEnum multiStyle = STYLE_1_1_DOT_4;         // 多季ITEM样式
    private List<Recommend> recommendList = new ArrayList<>();  // 推荐集合
    private ItemStyleEnum recommendStyle = STYLE_1_1_DOT_4;     // 推荐ITEM样式

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: DetailsDataBean.Dramas
      * @描述: 播放线路列表包装类
      * @作者: Li Z
      * @日期: 2024/1/23 22:18
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dramas implements Serializable {
        private String listTitle;                   // 播放线路标题 例如：A播放列表、B播放列表
        private List<DramasItem> dramasItemList;    // 集数列表

        @Override
        public String toString() {
            return JSONObject.toJSONString(this);
        }
    }

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: DetailsDataBean.DramasItem
      * @描述: 集数列表包装类
      * @作者: Li Z
      * @日期: 2024/1/23 22:18
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DramasItem implements Serializable {
        private String title;           // 集数标题
        private String url;             // 集数地址
        private Integer index;          // 集数列表下标，用于弹幕接口 预留
        private boolean selected;       // 是否选中
        private String downloadDataId;  // 下载ID

        // 在线播放器使用
        public DramasItem(int index, String title, String url, boolean selected) {
            this.index = index;
            this.title = title;
            this.url = url;
            this.selected = selected;
        }

        // 本地播放器使用
        public DramasItem(String title, String url, boolean selected, String downloadDataId) {
            this.title = title;
            this.url = url;
            this.selected = selected;
            this.downloadDataId = downloadDataId;
        }

        @Override
        public String toString() {
            return JSONObject.toJSONString(this);
        }
    }

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: DetailsDataBean.Recommend
      * @描述: 多季/推荐列表包装类
      * @作者: Li Z
      * @日期: 2024/1/23 22:18
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommend implements MultiItemEntity, Serializable {
        private String title;       // 推荐标题
        private String img;         // 图片
        private String url;         // 地址
        private String episodes;    // 集数 预留
        private String episodesUrl; // 集数跳转地址 -> 一般为播放地址 预留
        private String previewUrl; // 视频预览地址
        /**
         * @see VodItemStyleEnum
         * 默认为竖屏样式
         */
        private int vodItemStyleType = VodItemStyleEnum.STYLE_1_1_DOT_4.getType();

        public Recommend(String title, String img, String url) {
            this.title = title;
            this.img = img;
            this.url = url;
        }

        public Recommend(String title, String img, String url, int vodItemStyleType) {
            this.title = title;
            this.img = img;
            this.url = url;
            this.vodItemStyleType = vodItemStyleType;
        }

        public Recommend(String title, String img, String url, String previewUrl, int vodItemStyleType) {
            this.title = title;
            this.img = img;
            this.url = url;
            this.previewUrl = previewUrl;
            this.vodItemStyleType = vodItemStyleType;
        }

        @Override
        public String toString() {
            return JSONObject.toJSONString(this);
        }

        @Override
        public int getItemType() {
            return vodItemStyleType;
        }
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: DetailsDataBean
      * @描述: 详情页面浮动按钮配置实体
      * @作者: Li Z
      * @日期: 2025/3/17 18:05
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailsFabBren implements Serializable {
        private String title;                               // 名称
        @DrawableRes
        private int icon;                                   // 图标资源
        private String url;                                 // 连接地址
        private Class<? extends BaseActivity> openClass;    // 打开视图

        @Override
        public String toString() {
            return JSONObject.toJSONString(this);
        }
    }
}
