package my.project.moviesbox.presenter;

import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.model.ParsingInterfacesModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/21 8:37
 */
public class ParsingInterfacesPresenter extends Presenter<ParsingInterfacesContract.View> implements BasePresenter, ParsingInterfacesContract.LoadDataCallback {
    private String url;
    private ParsingInterfacesContract.View view;
    private ParsingInterfacesModel model;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public ParsingInterfacesPresenter(String url, ParsingInterfacesContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        model = new ParsingInterfacesModel();
    }

    public void parser() {
        view.loadingView();
        model.parser(url, this);
    }

    /**
     * @param msg 错误信息
     * @return
     * @方法名称: error
     * @方法描述: 失败回调方法
     * @日期: 2024/1/22 19:51
     * @作者: Li Z
     */
    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void success(Object object) {
        view.success(object);
    }

    @Override
    public void loadData(boolean isMain) {

    }
}
