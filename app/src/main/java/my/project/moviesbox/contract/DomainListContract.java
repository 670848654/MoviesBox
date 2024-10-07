package my.project.moviesbox.contract;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.parser.bean.DomainDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: VodListContract
  * @描述: 域名列表数据相关接口
  * @作者: Li Z
  * @日期: 2024/9/16 14:01
  * @版本: 1.0
 */
public interface DomainListContract {
    interface Model {
        void getData(LoadDataCallback callback) throws UnsupportedEncodingException;
    }

    interface View extends BaseView {
        void success(List<DomainDataBean.Domain> domainList);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<DomainDataBean.Domain> domainList);
    }
}
