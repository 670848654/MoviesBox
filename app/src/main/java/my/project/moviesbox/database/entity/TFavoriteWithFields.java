package my.project.moviesbox.database.entity;

import androidx.room.Embedded;
import androidx.room.Ignore;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TFavoriteWithFields
  * @描述: 收藏列表查询字段实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:17
  * @版本: 1.0
 */
@Data
public class TFavoriteWithFields implements Serializable {
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
}
