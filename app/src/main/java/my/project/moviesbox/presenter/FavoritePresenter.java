package my.project.moviesbox.presenter;

import java.util.List;

import my.project.moviesbox.contract.FavoriteContract;
import my.project.moviesbox.database.entity.TFavoriteWithFields;
import my.project.moviesbox.model.FavoriteModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/3 1:10
 */
public class FavoritePresenter extends Presenter<FavoriteContract.View> implements BasePresenter, FavoriteContract.LoadDataCallback {
    private FavoriteContract.View view;
    private FavoriteModel model;
    private int offset;
    private int limit;
    private boolean updateOrder;

    public FavoritePresenter(int offset, int limit, boolean updateOrder, FavoriteContract.View view) {
        super(view);
        this.view = view;
        this.offset = offset;
        this.limit = limit;
        this.updateOrder = updateOrder;
        model = new FavoriteModel();
    }

    @Override
    public void error(String msg) {
        view.errorView(msg);
    }

    @Override
    public void favoriteList(List<TFavoriteWithFields> list) {
        view.favoriteList(list);
    }

    @Override
    public void completion(boolean complete) {
        view.completion(complete);
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) 
            view.loadingView();
        model.getData(offset, limit, updateOrder, this);
    }
}
