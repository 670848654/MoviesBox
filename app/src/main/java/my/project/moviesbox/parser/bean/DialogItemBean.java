package my.project.moviesbox.parser.bean;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.parser.config.VodTypeEnum;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/8/17 22:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogItemBean implements Serializable {
    private String url;
    private VodTypeEnum vodTypeEnum;
    private boolean recommendedUse;

    public DialogItemBean(String url, VodTypeEnum vodTypeEnum) {
        this.url = url;
        this.vodTypeEnum = vodTypeEnum;
        this.recommendedUse = false;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
