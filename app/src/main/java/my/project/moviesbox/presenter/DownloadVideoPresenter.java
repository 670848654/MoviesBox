package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.DownloadVideoContract;
import my.project.moviesbox.model.DownloadVideoModel;
import my.project.moviesbox.parser.bean.DialogItemBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 13:37
 */
public class DownloadVideoPresenter extends Presenter<DownloadVideoContract.View, DownloadVideoModel> implements BasePresenter, DownloadVideoContract.LoadDataCallback {
    private DownloadVideoContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public DownloadVideoPresenter(DownloadVideoContract.View view) {
        super(view);
        this.view = view;
        model = new DownloadVideoModel();
    }

    public void loadData(String url, String playNumber) {
        model.getData(url, playNumber, this);
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void downloadVodUrlSuccess(List<DialogItemBean> urls, String playNumber) {
        view.downloadVodUrlSuccess(urls, playNumber);
    }

    @Override
    public void downloadVodUrlError(String playNumber) {
        view.downloadVodUrlError(playNumber);
    }

    @Override
    public void loadData(boolean isMain) {}
}
