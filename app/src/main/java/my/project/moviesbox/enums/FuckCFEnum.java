package my.project.moviesbox.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/7/17 11:17
 */
@Getter
@AllArgsConstructor
public enum FuckCFEnum {
    HOME, // 首页
    WEEK, // 时间表
    DEFAULT_VOD_LIST, // 默认视频列表
    SEARCH_VOD_LIST, // 搜索视频列表
    CLASSIFICATION_VOD_LIST, // 分类视频列表
    TEXT_LIST, // 文本列表
    TOPTIC_LIST, // 专题列表
    DETAILS, // 视频详情
    VIDEO_URL, // 获取视频地址（播放）
    DOWNLOAD_URL // 获取视频地址（下载）
}
