package my.project.moviesbox.contract;

import java.util.List;

import my.project.moviesbox.database.entity.TFavoriteWithFields;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: FavoriteContract
  * @描述: 收藏夹列表数据相关接口
  * @作者: Li Z
  * @日期: 2024/1/22 19:59
  * @版本: 1.0
 */
public interface FavoriteContract {
    interface Model {
        void getData(String directoryId, int offset, int limit, boolean updateOrder, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void favoriteList(List<TFavoriteWithFields> list);

        void completion(boolean complete);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void favoriteList(List<TFavoriteWithFields> list);

        void completion(boolean complete);
    }

}
