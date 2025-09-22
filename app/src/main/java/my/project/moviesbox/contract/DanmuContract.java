package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.parser.bean.DanmuDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: DanmuContract
  * @描述: 弹幕数据相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 19:58
  * @版本: 1.0
 */
public interface DanmuContract {

    interface Model {
        void getDanmu(LoadDataCallback callback, String... params);

        void getVipDanmu(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void successDanmuJson(List<DanmuDataBean> danmuDataBeanList);

        void successDanmuXml(String content);

        void errorDanmu(String msg);

        void netErrorDanmu(String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successDanmuJson(List<DanmuDataBean> danmuDataBeanList);

        void successDanmuXml(String content);

        void errorDanmu(String msg);

        void netErrorDanmu(String msg);
    }
}
