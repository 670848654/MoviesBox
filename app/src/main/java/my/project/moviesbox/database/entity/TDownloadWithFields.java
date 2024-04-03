package my.project.moviesbox.database.entity;

import androidx.room.Embedded;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TDownloadWithFields
  * @描述: 下载列表查询字段实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:10
  * @版本: 1.0
 */
@Data
public class TDownloadWithFields implements Serializable {
    @Embedded
    private TDownload tDownload;
    /**
     * 影视名称
     */
    private String videoTitle;
    /**
     * 影视所属源
     */
    private int videoSource;
    /**
     * 组下子数据集合大小
     */
    private int downloadDataSize;

    private String filesSize;
    /**
     * 组下子数据有几个未完成数据
     */
    private int noCompleteSize;
    /**
     * 组下子数据下载ID
     */
    private String ariaTaskIds;
}
