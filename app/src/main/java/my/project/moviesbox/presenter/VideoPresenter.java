package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.model.VideoModel;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 16:25
 */
public class VideoPresenter extends Presenter<VideoContract.View, VideoModel> implements BasePresenter, VideoContract.LoadDataCallback {
    private VideoContract.View view;

    public VideoPresenter(VideoContract.View view) {
        super(view);
        this.view = view;
        model = new VideoModel();
    }

    public void loadData(boolean onlyGetPlayUrl, String title, String url, int source, String playNumber) {
        model.getData(onlyGetPlayUrl, title, url, source, playNumber, this);
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void successPlayUrl(List<DialogItemBean> urls) {
        view.cancelDialog();
        view.successPlayUrl(urls);
    }

    @Override
    public void errorPlayUrl() {
        view.cancelDialog();
        view.errorPlayUrl();
    }

    @Override
    public void errorNet(boolean onlyGetPlayUrl, String msg) {
        view.cancelDialog();
        view.errorNet(onlyGetPlayUrl, msg);
    }

    @Override
    public void successDramasList(List<DetailsDataBean.DramasItem> dramasItems) {
        view.successDramasList(dramasItems);
    }

    @Override
    public void errorDramasList() {
        view.errorDramasList();
    }

    @Override
    public void successOnlyPlayUrl(List<DialogItemBean> urls) {
        view.successOnlyPlayUrl(urls);
    }

    @Override
    public void errorOnlyPlayUrl() {
        view.errorOnlyPlayUrl();
    }

    @Override
    public void loadData(boolean isMain) {
    }
}
