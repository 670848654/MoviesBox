package my.project.moviesbox.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/6/30 10:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuBean {
    /**
     * 标题
     */
    private String title;
    /**
     * 值
     */
    private Object value;
    /**
     * 是否选中
     */
    private boolean selected;

    @Getter
    @AllArgsConstructor
    public enum MenuEnum {
        SPEED,
        DISPLAY
    }
}
