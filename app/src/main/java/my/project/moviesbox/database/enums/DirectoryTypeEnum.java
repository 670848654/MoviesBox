package my.project.moviesbox.database.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/11/1 10:31
 */
@Getter
@AllArgsConstructor
public enum DirectoryTypeEnum {
    FAVORITE("favorite", "收藏列表目录"),
    DOWNLOAD("download", "下载列表目录");

    private String name;
    private String content;
}
