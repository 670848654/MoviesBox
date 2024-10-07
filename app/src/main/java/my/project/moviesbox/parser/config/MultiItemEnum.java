package my.project.moviesbox.parser.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li
 * @version 1.0
 * @description: 数据列表ITEM样式枚举
 * @date 2024/7/24 16:54
 */
@Getter
@AllArgsConstructor
public enum MultiItemEnum {
    /**
     * 首页TAG
     */
    TAG_LIST(0),
    /**
     * 轮播
     */
    BANNER_LIST(1),
    /**
     * 剧集列表 横向列表
     */
    ITEM_LIST(2),
    /**
     * 单条数据列表 纵向
     */
    ITEM_SINGLE_LINE_LIST(3),
    /**
     * 多条剧集列表 纵向
     */
    VOD_LIST(4),
    /**
     * 点击tag弹出menu菜单的类型
     */
    DROP_DOWN_TAG(5);
    ;

    private int type;
}
