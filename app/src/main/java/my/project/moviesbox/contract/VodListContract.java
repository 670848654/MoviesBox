package my.project.moviesbox.contract;

import java.io.UnsupportedEncodingException;

import my.project.moviesbox.parser.bean.VodDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: VodListContract
  * @描述: 影视列表数据相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 20:01
  * @版本: 1.0
 */
public interface VodListContract {
    interface Model {
        void getData(boolean firstTimeData, String url, int page, LoadDataCallback callback) throws UnsupportedEncodingException;
    }

    interface View extends BaseView {
        void success(boolean firstTimeData, VodDataBean vodDataBean, int pageCount);
        void error(boolean firstTimeData, String msg);
        void empty(String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(boolean firstTimeData, VodDataBean vodDataBean, int pageCount);
        void error(boolean firstTimeData, String msg);
        void empty(String msg);
    }
}
