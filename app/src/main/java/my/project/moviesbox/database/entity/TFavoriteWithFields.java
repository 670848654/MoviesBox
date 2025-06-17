package my.project.moviesbox.database.entity;

import androidx.room.Embedded;
import androidx.room.Ignore;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

import lombok.Data;
import my.project.moviesbox.parser.config.VodItemStyleEnum;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TFavoriteWithFields
  * @描述: 收藏列表查询字段实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:17
  * @版本: 1.0
 */
@Data
public class TFavoriteWithFields implements MultiItemEntity, Serializable {
    @Embedded
    private TFavorite tFavorite;
    /**
     * 影视ID
     */
    private String videoId;
    /**
     * 影视标题
     */
    private String videoTitle;
    /**
     * 是否需要背景模糊
     */
    @Ignore
    private boolean blurBg;
    /**
     * 是否已刷新封面
     */
    @Ignore
    private boolean refreshCover;
    /**
     * @see VodItemStyleEnum
     * 默认为竖屏样式
     */
    @Ignore
    private int vodItemStyleType = VodItemStyleEnum.STYLE_1_1_DOT_4.getType();

    @Override
    public int getItemType() {
        return vodItemStyleType;
    }
}
