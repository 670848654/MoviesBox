package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.DownloadContract;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.utils.Utils;

public class DownloadModel extends BaseModel implements DownloadContract.Model {
    @Override
    public void getDownloadList(String directoryId, int offset, int limit, DownloadContract.LoadDataCallback callback) {
        List<TDownloadWithFields> list = TDownloadManager.queryAllDownloads(directoryId, limit, offset);
        if (list.size() > 0)
            callback.downloadList(list);
        else
            callback.error(Utils.getString(R.string.emptyMyList));
    }

    @Override
    public void getDownloadDataList(String downloadId, int offset, int limit, DownloadContract.LoadDataCallback callback) {
        List<TDownloadDataWithFields> list = TDownloadDataManager.queryDownloadDataByDownloadId(downloadId, limit, offset);
        callback.downloadDataList(list);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
