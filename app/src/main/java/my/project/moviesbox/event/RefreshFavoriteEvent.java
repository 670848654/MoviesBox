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
    private String vodId;
    private String lastPlayNumber;
}
