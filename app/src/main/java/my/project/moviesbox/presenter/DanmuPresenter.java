package my.project.moviesbox.presenter;

import com.alibaba.fastjson.JSONObject;

import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.model.DanmuModel;

public class DanmuPresenter extends Presenter<DanmuContract.View> implements BasePresenter, DanmuContract.LoadDataCallback {
    private String[] params;
    private DanmuModel model;
    private DanmuContract.View view;

    public DanmuPresenter(DanmuContract.View view, String... params) {
        super(view);
        this.view = view;
        this.params = params;
        model = new DanmuModel();
    }

    public void loadDanmu() {
        model.getDanmu(this, params);
    }

    @Override
    public void loadData(boolean isMain) {

    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void successDanmuJson(JSONObject danmus) {
        view.successDanmuJson(danmus);
    }

    @Override
    public void successDanmuXml(String content) {
        view.successDanmuXml(content);
    }

    @Override
    public void errorDanmu(String msg) {
        view.errorDanmu(msg);
    }
}
