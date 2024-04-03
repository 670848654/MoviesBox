package my.project.moviesbox.presenter;

import java.io.UnsupportedEncodingException;

import my.project.moviesbox.contract.VodListContract;
import my.project.moviesbox.model.VodListModel;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 22:12
 */
public class VodListPresenter extends Presenter<VodListContract.View> implements BasePresenter, VodListContract.LoadDataCallback {
    private VodListContract.View view;
    private VodListModel model;
    private String url;
    private int page;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public VodListPresenter(String url, int page, VodListContract.View view) {
        super(view);
        this.url = url;
        this.page = page;
        this.view = view;
        model = new VodListModel();
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void success(boolean firstTimeData, VodDataBean vodDataBean, int pageCount) {
        view.success(firstTimeData, vodDataBean, pageCount);
    }

    @Override
    public void error(boolean firstTimeData, String msg) {
        view.error(firstTimeData, msg);
    }

    @Override
    public void empty(String msg) {
        view.empty(msg);
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) {
            view.emptyView();
            view.loadingView();
        }
        try {
            model.getData(isMain, url, page, this);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
