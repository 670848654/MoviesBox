package my.project.moviesbox.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import my.project.moviesbox.database.dao.TDirectoryDao;
import my.project.moviesbox.database.dao.TDownloadDao;
import my.project.moviesbox.database.dao.TDownloadDataDao;
import my.project.moviesbox.database.dao.TFavoriteDao;
import my.project.moviesbox.database.dao.THistoryDao;
import my.project.moviesbox.database.dao.THistoryDataDao;
import my.project.moviesbox.database.dao.TVideoDao;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.entity.TDownload;
import my.project.moviesbox.database.entity.TDownloadData;
import my.project.moviesbox.database.entity.TFavorite;
import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.database.entity.TVideo;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 11:16
 */
@Database(entities = {TVideo.class, TFavorite.class, THistory.class, THistoryData.class, TDownload.class, TDownloadData.class, TDirectory.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TVideoDao tVodDao();
    public abstract TFavoriteDao tFavoriteDao();
    public abstract THistoryDao tHistoryDao();
    public abstract THistoryDataDao tHistoryDataDao();
    public abstract TDownloadDao tDownloadDao();
    public abstract TDownloadDataDao tDownloadDataDao();
    public abstract TDirectoryDao tDirectoryDao();
}