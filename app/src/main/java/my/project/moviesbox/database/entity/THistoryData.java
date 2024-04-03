package my.project.moviesbox.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: THistoryData
  * @描述: 历史记录子表实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:19
  * @版本: 1.0
 */
@Entity
@Data
public class THistoryData implements Serializable {
    /**
     * 主键
     */
    @PrimaryKey(autoGenerate = true)
    public int index;
    /**
     * 数据ID
     */
    public String historyDataId;
    /**
     * 关联ID {@link THistory#historyId}
     */
    public String linkId;
    /**
     * 播放剧集所属播放列表下标
     */
    public int videoPlaySource;
    /**
     * 影视地址
     */
    public String videoUrl;
    /**
     * 影视集数标题
     */
    public String videoNumber;
    /**
     * 观看进度
     */
    public long watchProgress;
    /**
     * 影视时长
     */
    public long videoDuration;
    /**
     * 更新时间
     */
    public String updateTime;
}
