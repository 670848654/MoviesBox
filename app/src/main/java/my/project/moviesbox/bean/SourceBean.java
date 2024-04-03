package my.project.moviesbox.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  * @包名: my.project.moviesbox.bean
  * @类名: SourceBean
  * @描述: 开源列表数据实体
  * @作者: Li Z
  * @日期: 2024/1/24 13:43
  * @版本: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceBean {
    /**
     * 开源库名称
     */
    private String title;
    /**
     * 作者
     */
    private String author;
    /**
     * 描述
     */
    private String desc;
    /**
     * 开源地址
     */
    private String url;
}
