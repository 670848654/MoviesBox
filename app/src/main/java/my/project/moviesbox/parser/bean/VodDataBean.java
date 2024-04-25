package my.project.moviesbox.parser.bean;

import androidx.annotation.LayoutRes;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.R;

/**
  * @包名: my.project.moviesbox.parser.bean
  * @类名: VodDataBean
  * @描述: 影视数据实体
  * @作者: Li Z
  * @日期: 2024/1/23 22:13
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VodDataBean implements Serializable {
    @LayoutRes
    public final static int ITEM_TYPE_0 = R.layout.item_vod_type_1_4_1; // 布局类型 宽<高[类似：1:1.4]
    @LayoutRes
    public final static int ITEM_TYPE_1 = R.layout.item_vod_type_16_9; // 布局类型 宽>高[类似：16:9]
    private List<Item> itemList = new ArrayList<>();

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: VodDataBean.Item
      * @描述: 影视数据列表实体
      * @作者: Li Z
      * @日期: 2024/1/23 22:14
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String title; // 标题
        private String img; // 图片
        private String url; // 地址
        private String topLeftTag; // 左上角TAG
        private String episodesTag; // 图片底部TAG 一般为集数

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
