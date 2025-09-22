package my.project.moviesbox.event;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.parser.bean.DialogItemBean;

/**
 * @author Li
 * @version 1.0
 * @description: 视频嗅探
 * @date 2024/5/30 15:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoSniffEvent {
    private String vodId;
    private ActivityEnum activityEnum;
    private SniffEnum sniffEnum;
    /**
     * 是否成功获取资源内容
     */
    private boolean isSuccess;
    /**
     * 成功/失败匹配到的所有链接集合
     */
    private List<DialogItemBean> urls;
    /**
     * 集数名称
     */
    private String dramaTitle;

    public VideoSniffEvent(String vodId, ActivityEnum activityEnum, SniffEnum sniffEnum, boolean isSuccess, List<DialogItemBean> urls) {
        this.vodId = vodId;
        this.activityEnum = activityEnum;
        this.sniffEnum = sniffEnum;
        this.isSuccess = isSuccess;
        this.urls = urls;
    }

    /**
     * EventBus订阅处理判断枚举
     */
    public enum ActivityEnum {
        /**
         * 详情界面
         */
        DETAIL,
        /**
         * 播放界面
         */
        PLAYER,
        /**
         * 历史记录界面
         */
        HISTORY
    }

    /**
     * 嗅探结果处理类型枚举
     */
    public enum SniffEnum {
        /**
         * 用于播放
         */
        PLAY,
        /**
         * 用于获取下一集
         */
        NEXT_PLAY,
        /**
         * 用于下载
         */
        DOWNLOAD,
    }
}
