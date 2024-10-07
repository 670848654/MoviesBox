package my.project.moviesbox.contract;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.parser.bean.VodDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: TopTicListContract
  * @描述: 用于动漫专题数据列表相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 20:00
  * @版本: 1.0
 */
public interface TopTicListContract {
    interface Model {
        void getData(boolean firstTimeData, String url, boolean isVodList, int page, LoadDataCallback callback) throws UnsupportedEncodingException;
    }

    interface View extends BaseView {
        void success(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount);
        void error(boolean firstTimeData, String msg);
        void empty(String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(boolean firstTimeData, List<VodDataBean>  vodDataBeans, int pageCount);
        void error(boolean firstTimeData, String msg);
        void empty(String msg);
    }
}
