package my.project.moviesbox.parser.bean;

import androidx.annotation.LayoutRes;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.R;
import my.project.moviesbox.parser.config.WeekEnum;

/**
  * @包名: my.project.moviesbox.parser.bean
  * @类名: WeekDataBean
  * @描述: 星期时间表数据实体
  * @作者: Li Z
  * @日期: 2024/1/22 19:16
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeekDataBean implements Serializable {
    @LayoutRes
    public final static int ITEM_TYPE_0 = R.layout.item_week_1_4_1; // 带图片的布局视图 宽<高[类似：1:1.4]
    @LayoutRes
    public final static int ITEM_TYPE_1 = R.layout.item_week_16_9; // 带图片的布局视图 宽>高[类似：16:9]
    @LayoutRes
    public final static int ITEM_TYPE_2 = R.layout.item_week; // 布局类型 纯文本
    /**
     * 星期 使用{@link WeekEnum}枚举定义
     */
    private int weekDay;
    /**
     * 每个星期下的数据列表
     */
    private List<WeekItem> weekItems;

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: WeekDataBean.WeekItem
      * @描述: 星期时间表数据 子数据实体
      * @作者: Li Z
      * @日期: 2024/1/22 19:18
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekItem implements Serializable {
        /**
         * 剧集标题
         */
        private String title;
        /**
         * 剧集标题占用行数
         */
        private int titleLines = 2;
        /**
         * 剧集图片
         */
        private String imgUrl;
        /**
         * 剧集访问地址
         */
        private String url;
        /**
         * 集数标题
         */
        private String episodes;
        /**
         * 集数地址 暂未支持跳转
         */
        private String episodesUrl;
        /**
         * 左上角TAG
         */
        private String topLeftTag;

        public WeekItem(String title, int titleLines, String url, String episodes, String episodesUrl) {
            this.title = title;
            this.titleLines = titleLines;
            this.url = url;
            this.episodes = episodes;
            this.episodesUrl = episodesUrl;
        }

        public WeekItem(String title, String imgUrl, String url, String episodes, String episodesUrl) {
            this.title = title;
            this.imgUrl = imgUrl;
            this.url = url;
            this.episodes = episodes;
            this.episodesUrl = episodesUrl;
        }

        public WeekItem(String title, String imgUrl, String url, String episodes, String episodesUrl, String topLeftTag) {
            this.title = title;
            this.imgUrl = imgUrl;
            this.url = url;
            this.episodes = episodes;
            this.episodesUrl = episodesUrl;
            this.topLeftTag = topLeftTag;
        }

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
