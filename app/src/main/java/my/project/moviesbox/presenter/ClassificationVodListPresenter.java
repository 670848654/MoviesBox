package my.project.moviesbox.presenter;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.contract.ClassificationVodListContract;
import my.project.moviesbox.model.ClassificationVodListModel;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.VodDataBean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 22:12
 */
public class ClassificationVodListPresenter extends Presenter<ClassificationVodListContract.View> implements BasePresenter, ClassificationVodListContract.LoadDataCallback {
    private ClassificationVodListContract.View view;
    private ClassificationVodListModel model;
    private String[] param;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public ClassificationVodListPresenter(ClassificationVodListContract.View view, String... param) {
        super(view);
        this.view = view;
        this.param = param;
        model = new ClassificationVodListModel();
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void successClassList(List<ClassificationDataBean> classificationDataBeans) {
        view.successClassList(classificationDataBeans);
    }

    @Override
    public void errorClassList(String msg) {
        view.errorClassList(msg);
    }

    @Override
    public void emptyClassList() {
        view.emptyClassList();
    }

    @Override
    public void successVodList(boolean firstTimeData, VodDataBean vodDataBean, int pageCount) {
        view.successVodList(firstTimeData, vodDataBean, pageCount);
    }

    @Override
    public void errorVodList(boolean firstTimeData, String msg) {
        view.errorVodList(firstTimeData, msg);
    }

    @Override
    public void emptyVodList(String msg) {
        view.emptyVodList(msg);
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) {
            view.emptyView();
            view.loadingView();
        }
        try {
            model.getData(isMain, this, param);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
