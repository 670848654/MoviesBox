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
public class FavoritePresenter extends Presenter<FavoriteContract.View, FavoriteModel> implements BasePresenter, FavoriteContract.LoadDataCallback {
    private FavoriteContract.View view;

    public FavoritePresenter(FavoriteContract.View view) {
        super(view);
        this.view = view;
        model = new FavoriteModel();
    }

    public void loadData(boolean isMain, String directoryId, int offset, int limit, boolean updateOrder) {
        if (isMain)
            view.loadingView();
        model.getData(directoryId, offset, limit, updateOrder, this);
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
    public void loadData(boolean isMain) {}
}
