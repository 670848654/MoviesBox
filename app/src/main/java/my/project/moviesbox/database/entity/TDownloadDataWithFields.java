package my.project.moviesbox.database.entity;

import androidx.room.Embedded;

import java.io.Serializable;

import lombok.Data;

/**
  * @包名: my.project.moviesbox.database.entity
  * @类名: TDownloadDataWithFields
  * @描述: 子下载列表查询字段实体
  * @作者: Li Z
  * @日期: 2024/2/20 16:10
  * @版本: 1.0
 */
@Data
public class TDownloadDataWithFields implements Serializable {
    @Embedded
    private TDownloadData tDownloadData;
    /**
     * 影视标题
     */
    private String videoTitle;
    /**
     * 影视所属源
     */
    private int videoSource;
    /**
     * 图片地址
     */
    private String videoImgUrl;
}
