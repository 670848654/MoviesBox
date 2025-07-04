package my.project.moviesbox.parser.bean;

import lombok.Data;

/**
 * @author Li
 * @version 1.0
 * @description: 每个站点json数据结构肯定是不一样的，这里统一弹幕数据实体规则
 * @date 2024/9/14 10:19
 */
@Data
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

    private DanmuDataBean() {}

    /**
     * 构造方法
     * @param text      弹幕文本
     * @param color     弹幕颜色
     * @param type      弹幕显示类型
     * @param time      弹幕出现实际那
     */
    public DanmuDataBean(String text, String color, int type, Long time) {
        this.text = text;
        this.color = color;
        this.setType(type);
        this.time = time;
    }

    /**
     * 构造方法
     * @param text      弹幕文本
     * @param color     弹幕颜色
     * @param type      弹幕显示类型
     * @param time      弹幕出现实际那
     * @param textSize  弹幕字体大小
     */
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
