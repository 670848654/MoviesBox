package my.project.moviesbox.contract;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: ClassificationVodListContract
  * @描述: 分类列表相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 19:52
  * @版本: 1.0
 */
public interface ClassificationVodListContract {
    interface Model {
        void getData(boolean firstTimeData, LoadDataCallback callback, String... params) throws UnsupportedEncodingException;
    }

    interface View extends BaseView {
        void successClassList(List<ClassificationDataBean> classificationDataBeans);
        void errorClassList(String msg);
        void emptyClassList();
        void successVodList(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount);
        void errorVodList(boolean firstTimeData, String msg);
        void emptyVodList(boolean firstTimeData, String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successClassList(List<ClassificationDataBean> classificationDataBeans);
        void errorClassList(String msg);
        void emptyClassList();
        void successVodList(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount);
        void errorVodList(boolean firstTimeData, String msg);
        void emptyVodList(boolean firstTimeData, String msg);
    }
}
