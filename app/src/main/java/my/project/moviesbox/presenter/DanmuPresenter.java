package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.model.DanmuModel;
import my.project.moviesbox.parser.bean.DanmuDataBean;

public class DanmuPresenter extends Presenter<DanmuContract.View, DanmuModel> implements BasePresenter, DanmuContract.LoadDataCallback {
    private DanmuContract.View view;

    public DanmuPresenter(DanmuContract.View view) {
        super(view);
        this.view = view;
        model = new DanmuModel();
    }

    public void loadVipDanmu(String url) {
        model.getVipDanmu(url, this);
    }

    public void loadDanmu(String... params) {
        model.getDanmu(this, params);
    }

    @Override
    public void loadData(boolean isMain) {

    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void successDanmuJson(List<DanmuDataBean> danmuDataBeanList) {
        view.successDanmuJson(danmuDataBeanList);
    }

    @Override
    public void successDanmuXml(String content) {
        view.successDanmuXml(content);
    }

    @Override
    public void errorDanmu(String msg) {
        view.errorDanmu(msg);
    }

    @Override
    public void netErrorDanmu(String msg) {
        view.netErrorDanmu(msg);
    }
}
