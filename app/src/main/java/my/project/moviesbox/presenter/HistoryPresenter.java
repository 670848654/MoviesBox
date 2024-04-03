package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.HistoryContract;
import my.project.moviesbox.database.entity.THistoryWithFields;
import my.project.moviesbox.model.HistoryModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/3 22:49
 */
public class HistoryPresenter extends Presenter<HistoryContract.View> implements BasePresenter, HistoryContract.LoadDataCallback {
    private HistoryContract.View view;
    private HistoryModel model;
    private int offset;
    private int limit;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public HistoryPresenter(int offset, int limit, HistoryContract.View view) {
        super(view);
        this.view = view;
        this.offset = offset;
        this.limit = limit;
        model = new HistoryModel();
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void success(List<THistoryWithFields> list) {
        view.success(list);
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.loadingView();
        model.getData(offset, limit, this);
    }
}
