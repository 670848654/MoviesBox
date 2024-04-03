package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @包名: my.project.moviesbox.event
  * @类名: UpdateImgEvent
  * @描述: 用于通知图片更新
  * @作者: Li Z
  * @日期: 2024/2/20 14:18
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImgEvent {
    private String oldImgUrl; // 旧图片地址
    private String descUrl; // 影视详情地址
}
