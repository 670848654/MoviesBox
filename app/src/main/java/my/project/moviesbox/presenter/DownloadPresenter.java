package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.DownloadContract;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.model.DownloadModel;

public class DownloadPresenter extends Presenter<DownloadContract.View, DownloadModel> implements BasePresenter, DownloadContract.LoadDataCallback {
    private DownloadContract.View view;

    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public DownloadPresenter(DownloadContract.View view) {
        super(view);
        this.view = view;
        model = new DownloadModel();
    }

    /**
     * 下载主目录
     * @param isMain
     * @param offset
     * @param limit
     */
    public void loadDownloadList(boolean isMain, int offset, int limit) {
        if (isMain)
            view.loadingView();
        model.getDownloadList(offset, limit, this);
    }

    /**
     * 下载子级列表
     * @param isMain
     * @param downloadId
     * @param offset
     * @param limit
     */
    public void loadDownloadDataList(boolean isMain, String downloadId, int offset, int limit) {
        if (isMain)
            view.loadingView();
        model.getDownloadDataList(downloadId, offset, limit, this);
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void downloadList(List<TDownloadWithFields> list) {
        view.downloadList(list);
    }

    @Override
    public void downloadDataList(List<TDownloadDataWithFields> list) {
        view.downloadDataList(list);
    }

    @Override
    public void loadData(boolean isMain) {

    }
}
