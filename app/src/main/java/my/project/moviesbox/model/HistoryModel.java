package my.project.moviesbox.model;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.HistoryContract;
import my.project.moviesbox.database.entity.THistoryWithFields;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/3 22:47
 */
public class HistoryModel extends BaseModel implements HistoryContract.Model {
    @Override
    public void getData(int offset, int limit, HistoryContract.LoadDataCallback callback) {
        List<THistoryWithFields> list = THistoryManager.queryAllHistory(limit, offset);
        if (list.size() > 0)
            callback.success(list);
        else
            callback.error(Utils.getString(R.string.emptyMyList));
    }
}
