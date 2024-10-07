package my.project.moviesbox.presenter;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.contract.VodListContract;
import my.project.moviesbox.model.VodListModel;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 22:12
 */
public class VodListPresenter extends Presenter<VodListContract.View, VodListModel> implements BasePresenter, VodListContract.LoadDataCallback {
    private VodListContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public VodListPresenter(VodListContract.View view) {
        super(view);
        this.view = view;
        model = new VodListModel();
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void success(boolean firstTimeData, List<VodDataBean> vodDataBean, int pageCount) {
        view.success(firstTimeData, vodDataBean, pageCount);
    }

    @Override
    public void error(boolean firstTimeData, String msg) {
        view.error(firstTimeData, msg);
    }

    @Override
    public void empty(boolean firstTimeData, String msg) {
        view.empty(firstTimeData, msg);
    }

    @Override
    public void loadData(boolean isMain) {}

    /**
     * 首次调用
     * @param isMain
     * @param url
     * @param page
     */
    public void loadMainData(boolean isMain, String url, int page) {
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

    /**
     * 分页时调用
     * @param page
     */
    public void loadPageData(String url, int page) {
        try {
            model.getData(false, url, page, this);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
