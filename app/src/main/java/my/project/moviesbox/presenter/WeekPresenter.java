package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.WeekContract;
import my.project.moviesbox.model.WeekModel;
import my.project.moviesbox.parser.bean.WeekDataBean;

public class WeekPresenter extends Presenter<WeekContract.View> implements BasePresenter, WeekContract.LoadDataCallback {
    private String url;
    private WeekContract.View view;
    private WeekModel model;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public WeekPresenter(String url, WeekContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        model = new WeekModel();
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void weekSuccess(List<WeekDataBean> weekDataBeans) {
        view.weekSuccess(weekDataBeans);
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.loadingView();
        model.getWeekData(url, this);
    }
}
