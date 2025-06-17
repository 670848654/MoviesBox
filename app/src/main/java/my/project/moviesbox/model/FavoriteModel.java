package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.FavoriteContract;
import my.project.moviesbox.database.entity.TFavoriteWithFields;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/3 1:09
 */
public class FavoriteModel extends BaseModel implements FavoriteContract.Model {
    @Override
    public void getData(String directoryId, int offset, int limit, boolean updateOrder, FavoriteContract.LoadDataCallback callback) {
        List<TFavoriteWithFields> list = TFavoriteManager.queryFavorite(directoryId, offset, limit, updateOrder);
        if (list.size() > 0)
            callback.favoriteList(list);
        else
            callback.error(Utils.getString(R.string.emptyMyList));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
