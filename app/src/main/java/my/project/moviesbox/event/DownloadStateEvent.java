package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/9/4 16:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadStateEvent {
    /**
     * 任务ID
     */
    private long taskId;
    /**
     * 集数
     */
    private String drama;
    /** 下载状态 **/
    private int complete;
}
