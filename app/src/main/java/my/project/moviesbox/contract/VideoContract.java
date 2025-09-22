package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: VideoContract
  * @描述: 获取影视剧集播放地址数据相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 20:01
  * @版本: 1.0
 */
public interface VideoContract {
    interface Model {
        void getData(boolean onlyGetPlayUrl, String title, String url, int source, String playNumber, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void cancelDialog();
        void successPlayUrl(List<DialogItemBean> urls);
        void errorPlayUrl();
        void errorNet(boolean onlyGetPlayUrl, String msg);
        void successDramasList(List<DetailsDataBean.DramasItem> dramasItems);
        void errorDramasList();
        void successOnlyPlayUrl(List<DialogItemBean> urls);
        void errorOnlyPlayUrl();
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successPlayUrl(List<DialogItemBean> urls);
        void errorPlayUrl();
        void errorNet(boolean onlyGetPlayUrl, String msg);
        void successDramasList(List<DetailsDataBean.DramasItem> dramasItems);
        void errorDramasList();
        void successOnlyPlayUrl(List<DialogItemBean> urls);
        void errorOnlyPlayUrl();
    }
}
