package my.project.moviesbox.contract;

import my.project.moviesbox.parser.bean.DetailsDataBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: DetailsContract
  * @描述: 影视详情相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 19:58
  * @版本: 1.0
 */
public interface DetailsContract {
    interface Model {
        void getData(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void success(DetailsDataBean detailsDataBean);
        void favorite(boolean favorite);
        void getVodId(String id);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(DetailsDataBean detailsDataBean);
        void favorite(boolean favorite);
        void getVodId(String id);
    }
}
