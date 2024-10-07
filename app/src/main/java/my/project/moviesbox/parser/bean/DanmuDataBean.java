package my.project.moviesbox.parser.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 弹幕数据实体
 * @date 2024/9/14 10:19
 */
@Data
@NoArgsConstructor
public class DanmuDataBean {
    /**
     * 默认颜色
     */
    public static final String DEFAULT_COLOR = "#FFFFFF";
    /**
     * 弹幕类型： 从右至左滚动弹幕
     */
    public static final int TYPE_ROLL = 1;
    /**
     * 弹幕类型： 顶端固定弹幕
     */
    public static final int TYPE_TOP = 5;
    /**
     * 弹幕类型： 底端固定弹幕
     */
    public static final int TYPE_BOTTOM = 4;
    /**
     * 默认字体大小
     */
    public static final int DEFAULT_TEXT_SIZE = 25;

    /**
     * 弹幕文本
     */
    private String text;
    /**
     * 字体颜色
     */
    private String color;
    /**
     * 弹幕类型
     */
    private int type;
    /**
     * 时间
     */
    private Long time;
    /**
     * 字体大小
     */
    private int textSize;

    public DanmuDataBean(String text, String color, int type, Long time, int textSize) {
        this.text = text;
        this.color = color;
        this.setType(type);
        this.time = time;
        this.textSize = textSize;
    }

    public void setType(int type) {
        if (type != TYPE_ROLL && type != TYPE_TOP && type != TYPE_BOTTOM)
            this.type = TYPE_ROLL;
        else
            this.type = type;
    }
}
