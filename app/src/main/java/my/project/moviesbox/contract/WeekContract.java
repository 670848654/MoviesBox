package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.parser.bean.WeekDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: WeekContract
  * @描述: 用于动漫星期时间表数据列表相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 20:01
  * @版本: 1.0
 */
public interface WeekContract  {
    interface Model {
        void getWeekData(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void weekSuccess(List<WeekDataBean> weekDataBeans);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void weekSuccess(List<WeekDataBean> weekDataBeans);
    }
}
