package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.parser.bean.MainDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: HomeContract
  * @描述: 首页数据列表相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 20:00
  * @版本: 1.0
 */
public interface HomeContract {

    interface Model {
        /**
         * 获取数据
         * @param loadDataCallback
         */
        void getData(LoadDataCallback loadDataCallback);
    }

    interface View extends BaseView {
        /**
         * 成功视图
         * @param mainDataBeans
         */
        void success(List<MainDataBean> mainDataBeans);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        /**
         * 成功回调
         * @param mainDataBeans
         */
        void success(List<MainDataBean> mainDataBeans);
    }
}
