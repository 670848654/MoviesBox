package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.parser.bean.DialogItemBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: DownloadVideoContract
  * @描述: 获取下载链接相关接口
  * @作者: Li Z
  * @日期: 2024/1/26 14:56
  * @版本: 1.0
 */
public interface DownloadVideoContract {
    interface Model {
        void getData(String url, String playNumber, DownloadVideoContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void downloadVodUrlSuccess(List<DialogItemBean> urls, String playNumber);

        void downloadVodUrlError(String playNumber);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void downloadVodUrlSuccess(List<DialogItemBean> urls, String playNumber);

        void downloadVodUrlError(String playNumber);
    }
}
