package my.project.moviesbox.parser.config;

import androidx.annotation.LayoutRes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.R;

/**
 * @author Li
 * @version 1.0
 * @description: 影视item布局枚举
 * @date 2024/7/27 20:33
 */
@Getter
@AllArgsConstructor
public enum VodItemStyleEnum {
    /**
     * 清单列表头部布局
     */
    STYLE_DIRECTORY_HEADER(-1, R.layout.base_header_view),
    /**
     * 布局类型 宽<高[类似：1:1.4]
     */
    STYLE_1_1_DOT_4(0, R.layout.item_list_vod_1_4_1),
    /**
     * 布局类型 宽>高[类似：16:9]
     */
    STYLE_16_9(1, R.layout.item_list_vod_16_9),
    /**
     * 布局类型 宽>高[类似：2:1]
     */
    STYLE_2_1(2, R.layout.item_list_vod_2_1); // 暂无实现

    public int type;
    @LayoutRes
    public int layoutId;
}
