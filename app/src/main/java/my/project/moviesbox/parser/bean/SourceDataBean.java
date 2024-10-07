package my.project.moviesbox.parser.bean;

import androidx.annotation.DrawableRes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/8/2 14:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceDataBean {
    /**
     * 标题
     */
    private String title;
    /**
     * 源
     */
    private SourceIndexEnum source;
    /**
     * 源类型
     */
    private String sourceType;
    /**
     * 源背景
     */
    @DrawableRes
    private int sourceBg;
    /**
     * 源描述
     */
    private String sourceInfo;
    /**
     * 是否有弹幕
     */
    private boolean hasDanmu;
    /**
     * 是否有发布页
     */
    private boolean hasWebsiteRelease;
    /**
     * 发布页地址
     */
    private String websiteReleaseUrl;
    /**
     * 是否有RSS订阅
     */
    private boolean hasRss;
    /**
     * RSS订阅地址
     */
    private String rssUrl;
}
