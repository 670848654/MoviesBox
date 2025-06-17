package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: DownloadContract
  * @描述: 下载列表相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 19:59
  * @版本: 1.0
 */
public interface DownloadContract {
    interface Model {
        void getDownloadList(String directoryId, int offset, int limit, DownloadContract.LoadDataCallback callback);

        void getDownloadDataList(String downloadId, int offset, int limit, DownloadContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void downloadList(List<TDownloadWithFields> list);

        void downloadDataList(List<TDownloadDataWithFields> list);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void downloadList(List<TDownloadWithFields> list);

        void downloadDataList(List<TDownloadDataWithFields> list);
    }
}
