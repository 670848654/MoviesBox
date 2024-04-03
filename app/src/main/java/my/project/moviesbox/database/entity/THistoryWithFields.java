package my.project.moviesbox.database.entity;

import androidx.room.Embedded;
import androidx.room.Ignore;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: THistoryWithFields
  * @描述: 历史记录列表查询字段实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:22
  * @版本: 1.0
 */
@Data
public class THistoryWithFields implements Serializable {
    @Embedded
    private THistory tHistory;
    /**
     * 影视ID
     */
    private String videoId;
    /**
     * 影视标题
     */
    private String videoTitle;
    /**
     * 影视所属源
     */
    private int videoSource;
    /**
     * 播放剧集所属播放列表下标
     */
    @Ignore
    private int videoPlaySource;
    /**
     * 影视地址
     */
    @Ignore
    private String videoUrl;
    /**
     * 影视剧集标题
     */
    @Ignore
    private String videoNumber;
    /**
     * 观看进度
     */
    @Ignore
    private long watchProgress;
    /**
     * 影视时长
     */
    @Ignore
    private long videoDuration;
}
