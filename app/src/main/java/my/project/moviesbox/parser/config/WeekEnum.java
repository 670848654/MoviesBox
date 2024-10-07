package my.project.moviesbox.parser.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.R;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.parser.config
  * @类名: WeekEnum
  * @描述: 星期枚举类
  * @作者: Li Z
  * @日期: 2024/1/22 19:16
  * @版本: 1.0
 */
@Getter
@AllArgsConstructor
public enum WeekEnum {
    MONDAY(0, Utils.getString(R.string.monday)),
    TUESDAY(1, Utils.getString(R.string.tuesday)),
    WEDNESDAY(2, Utils.getString(R.string.wednesday)),
    THURSDAY(3, Utils.getString(R.string.thursday)),
    FRIDAY(4, Utils.getString(R.string.friday)),
    SATURDAY(5, Utils.getString(R.string.saturday)),
    SUNDAY(6, Utils.getString(R.string.sunday));

    private int index;
    private String content;
}
