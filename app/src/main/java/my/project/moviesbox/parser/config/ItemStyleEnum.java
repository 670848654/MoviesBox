package my.project.moviesbox.parser.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li
 * @version 1.0
 * @description: 影视ITEM显示样式枚举
 * @date 2024/7/24 14:51
 */
@Getter
@AllArgsConstructor
public enum ItemStyleEnum {
    /**
     * 布局类型 宽<高[类似：1:1.4]
     */
    STYLE_1_1_DOT_4,
    /**
     * 布局类型 宽>高[类似：16:9]
     */
    STYLE_16_9,
    /**
     * 布局类型 宽>高[类似：2:1]
     */
    STYLE_2_1;
}
