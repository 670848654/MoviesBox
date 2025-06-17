package my.project.moviesbox.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/11/1 10:22
 */
@Entity
@Data
public class TDirectory implements Serializable {
    /**
     * 主键
     */
    @PrimaryKey(autoGenerate = true)
    private int index;
    /**
     * 关联ID
     */
    private String id;
    /**
     * 目录名称
     */
    private String name;
    /**
     * 所属源
     */
    private int source;
    /**
     * <p>所属类型</p>
     * @see my.project.moviesbox.database.enums.DirectoryTypeEnum#name
     */
    private String type;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 是否显示配置按钮
     */
    @Ignore
    private boolean showConfigBtn;
}
