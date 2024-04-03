package my.project.moviesbox.database.manager;

import java.util.List;

import my.project.moviesbox.database.dao.TDownloadDao;
import my.project.moviesbox.database.dao.TDownloadDataDao;
import my.project.moviesbox.database.dao.TVideoDao;
import my.project.moviesbox.database.entity.TDownload;
import my.project.moviesbox.database.entity.TDownloadData;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 16:46
 */
public class TDownloadManager extends BaseManager {
    public static TDownloadDao tDownloadDao = getInstance().tDownloadDao();
    public static TVideoDao tVideoDao = getInstance().tVodDao();
    public static TDownloadDataDao tDownloadDataDao = getInstance().tDownloadDataDao();

    /**
     * 查询所有下载任务
     * @param limit
     * @param offset
     * @return
     */
    public static List<TDownloadWithFields> queryAllDownloads(int limit, int offset) {
        List<TDownloadWithFields> tDownloadWithFields = tDownloadDao.queryAllDownloads(limit, offset);
        for (TDownloadWithFields withFields : tDownloadWithFields) {
            withFields.setFilesSize(Utils.getNetFileSizeDescription(Long.parseLong(withFields.getFilesSize())));
        }
        return tDownloadWithFields;
    }

    /**
     * 更新下载成功数据
     * @param savePath
     * @param ariaTaskId
     * @param videoFileSize
     */
    public static void updateDownloadSuccess(String savePath, long ariaTaskId, long videoFileSize) {
        if (savePath.contains(".m3u8")) {
            savePath = savePath.replaceAll("m3u8", "mp4");
            /*File file = new File(savePath);
            videoFileSize = file.length();*/
        }
        tDownloadDataDao.updateDownloadVideoSuccess(savePath, ariaTaskId, videoFileSize);
    }

    /**
     * 更新下载失败数据
     * @param savePath
     * @param ariaTaskId
     * @param videoFileSize
     */
    public static void updateDownloadError(String savePath, long ariaTaskId, long videoFileSize) {
        tDownloadDataDao.updateDownloadVideoError(savePath, ariaTaskId, videoFileSize);
    }

    /**
     * 新增下载信息
     * @param videoTitle
     * @param imgUrl
     * @param descUrl
     */
    public static void insertDownload(String videoTitle, String imgUrl, String descUrl) {
        String videoId = tVideoDao.queryId(videoTitle, source);
        TDownload tDownload = tDownloadDao.queryByVideoId(videoId);
        if (Utils.isNullOrEmpty(tDownload)) {
            tDownload = new TDownload();
            tDownload.setDownloadId(getUUID());
            tDownload.setLinkId(videoId);
            tDownload.setVideoImgUrl(imgUrl);
            tDownload.setVideoDescUrl(descUrl);
            tDownload.setCreateTime(getDateTimeStr());
            tDownload.setUpdateTime(getDateTimeStr());
            tDownloadDao.insert(tDownload);
        } else {
            tDownload.setUpdateTime(getDateTimeStr());
            tDownloadDao.update(tDownload);
        }
    }

    /**
     * 查询当前下载任务状态
     * @param videoId
     * @param playNumber
     * @param playSource
     * @return
     */
    public static int queryDownloadDataIsDownloadError(String videoId, String playNumber, int playSource) {
        TDownloadData tDownloadData = tDownloadDataDao.queryDownloadDataIsDownloadError(videoId, playNumber, playSource);
        return Utils.isNullOrEmpty(tDownloadData) ? -1 : tDownloadData.getComplete();
    }

    /**
     * 删除下载数据
     * @param id
     */
    public static void deleteDownload(String id) {
        tDownloadDao.deleteDownload(id);
    }

    /**
     * 查询下载列表总数
     * @return
     */
    public static int queryDownloadCount() {
        return tDownloadDao.queryDownloadCount();
    }

    /**
     * 查询下载总数
     * @return
     */
    public static int queryAllDownloadCount() {
        return tDownloadDao.queryAllDownloadCount();
    }

    /**
     * 删除所有下载记录
     */
    public static void deleteAllDownloads() {
        tDownloadDao.deleteAllDownload();
        tDownloadDataDao.deleteAllDownloadData();
    }
}
