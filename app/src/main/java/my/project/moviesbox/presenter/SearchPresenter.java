package my.project.moviesbox.presenter;

import java.io.UnsupportedEncodingException;

import my.project.moviesbox.contract.SearchContract;
import my.project.moviesbox.model.SearchModel;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 22:12
 */
public class SearchPresenter extends Presenter<SearchContract.View> implements BasePresenter, SearchContract.LoadDataCallback {
    private SearchContract.View view;
    private SearchModel model;
    private String[] param;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public SearchPresenter(SearchContract.View view, String... param) {
        super(view);
        this.view = view;
        this.param = param;
        model = new SearchModel();
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
            model.getData(isMain, this, param);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
