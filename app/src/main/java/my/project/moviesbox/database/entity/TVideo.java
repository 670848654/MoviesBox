package my.project.moviesbox.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TVideo
  * @描述: 影视主表数据实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:23
  * @版本: 1.0
 */
@Entity
@Data
public class TVideo implements Serializable {
    /**
     * 主键
     */
    @PrimaryKey(autoGenerate = true)
    public int index;
    /**
     * 影视ID
     */
    public String videoId;
    /**
     * 影视标题
     */
    public String videoTitle;
    /**
     * 影视所属源
     */
    public int videoSource;
}
