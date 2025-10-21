package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/3/7 13:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshFavoriteEvent {
    /**
     * 影视ID
     */
    private String vodId;
    /**
     * 下载目录ID
     */
    private String downloadId;
    /**
     * 下载数量
     */
    private Integer downloadCount;
    /**
     * 最后观看集数
     */
    private String lastPlayNumber;
}
