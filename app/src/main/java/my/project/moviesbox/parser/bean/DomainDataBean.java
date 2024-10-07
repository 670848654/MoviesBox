package my.project.moviesbox.parser.bean;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Li
 * @version 1.0
 * @description: 域名数据实体
 * @date 2024/9/16 13:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainDataBean {
    private boolean success;
    private String msg;
    private List<Domain> domainList;

    public DomainDataBean success(List<Domain> domainList) {
        this.success = true;
        this.domainList = domainList;
        return this;
    }

    public DomainDataBean error(String errorMsg) {
        this.success = false;
        this.msg = errorMsg;
        return this;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Domain {
        private String title;
        private String url;
    }
}
