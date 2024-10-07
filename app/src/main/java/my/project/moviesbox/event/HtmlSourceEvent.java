package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/7/17 9:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HtmlSourceEvent {
    private String source;
    private String type;
}
