package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @包名: my.project.moviesbox.event
  * @类名: RefreshDownloadEvent
  * @描述: 用于本地播放时记录当前播放进度
  * @作者: Li Z
  * @日期: 2024/1/26 15:03
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshDownloadEvent {
    /**
     * 下载ID
     */
    private String id;
    /**
     * 当前播放长度
     */
    private long playPosition;
    /**
     * 视频总长度
     */
    private long videoDuration;
}
