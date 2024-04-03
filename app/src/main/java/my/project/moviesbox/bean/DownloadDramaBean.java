package my.project.moviesbox.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @包名: my.project.moviesbox.bean
  * @类名: DownloadDramaBean
  * @描述: 影视详情下载列表实体
  * @作者: Li Z
  * @日期: 2024/1/22 19:39
  * @版本: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadDramaBean {
    /**
     * 集数名称
     */
    private String title;
    /**
     * 跳转地址
     */
    private String url;
    /**
     * 是否选中 用于多选
     * @deprecated 不再支持多选下载
     */
    @Deprecated
    private boolean selected;
    /**
     * 用于判断集数是否已经下载
     * true 存在下载任务
     * false 不存在下载任务
     */
    private boolean hasDownload;
}
