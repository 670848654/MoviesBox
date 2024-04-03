package my.project.moviesbox.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TFavorite
  * @描述: 收藏列表主表实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:11
  * @版本: 1.0
 */
@Entity
@Data
public class TFavorite implements Serializable {
    /**
     * 主键
     */
    @PrimaryKey(autoGenerate = true)
    public int index;
    /**
     * 数据ID
     */
    public String favoriteId;
    /**
     * 关联ID {@link TVideo#videoId}
     */
    public String linkId;
    /**
     * 影视图片地址
     */
    public String videoImgUrl;
    /**
     * 影视地址
     */
    public String videoUrl;
    /**
     * 影视详情
     */
    public String videoDesc;
    /**
     * 最后一次播放集数地址
     */
    public String lastVideoPlayNumberUrl;
    /**
     * 最后一次播放集数名称
     */
    public String lastVideoUpdateNumber;
    /**
     * 状态
     */
    @Deprecated
    public int state;
}
