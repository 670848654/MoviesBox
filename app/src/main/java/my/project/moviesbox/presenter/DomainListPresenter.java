package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.DomainListContract;
import my.project.moviesbox.model.DomainListModel;
import my.project.moviesbox.parser.bean.DomainDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/9/16 14:33
 */
public class DomainListPresenter extends Presenter<DomainListContract.View, DomainListModel> implements BasePresenter, DomainListContract.LoadDataCallback {
    private DomainListModel model;
    private DomainListContract.View view;

    public DomainListPresenter(DomainListContract.View view) {
        super(view);
        model = new DomainListModel();
        this.view = view;
    }

    @Override
    public void loadData(boolean firstTimeData) {

    }

    public void loadData() {
        view.loadingView();
        model.getData(this);
    }

    @Override
    public void success(List<DomainDataBean.Domain> domainList) {
        view.success(domainList);
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }
}
