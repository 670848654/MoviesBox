package my.project.moviesbox.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/3/26 9:16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckUpdateEvent {
    private boolean hasUpdate;
    private String msg;
    private String versionTitle;
    private String versionContent;
    private String releaseUrl;

    public CheckUpdateEvent fail(String msg) {
        hasUpdate = false;
        this.msg = msg;
        return this;
    }

    public CheckUpdateEvent success(String msg, String versionTitle, String versionContent, String releaseUrl) {
        hasUpdate = true;
        this.msg = msg;
        this.versionTitle = versionTitle;
        this.versionContent = versionContent;
        this.releaseUrl = releaseUrl;
        return this;
    }
}
