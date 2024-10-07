package my.project.moviesbox.database.manager;

import java.util.List;

import my.project.moviesbox.database.dao.TDownloadDao;
import my.project.moviesbox.database.dao.TDownloadDataDao;
import my.project.moviesbox.database.dao.TVideoDao;
import my.project.moviesbox.database.entity.TDownload;
import my.project.moviesbox.database.entity.TDownloadData;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 18:50
 */
public class TDownloadDataManager extends BaseManager {
    public static TDownloadDataDao tDownloadDataDao = getInstance().tDownloadDataDao();
    public static TVideoDao tVideoDao = getInstance().tVodDao();
    public static TDownloadDao tDownloadDao = getInstance().tDownloadDao();
    /**
     * 更新当前播放进度
     * @param position
     * @param duration
     * @param downloadDataId
     */
    public static void updateDownloadDataProgressById(long position, long duration, String downloadDataId) {
        TDownloadData tDownloadData = tDownloadDataDao.queryById(downloadDataId);
        tDownloadData.setWatchProgress(position);
        if (tDownloadData.getVideoDuration() == 0)
            tDownloadData.setVideoDuration(duration);
        tDownloadDataDao.update(tDownloadData);
    }

    /**
     * 新增下载数据
     * @param videoTitle
     * @param playNumber
     * @param playSource
     * @param taskId
     */
    public static void insertDownloadData(String videoTitle, String playNumber, int playSource, long taskId) {
        String videoId = tVideoDao.queryId(videoTitle, source);
        TDownload tDownload = tDownloadDao.queryByVideoId(videoId);
        TDownloadData tDownloadData = tDownloadDataDao.queryByLinkIdVideoUrl(tDownload.getDownloadId(), playNumber);
        if (Utils.isNullOrEmpty(tDownloadData)) {
            tDownloadData = new TDownloadData();
            tDownloadData.setDownloadDataId(getUUID());
            tDownloadData.setLinkId(tDownload.getDownloadId());
            tDownloadData.setVideoNumber(playNumber);
            tDownloadData.setComplete(0);
            tDownloadData.setSavePath("");
            tDownloadData.setVideoFileSize(0);
            tDownloadData.setVideoPlaySource(playSource);
            tDownloadData.setAriaTaskId(taskId);
            tDownloadData.setCreateTime(getDateTimeStr());
            tDownloadData.setWatchProgress(0);
            tDownloadData.setVideoDuration(0);
            tDownloadDataDao.insert(tDownloadData);
        } else
            tDownloadDataDao.updateDownloadVideoInsert(tDownload.getDownloadId(), taskId);
    }

    /**
     * 获取下载的影视下所有剧集总数
     * @param downloadId
     * @return
     */
    public static int queryDownloadDataCount(String downloadId) {
        return tDownloadDataDao.queryDownloadDataCount(downloadId);
    }

    /**
     * 更新下载信息
     * @param taskId
     */
    public static void updateDownloadState(int complete, long taskId) {
        tDownloadDataDao.updateDownloadState(complete, taskId);
    }

    /**
     * 删除下载数据
     * @param id
     */
    public static void deleteDownloadData(String id) {
        tDownloadDataDao.deleteDownloadData(id);
    }

    /**
     * 获取已下载的文件总大小
     * @param downloadId
     * @return
     */
    public static String queryDownloadFilesSize(String downloadId) {
        return tDownloadDataDao.queryDownloadFilesSize(downloadId);
    }

    /**
     * 获取当前任务下未完成的数量
     * @param downloadId
     * @return
     */
    public static int queryDownloadNotCompleteCount(String downloadId) {
        return tDownloadDataDao.queryDownloadNotCompleteCount(downloadId);
    }

    /**
     * 根据下载主表ID查询子表数据
     * @param downloadId
     * @param limit
     * @param offset
     * @return
     */
    public static List<TDownloadDataWithFields> queryDownloadDataByDownloadId(String downloadId, int limit, int offset) {
        return tDownloadDataDao.queryDownloadDataByDownloadId(downloadId, limit, offset);
    }

    /**
     * 获取当前播放进度
     * @param downloadDataId
     * @return
     */
    public static long queryDownloadDataProgressById(String downloadDataId) {
        return tDownloadDataDao.queryDownloadDataProgressById(downloadDataId);
    }
}
