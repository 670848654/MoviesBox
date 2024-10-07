package my.project.moviesbox.parser.bean;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.parser.config.VodItemStyleEnum;

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
public class VodDataBean implements MultiItemEntity, Serializable {
    /**
     * @see VodItemStyleEnum
     * 默认为竖屏样式
     */
    private int vodItemStyleType = VodItemStyleEnum.STYLE_1_1_DOT_4.getType();
    private String title; // 标题
    private String img; // 图片
    private String url; // 地址
    private String topLeftTag; // 左上角TAG
    private String episodesTag; // 图片底部TAG 一般为集数

    @Override
    public int getItemType() {
        return vodItemStyleType;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
