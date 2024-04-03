package my.project.moviesbox.bean;

import androidx.annotation.DrawableRes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/27 9:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingAboutBean {
    @DrawableRes
    private int icon; // 图标
    private String title; // 标题
    private String subTitle; // 子标题
    private Object[] option; // 选项
    @DrawableRes
    private int endIcon; // 右侧图标

    public SettingAboutBean(int icon, String title, String subTitle) {
        this.icon = icon;
        this.title = title;
        this.subTitle = subTitle;
    }
}
