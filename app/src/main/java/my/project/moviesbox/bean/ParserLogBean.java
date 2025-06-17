package my.project.moviesbox.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/4/28 14:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParserLogBean {
    private String dateTime;
    private String content;
}
