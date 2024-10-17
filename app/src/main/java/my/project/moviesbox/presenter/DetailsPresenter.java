package my.project.moviesbox.presenter;

import my.project.moviesbox.contract.DetailsContract;
import my.project.moviesbox.model.DetailsModel;
import my.project.moviesbox.parser.bean.DetailsDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 13:13
 */
public class DetailsPresenter extends Presenter<DetailsContract.View, DetailsModel> implements BasePresenter, DetailsContract.LoadDataCallback {
    private DetailsContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public DetailsPresenter(DetailsContract.View view) {
        super(view);
        this.view = view;
        model = new DetailsModel();
    }

    @Override
    public void loadData(boolean isMain) {
    }

    public void loadData(boolean isMain, String url) {
        if (isMain)
            view.loadingView();
        model.getData(url, this);
    }


    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void success(DetailsDataBean detailsDataBean) {
        view.success(detailsDataBean);
    }

    @Override
    public void favorite(boolean favorite) {
        view.favorite(favorite);
    }

    @Override
    public void getVodId(String id) {
        view.getVodId(id);
    }
}
