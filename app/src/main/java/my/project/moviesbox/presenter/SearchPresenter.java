package my.project.moviesbox.presenter;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.contract.SearchContract;
import my.project.moviesbox.model.SearchModel;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 22:12
 */
public class SearchPresenter extends Presenter<SearchContract.View, SearchModel> implements BasePresenter, SearchContract.LoadDataCallback {
    private SearchContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public SearchPresenter(SearchContract.View view) {
        super(view);
        this.view = view;
        model = new SearchModel();
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void success(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount) {
        view.success(firstTimeData, vodDataBeans, pageCount);
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
     * @param param
     */
    public void loadMainData(boolean isMain, String... param) {
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

    /**
     * 分页时调用
     * @param param
     */
    public void loadPageData(String... param) {
        try {
            model.getData(false, this, param);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
