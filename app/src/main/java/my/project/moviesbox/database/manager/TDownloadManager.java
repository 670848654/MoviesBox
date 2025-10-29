package my.project.moviesbox.database.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.database.dao.TDirectoryDao;
import my.project.moviesbox.database.dao.TDownloadDao;
import my.project.moviesbox.database.dao.TDownloadDataDao;
import my.project.moviesbox.database.dao.TVideoDao;
import my.project.moviesbox.database.entity.TDownload;
import my.project.moviesbox.database.entity.TDownloadData;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
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
    public static TDirectoryDao tDirectoryDao = getInstance().tDirectoryDao();

    /**
     * 查询所有下载任务
     * @param limit
     * @param offset
     * @return
     */
    public static List<TDownloadWithFields> queryAllDownloads(String directoryId, int limit, int offset) {
        List<TDownloadWithFields> tDownloadWithFields = tDownloadDao.queryAllDownloads(directoryId, limit, offset);
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
     * 通过影视名称获取下载记录
     * @param videoTitle
     * @return
     */
    public static TDownload queryByVideoTitle(String videoTitle) {
        String videoId = tVideoDao.queryId(videoTitle, source);
        return tDownloadDao.queryByVideoId(videoId);
    }

    /**
     * 新增下载信息
     * @param videoTitle
     * @param imgUrl
     * @param descUrl
     */
    public static String insertDownload(String videoTitle, String imgUrl, String descUrl, String directoryId) {
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
            tDownload.setDirectoryId(Utils.isNullOrEmpty(directoryId) ? null : directoryId);
            tDownloadDao.insert(tDownload);
        } else {
            tDownload.setUpdateTime(getDateTimeStr());
            tDownloadDao.update(tDownload);
        }
        return tDownload.getDownloadId();
    }

    /**
     * 更新下载清单位置
     * @param downloadId
     * @param directoryId
     */
    public static void updateDownloadDirectoryId(String downloadId, String directoryId) {
        TDownload tDownload = tDownloadDao.queryByDownloadId(downloadId);
        if (!Utils.isNullOrEmpty(tDownload)) {
            tDownload.setDirectoryId(Utils.isNullOrEmpty(directoryId) ? null : directoryId);
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
     * 根据目录id查询目录下数据总数
     * @param directoryId
     * @return
     */
    public static int queryDownloadCountByDirectoryId(String directoryId) {
        return tDownloadDao.queryDownloadCountByDirectoryId(directoryId);
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

    /**
     * 查询清单下所有下载完成的文件总数
     * @param directoryId
     * @return
     */
    public static int countAllCompletedDownloadDataByDirectoryId(String directoryId) {
        return tDownloadDataDao.countAllCompletedDownloadDataByDirectoryId(directoryId);
    }

    /**
     * 查询清单下所有下载完成的文件
     * @param directoryId
     * @param limit
     * @param offset
     * @return
     */
    public static List<TDownloadDataWithFields> getDownloadDataListByDirectoryId(String directoryId, int limit, int offset) {
        return tDownloadDataDao.queryAllDownloadDataByDirectoryId(directoryId, limit, offset);
    }

    /**
     * 根据下载ID获取第一条数据
     * @param downloadId
     * @return
     */
    public static TDownloadData querySingleDataByDownloadId(String downloadId) {
        return tDownloadDataDao.querySingleDataByDownloadId(downloadId);
    }

    public static String queryDownloadInfo() {
        JSONArray jsonArray = new JSONArray();
        List<TDownload> tDownloads = tDownloadDao.queryAllData();
        for (TDownload tDownload : tDownloads) {
            JSONObject jsonObject = new JSONObject();
            String directoryName = tDirectoryDao.queryNameById(tDownload.getDirectoryId());
            TDownloadData tDownloadData = querySingleDataByDownloadId(tDownload.getDownloadId());
            String hashTitle = tDownloadData.getSavePath();
            if (!Utils.isNullOrEmpty(hashTitle)) {
                String[] parts = hashTitle.split("/");
                String hash = parts[parts.length - 2];
                jsonObject.put("videoHash", hash);
            }
            jsonObject.put("directory", Utils.isNullOrEmpty(directoryName) ? Utils.getString(R.string.defaultList) : directoryName);
            jsonObject.put("videoName", tVideoDao.queryTitleById(tDownload.getLinkId()));
            jsonArray.add(jsonObject);
        }
        return JSON.toJSONString(jsonArray);
    }
}
