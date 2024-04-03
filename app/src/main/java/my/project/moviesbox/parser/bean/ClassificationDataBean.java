package my.project.moviesbox.parser.bean;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.adapter.ClassificationAdapter;

/**
  * @包名: my.project.moviesbox.parser.bean
  * @类名: ClassificationDataBean
  * @描述: 分类数据实体 用于{@link ClassificationAdapter}适配器
  * @作者: Li Z
  * @日期: 2024/1/23 22:16
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationDataBean implements MultiItemEntity, Serializable {
    public final static int ITEM_TYPE = 0;
    private String classificationTitle; // 分类标题
    private boolean multipleChoices; // 分类是否为多选
    private int index; // 下标
    private List<Item> itemList; // 数据列表

    @Override
    public int getItemType() {
        return ITEM_TYPE;
    }

    /**
      * @包名: my.project.moviesbox.parser.bean
      * @类名: ClassificationDataBean.Item
      * @描述: 分类数据列表实体
      * @作者: Li Z
      * @日期: 2024/1/23 22:16
      * @版本: 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {
        private String title; // 标题
        private String url; // 地址
        private boolean selected; // 默认选择

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
