package my.project.moviesbox.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TDownload
  * @描述: 影视下载列表主表实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:02
  * @版本: 1.0
 */
@Entity
@Data
public class TDownload implements Serializable {
    /**
     * 主键
     */
    @PrimaryKey(autoGenerate = true)
    private int index;
    /**
     * 数据ID
     */
    private String downloadId;
    /**
     * 关联ID {@link TVideo#videoId}
     */
    private String linkId;
    /**
     * 影视图片地址
     */
    private String videoImgUrl;
    /**
     * 影视详情地址
     */
    private String videoDescUrl;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
}
