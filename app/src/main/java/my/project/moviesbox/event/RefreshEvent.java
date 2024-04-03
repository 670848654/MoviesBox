package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @包名: my.project.moviesbox.event
  * @类名: RefreshEvent
  * @描述: 用于刷新相关位置的数据
  * @作者: Li Z
  * @日期: 2024/1/26 15:05
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshEvent {
    /**
     * 数据标识
     * -1 换源
     * 0 刷新首页
     * 1 刷新收藏
     * 2 刷新历史记录
     * 3 刷新下载记录
     * 4 刷新我的TAB收藏、历史、下载数量
     */
    private int index;
}
