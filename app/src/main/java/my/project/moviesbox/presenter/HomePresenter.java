package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.HomeContract;
import my.project.moviesbox.model.HomeModel;
import my.project.moviesbox.parser.bean.MainDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/30 22:58
 */
public class HomePresenter extends Presenter<HomeContract.View, HomeModel> implements BasePresenter, HomeContract.LoadDataCallback {
    private HomeContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public HomePresenter(HomeContract.View view) {
        super(view);
        this.view = view;
        model = new HomeModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.loadingView();
        model.getData(this);
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void success(List<MainDataBean> mainDataBeans) {
        view.success(mainDataBeans);
    }
}
