package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.parser.bean.TextDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: TextListContract
  * @描述: 文本类数据列表相关接口
  * @作者: Li Z
  * @日期: 2024/1/26 14:57
  * @版本: 1.0
 */
public interface TextListContract {
    interface Model {
        void getData(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void success(List<TextDataBean> textDataBeans);
        void empty(String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<TextDataBean> textDataBeans);
        void empty(String msg);
    }
}
