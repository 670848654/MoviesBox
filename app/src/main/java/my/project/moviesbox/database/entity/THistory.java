package my.project.moviesbox.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: THistory
  * @描述: 历史记录主表实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:17
  * @版本: 1.0
 */
@Entity
@Data
public class THistory implements Serializable {
    /**
     * 主键
     */
    @PrimaryKey(autoGenerate = true)
    public int index;
    /**
     * 数据ID
     */
    public String historyId;
    /**
     * 关联ID {@link TVideo#videoId}
     */
    public String linkId;
    /**
     * 影视地址
     */
    public String videoDescUrl;
    /**
     * 影视图片地址
     */
    public String videoImgUrl;
    /**
     * 是否可见
     * <p>0：不可见</p>
     * <p>1：可见</p>
     */
    public int visible;
    /**
     * 更新时间
     */
    public String updateTime;
}
