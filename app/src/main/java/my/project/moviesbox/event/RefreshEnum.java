package my.project.moviesbox.event;

import lombok.Getter;

/**
  * @包名: my.project.moviesbox.event
  * @类名: RefreshEvent
  * @描述: 用于刷新相关位置的数据
  * @作者: Li Z
  * @日期: 2024/1/26 15:05
  * @版本: 1.0
 */
@Getter
public enum RefreshEnum {
    /** 换源 **/
    CHANGE_SOURCES,
    /** 刷新首页 **/
    REFRESH_INDEX,
    /** 刷新收藏 **/
    REFRESH_FAVORITE,
    /** 刷新历史记录 **/
    REFRESH_HISTORY,
    /** 刷新下载记录 **/
    REFRESH_DOWNLOAD,
    /** 刷新我的TAB收藏、历史、下载数量 **/
    REFRESH_TAB_COUNT,
    /** 刷新设置界面播放器内核 **/
    REFRESH_PLAYER_KERNEL,
    /** 开启隐藏功能 **/
    REFRESH_ON_HIDDEN_FEATURES,
    /** 关闭隐藏功能 **/
    REFRESH_OFF_HIDDEN_FEATURES
}
