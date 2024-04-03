package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @包名: my.project.moviesbox.event
  * @类名: DramaEvent
  * @描述: 在视频播放列表选集时通知详情页当前播放源>播放集数的数据点击刷新适配器ITEM
  * @作者: Li Z
  * @日期: 2024/1/26 15:01
  * @版本: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DramaEvent {
    /**
     * 当前播放源
     */
    private int nowSource;
    /**
     * 当前播放的集数下标
     */
    private int clickIndex;
}
