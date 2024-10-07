package my.project.moviesbox.parser.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/7/22 9:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayUpdateBean {
    /**
     * 标题
     */
    private String title;
    /**
     * 更新内容
     */
    private String info;
    /**
     * 更新时间
     */
    @Deprecated
    private String updateTime;
    /**
     * 链接
     */
    private String url;
}
