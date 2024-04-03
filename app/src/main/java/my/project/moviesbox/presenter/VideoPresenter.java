package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.model.VideoModel;
import my.project.moviesbox.parser.bean.DetailsDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 16:25
 */
public class VideoPresenter extends Presenter<VideoContract.View> implements BasePresenter, VideoContract.LoadDataCallback {
    private VideoContract.View view;
    private VideoModel videoModel;
    private boolean onlyGetPlayUrl;
    private String title;
    private String url;
    private int source;
    private String playNumber;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public VideoPresenter(boolean onlyGetPlayUrl, String title, String url, int source, String playNumber, VideoContract.View view) {
        super(view);
        this.onlyGetPlayUrl = onlyGetPlayUrl;
        this.title = title;
        this.url = url;
        this.source = source;
        this.playNumber = playNumber;
        this.view = view;
        videoModel = new VideoModel();
    }

    public VideoPresenter(String url, VideoContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        videoModel = new VideoModel();
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void successPlayUrl(List<String> urls) {
        view.cancelDialog();
        view.successPlayUrl(urls);
    }

    @Override
    public void errorPlayUrl() {
        view.cancelDialog();
        view.errorPlayUrl();
    }

    @Override
    public void errorNet(String msg) {
        view.cancelDialog();
        view.errorNet(msg);
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
    public void successOnlyPlayUrl(List<String> urls) {
        view.successOnlyPlayUrl(urls);
    }

    @Override
    public void errorOnlyPlayUrl() {
        view.errorOnlyPlayUrl();
    }

    @Override
    public void loadData(boolean isMain) {
        videoModel.getData(onlyGetPlayUrl, title, url, source, playNumber, this);
    }
}
