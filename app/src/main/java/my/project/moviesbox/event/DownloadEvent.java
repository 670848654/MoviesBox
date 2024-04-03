package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @包名: my.project.moviesbox.event
  * @类名: DownloadEvent
  * @描述: 用于通知下载完成刷新下载列表数据适配器
  * @作者: Li Z
  * @日期: 2024/1/26 14:59
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadEvent {
    /**
     * 任务ID
     */
    private long taskId;
    /**
     * 影视名称
     */
    private String title;
    /**
     * 集数
     */
    private String drama;
    /**
     * 文件保存地址
     */
    private String filePath;
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * 下载状态
     */
    private int complete;
}
