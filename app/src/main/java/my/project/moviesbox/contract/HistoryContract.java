package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.database.entity.THistoryWithFields;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: HistoryContract
  * @描述: 历史记录列表相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 20:00
  * @版本: 1.0
 */
public interface HistoryContract {
    interface Model {
        void getData(int offset, int limit, HistoryContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void success(List<THistoryWithFields> list);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<THistoryWithFields> list);
    }
}
