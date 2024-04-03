package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.DownloadVideoContract;
import my.project.moviesbox.model.DownloadVideoModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 13:37
 */
public class DownloadVideoPresenter extends Presenter<DownloadVideoContract.View> implements BasePresenter, DownloadVideoContract.LoadDataCallback {
    private DownloadVideoContract.View view;
    private DownloadVideoModel downloadModel;
    private String url;
    private String playNumber;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public DownloadVideoPresenter(String url, String playNumber, DownloadVideoContract.View view) {
        super(view);
        this.url = url;
        this.playNumber = playNumber;
        this.view = view;
        downloadModel = new DownloadVideoModel();
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void downloadVodUrlSuccess(List<String> urls, String playNumber) {
        view.downloadVodUrlSuccess(urls, playNumber);
    }

    @Override
    public void downloadVodUrlError(String playNumber) {
        view.downloadVodUrlError(playNumber);
    }

    @Override
    public void loadData(boolean isMain) {
        downloadModel.getData(url, playNumber, this);
    }
}
