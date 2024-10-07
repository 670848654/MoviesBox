package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.TextListContract;
import my.project.moviesbox.model.TextListModel;
import my.project.moviesbox.parser.bean.TextDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/24 15:54
 */
public class TextListPresenter extends Presenter<TextListContract.View, TextListModel> implements BasePresenter, TextListContract.LoadDataCallback {
    private TextListContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public TextListPresenter(TextListContract.View view) {
        super(view);
        this.view = view;
        model = new TextListModel();
    }

    public void loadData(String url) {
        view.emptyView();
        view.loadingView();
        model.getData(url, this);
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void success(List<TextDataBean> textDataBeans) {
        view.success(textDataBeans);
    }

    @Override
    public void empty(String msg) {
        view.empty(msg);
    }

    @Override
    public void loadData(boolean isMain) {
    }
}
