package my.project.moviesbox.presenter;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.contract.TopTicListContract;
import my.project.moviesbox.model.TopticListModel;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 22:12
 */
public class TopticListPresenter extends Presenter<TopTicListContract.View, TopticListModel> implements BasePresenter, TopTicListContract.LoadDataCallback {
    private TopTicListContract.View view;

    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public TopticListPresenter(TopTicListContract.View view) {
        super(view);
        this.view = view;
        model = new TopticListModel();
    }

    @Override
    public void error(String msg) {

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
    public void empty(String msg) {
        view.empty(msg);
    }

    @Override
    public void loadData(boolean firstTimeData) {
    }

    /**
     * 首次调用
     * @param firstTimeData
     * @param url
     * @param isVodList
     * @param page
     */
    public void loadMainData(boolean firstTimeData, String url, boolean isVodList, int page) {
        if (firstTimeData) {
            view.emptyView();
            view.loadingView();
        }
        try {
            model.getData(firstTimeData, url, isVodList, page, this);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 分页时调用
     * @param page
     */
    public void loadPageData(String url, boolean isVodList, int page) {
        try {
            model.getData(false, url, isVodList, page, this);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
