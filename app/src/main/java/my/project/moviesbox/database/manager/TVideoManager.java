package my.project.moviesbox.database.manager;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.database.dao.TDownloadDao;
import my.project.moviesbox.database.dao.TFavoriteDao;
import my.project.moviesbox.database.dao.THistoryDao;
import my.project.moviesbox.database.dao.THistoryDataDao;
import my.project.moviesbox.database.dao.TVideoDao;
import my.project.moviesbox.database.entity.TFavorite;
import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.database.entity.TVideo;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 13:31
 */
public class TVideoManager extends BaseManager {
    public static TVideoDao tVideoDao = getInstance().tVodDao();
    public static TFavoriteDao tFavoriteDao = getInstance().tFavoriteDao();
    public static THistoryDao tHistoryDao = getInstance().tHistoryDao();
    public static THistoryDataDao tHistoryDataDao = getInstance().tHistoryDataDao();
    public static TDownloadDao tDownloadDao = getInstance().tDownloadDao();

    /**
     * 新增数据
     * @param videoTitle
     */
    public static String insertVod(String videoTitle) {
        int count = tVideoDao.queryIsExistByTitleAndSource(videoTitle, source);
        if (count == 0) {
            TVideo tVideo = new TVideo();
            tVideo.setVideoId(getUUID());
            tVideo.setVideoTitle(videoTitle);
            tVideo.setVideoSource(source);
            tVideoDao.insert(tVideo);
            return tVideo.getVideoId();
        } else
            return tVideoDao.queryId(videoTitle, source);
    }

    /**
     * 获取影视ID
     * @param videoTitle
     * @return
     */
    public static String queryId(String videoTitle) {
        return tVideoDao.queryId(videoTitle, source);
    }

    /**
     * 播放剧集时执行保存播放状态
     * <p>1.通过影视ID查询收藏表，查看当前观看的影视是否在收藏表中，如果查到则更新收藏表该影视的最后播放地址字段，然后通过该播放地址比对收藏表中最后一次更新的播放地址，如果两者相同，则更新收藏表该影视更新状态为0（未更新）</p>
     * <p>2.通过影视ID查询历史记录表获取历史记录ID</p>
     * <p>3.通过影视ID更新历史记录表的最后更新时间</p>
     * <p>4.通过历史记录ID、播放页地址查询历史记录子表，如果记录不存在，则新增记录，存在则更新历史记录子表</p>
     * @param videoId 影视ID
     * @param videoUrl 播放页地址
     * @param videoSource 播放源
     * @param videoNumber 播放集数名称
     */
    public static void addVideoHistory(String videoId,  String videoUrl, int videoSource, String videoNumber) {
        videoUrl = videoUrl.replaceAll(parserInterface.getDefaultDomain(), "");
        // 查询收藏夹是否存在该剧集
        TFavorite tFavorite = tFavoriteDao.queryByVideoId(videoId);
        if (!Utils.isNullOrEmpty(tFavorite)) {
            // 收藏夹存在
            tFavoriteDao.updateLastPlayNumberUrlByVideoId(videoId, videoUrl);
            if (tFavorite.getLastVideoPlayNumberUrl().equals(videoId)) {
                tFavorite.setState(0);
                tFavoriteDao.update(tFavorite);
            }
        }
        // 查询在历史记录中是否存在
        THistory tHistory = tHistoryDao.queryByVideoId(videoId);
        if (!Utils.isNullOrEmpty(tHistory)) {
            // 存在则更新历史记录
            tHistory.setVisible(1);
            tHistory.setUpdateTime(getDateTimeStr());
            tHistoryDao.update(tHistory);
        }
        // 查询历史记录子表是否存在
        THistoryData tHistoryData = tHistoryDataDao.queryByLinkIdVideoUrl(tHistory.getHistoryId(), videoUrl);
        if (Utils.isNullOrEmpty(tHistoryData)) {
            // 不存在则新增子表数据
            tHistoryData = new THistoryData();
            tHistoryData.setHistoryDataId(getUUID());
            tHistoryData.setLinkId(tHistory.getHistoryId());
            tHistoryData.setVideoPlaySource(videoSource);
            tHistoryData.setVideoUrl(videoUrl);
            tHistoryData.setVideoNumber(videoNumber);
            tHistoryData.setWatchProgress(0);
            tHistoryData.setVideoDuration(0);
            tHistoryData.setUpdateTime(getDateTimeStr());
            tHistoryDataDao.insert(tHistoryData);
        } else {
            // 存在则更新子表数据
            tHistoryData.setVideoPlaySource(videoSource);
            tHistoryData.setVideoNumber(videoNumber);
            tHistoryData.setUpdateTime(getDateTimeStr());
            tHistoryDataDao.update(tHistoryData);
        }
    }

    /**
     * 根据下载ID查询
     * 影视名称
     * 影视来源
     * @param ariaTaskId
     * @return
     */
    public static List<Object> queryDownloadVodInfo(long ariaTaskId) {
        TVideo tVideo = tVideoDao.queryDownloadVodInfo(ariaTaskId);
        List<Object> objects = new ArrayList<>();
        objects.add(tVideo.getVideoTitle());
        objects.add(tVideo.getVideoSource());
        return objects;
    }

    public static void updateImg(String id, String imgUrl, int type) {
        switch (type) {
            case 0:
                // 更新收藏夹图片
                TFavorite tFavorite = tFavoriteDao.queryByVideoId(id);
                tFavorite.setVideoImgUrl(imgUrl);
                tFavoriteDao.update(tFavorite);
                break;
            case 1:
                // 更新历史记录图片
                THistory tHistory = tHistoryDao.queryByVideoId(id);
                tHistory.setVideoImgUrl(imgUrl);
                tHistoryDao.update(tHistory);
                break;
            /*case 2:
                // 更新下载记录图片
                TDownload tDownload = tDownloadDao.queryByVideoId(id);
                tDownload.setVideoImgUrl(imgUrl);
                tDownloadDao.update(tDownload);
                break;*/
        }
    }

    /**
     * 获取上次观看集数和时间
     * @param videoId
     * @return
     */
    public static THistoryData queryLastWatchData(String videoId) {
        // 查询在历史记录中是否存在
        THistory tHistory = tHistoryDao.queryByVideoId(videoId);
        if (!Utils.isNullOrEmpty(tHistory)) {
            return tHistoryDataDao.querySingleData(tHistory.getHistoryId());
        }
        return null;
    }
}
