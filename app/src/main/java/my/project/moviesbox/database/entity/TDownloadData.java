package my.project.moviesbox.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TDownloadData
  * @描述: 影视下载列表剧集子表实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:03
  * @版本: 1.0
 */
@Entity
@Data
public class TDownloadData implements Serializable {
    /**
     * 主键
     */
    @PrimaryKey(autoGenerate = true)
    private int index;
    /**
     * 数据ID
     */
    private String downloadDataId;
    /**
     * 关联ID {@link TDownload#downloadId}
     */
    private String linkId;
    /**
     * 影视集数标题
     */
    private String videoNumber;
    /**
     * 是否下载完成
     * <p>0：待下载 </p>
     * <p>1：下载成功 </p>
     * <p>2：下载失败</p>
     * <p>3：正在转码</p>
     */
    private int complete;
    /**
     * 文件保存位置
     */
    private String savePath;
    /**
     * 文件大小
     */
    private long videoFileSize;
    /**
     * 影视所属播放列表下标
     */
    private int videoPlaySource;
    /**
     * aria下载ID
     */
    private long ariaTaskId;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 观看进度
     */
    private long watchProgress;
    /**
     * 视频总长度
     */
    private long videoDuration;
}
