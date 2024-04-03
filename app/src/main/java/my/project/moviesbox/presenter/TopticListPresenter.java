package my.project.moviesbox.presenter;

import java.io.UnsupportedEncodingException;

import my.project.moviesbox.contract.TopTicListContract;
import my.project.moviesbox.model.TopticListModel;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 22:12
 */
public class TopticListPresenter extends Presenter<TopTicListContract.View> implements BasePresenter, TopTicListContract.LoadDataCallback {
    private TopTicListContract.View view;
    private TopticListModel model;
    private String url;
    private int page;
    private boolean isVodList;

    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public TopticListPresenter(String url, boolean isVodList, int page, TopTicListContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        this.isVodList = isVodList;
        this.page = page;
        model = new TopticListModel();
    }

    @Override
    public void error(String msg) {

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
    public void loadData(boolean firstTimeData) {
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
}
