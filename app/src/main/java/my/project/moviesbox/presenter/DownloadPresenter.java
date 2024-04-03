package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.DownloadContract;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.model.DownloadModel;

public class DownloadPresenter extends Presenter<DownloadContract.View> implements BasePresenter, DownloadContract.LoadDataCallback {
    private DownloadContract.View view;
    private DownloadModel model;
    private int offset;
    private int limit;
    private String downloadId;

    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public DownloadPresenter(DownloadContract.View view) {
        super(view);
    }

    /**
     * 下载主目录
     * @param offset
     * @param limit
     * @param view
     */
    public DownloadPresenter(int offset, int limit, DownloadContract.View view) {
        super(view);
        this.view = view;
        this.offset = offset;
        this.limit = limit;
        model = new DownloadModel();
    }

    public void loadDownloadList(boolean isMain) {
        if (isMain)
            view.loadingView();
        model.getDownloadList(offset, limit, this);
    }


    /**
     * 下载子级列表
     * @param downloadId
     * @param offset
     * @param limit
     * @param view
     */
    public DownloadPresenter(String downloadId, int offset, int limit, DownloadContract.View view) {
        super(view);
        this.view = view;
        this.downloadId = downloadId;
        this.offset = offset;
        this.limit = limit;
        model = new DownloadModel();
    }

    public void loadDownloadDataList(boolean isMain) {
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
