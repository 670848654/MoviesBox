package my.project.moviesbox.database.manager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import my.project.moviesbox.database.dao.THistoryDao;
import my.project.moviesbox.database.dao.THistoryDataDao;
import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.database.entity.THistoryWithFields;
import my.project.moviesbox.utils.DateUtils;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 15:12
 */
public class THistoryManager extends BaseManager {
    public static THistoryDao tHistoryDao = getInstance().tHistoryDao();
    public static THistoryDataDao tHistoryDataDao = getInstance().tHistoryDataDao();

    /**
     * 新增或更新历史记录
     * @param videoId
     * @param videoDescUrl
     * @param videoImgUrl
     */
    public static void addOrUpdateHistory(String videoId, String videoDescUrl, String videoImgUrl) {
        videoDescUrl = videoDescUrl.replaceAll(parserInterface.getDefaultDomain(), "");
        THistory tHistory = tHistoryDao.queryByVideoId(videoId);
        if (Utils.isNullOrEmpty(tHistory)) {
            // 不存在则新增
            tHistory = new THistory();
            tHistory.setHistoryId(getUUID());
            tHistory.setLinkId(videoId);
            tHistory.setVideoDescUrl(videoDescUrl);
            tHistory.setVideoImgUrl(videoImgUrl);
            tHistory.setVisible(0);
            tHistory.setUpdateTime(getDateTimeStr());
            tHistoryDao.insert(tHistory);
        } else {
            // 存在则更新
            if (!tHistory.getVideoImgUrl().contains("base64"))
                tHistory.setVideoImgUrl(videoImgUrl);
            tHistory.setVideoDescUrl(videoDescUrl);
            tHistoryDao.update(tHistory);
        }
    }

    /**
     * 查询所有已播放过的剧集信息
     * @param videoId
     * @param isHistory
     * @param videoSource
     */
    public static String queryAllIndex(String videoId, boolean isHistory, int videoSource) {
        List<String> allDramas = isHistory ? tHistoryDao.queryAllVideoUrlBySourceVideoId(videoSource, videoId) : tHistoryDao.queryAllVideoUrlByVideoId(videoId);
        StringBuilder stringBuilder = new StringBuilder();
        for (String drama : allDramas) {
            stringBuilder.append(drama);
        }
        return stringBuilder.toString();
    }

    /**
     * 获取所有有效历史记录
     * @param limit
     * @param offset
     * @return
     */
    public static List<THistoryWithFields> queryAllHistory(int limit, int offset) {
        List<THistoryWithFields> tHistories = tHistoryDao.queryAllHistory(source, limit, offset);
        for (THistoryWithFields withFields : tHistories) {
            THistoryData tHistoryData = tHistoryDataDao.querySingleData(withFields.getTHistory().getHistoryId());
            withFields.setVideoPlaySource(tHistoryData.getVideoPlaySource());
            withFields.setVideoUrl(tHistoryData.getVideoUrl());
            withFields.setVideoNumber(tHistoryData.getVideoNumber());
            withFields.setWatchProgress(tHistoryData.getWatchProgress());
            withFields.setVideoDuration(tHistoryData.getVideoDuration());
            String lastWatchTIme = withFields.getTHistory().getUpdateTime();
            try {
                withFields.getTHistory().setUpdateTime(DateUtils.isYesterday(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastWatchTIme)));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return tHistories;
    }

    /**
     * 更新历史记录
     * @param videoId
     * @param videoUrl
     * @param position
     * @param duration
     */
    public static void updateHistory(String videoId, String videoUrl, long position, long duration) {
        THistory tHistory = tHistoryDao.queryByVideoId(videoId);
        tHistory.setUpdateTime(getDateTimeStr());
        tHistoryDao.update(tHistory);
        THistoryData tHistoryData = tHistoryDataDao.queryByLinkIdVideoUrl(tHistory.getHistoryId(), videoUrl);
        tHistoryData.setWatchProgress(position);
        if (tHistoryData.getVideoDuration() == 0)
            tHistoryData.setVideoDuration(duration);
        tHistoryData.setUpdateTime(getDateTimeStr());
        tHistoryDataDao.update(tHistoryData);
    }

    /**
     * 获取剧集播放进度
     * @param videoId
     * @param videoUrl
     * @return
     */
    public static long getPlayPosition(String videoId, String videoUrl) {
        THistory tHistory = tHistoryDao.queryByVideoId(videoId);
        THistoryData tHistoryData = tHistoryDataDao.queryByLinkIdVideoUrl(tHistory.getHistoryId(), videoUrl);
        return tHistoryData.getWatchProgress();
    }

    /**
     * 询历史记录总数
     * @return
     */
    public static int queryHistoryCount() {
        return tHistoryDao.queryHistoryCount(source);
    }

    /**
     * 隐藏历史记录
     * @param historyId
     * @param videoSource
     * @param isAll
     */
    public static void deleteHistory(String historyId, int videoSource, boolean isAll) {
        if (isAll)
            tHistoryDao.deleteAllHistory(videoSource);
        else
            tHistoryDao.deleteSingleHistory(historyId);
    }
}
