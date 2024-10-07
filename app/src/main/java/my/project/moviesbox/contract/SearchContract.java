package my.project.moviesbox.contract;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.parser.bean.VodDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: SearchContract
  * @描述: 搜索列表数据相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 20:00
  * @版本: 1.0
 */
public interface SearchContract {
    interface Model {
        void getData(boolean firstTimeData, LoadDataCallback callback, String... param) throws UnsupportedEncodingException;
    }

    interface View extends BaseView {
        void success(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount);

        void error(boolean firstTimeData, String msg);

        void empty(boolean firstTimeData, String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount);

        void error(boolean firstTimeData, String msg);
        void empty(boolean firstTimeData, String msg);
    }
}
