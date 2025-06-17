package my.project.moviesbox.database.manager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import my.project.moviesbox.database.dao.TFavoriteDao;
import my.project.moviesbox.database.dao.THistoryDao;
import my.project.moviesbox.database.dao.THistoryDataDao;
import my.project.moviesbox.database.entity.TFavorite;
import my.project.moviesbox.database.entity.TFavoriteWithFields;
import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.event.RefreshFavoriteEvent;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 16:17
 */
public class TFavoriteManager extends BaseManager {
    public static TFavoriteDao tFavoriteDao = getInstance().tFavoriteDao();
    public static THistoryDao tHistoryDao = getInstance().tHistoryDao();
    public static THistoryDataDao tHistoryDataDao = getInstance().tHistoryDataDao();


    /**
     * 检查影视是否收藏
     * @param videoId
     * @return
     */
    public static boolean checkFavorite(String videoId) {
        return !Utils.isNullOrEmpty(tFavoriteDao.queryByVideoId(videoId));
    }

    /**
     * 收藏、移除收藏
     * @param videoDescUrl
     * @param videoImgUrl
     * @param videoDesc
     * @param videoId
     * @return
     */
    public static boolean favorite(String videoDescUrl, String videoImgUrl, String videoDesc, String videoId, String directoryId) {
        TFavorite tFavorite = tFavoriteDao.queryByVideoId(videoId);
        if (Utils.isNullOrEmpty(tFavorite)) {
            // 收藏
            videoDescUrl = videoDescUrl.replaceAll(parserInterface.getDefaultDomain(), "");
            tFavorite = new TFavorite();
            tFavorite.setFavoriteId(getUUID());
            tFavorite.setLinkId(videoId);
            tFavorite.setVideoImgUrl(videoImgUrl);
            tFavorite.setVideoUrl(videoDescUrl);
            tFavorite.setVideoDesc(videoDesc);
            tFavorite.setLastVideoPlayNumberUrl("");
            tFavorite.setLastVideoUpdateNumber("");
            tFavorite.setDirectoryId(Utils.isNullOrEmpty(directoryId) ? null : directoryId);
            THistory tHistory = tHistoryDao.queryByVideoId(videoId);
            if (!Utils.isNullOrEmpty(tHistory)) {
                THistoryData tHistoryData = tHistoryDataDao.querySingleData(tHistory.getHistoryId());
                if (!Utils.isNullOrEmpty(tHistoryData)) {
                    tFavorite.setLastVideoPlayNumberUrl(tHistoryData.getVideoUrl());
                    tFavorite.setLastVideoUpdateNumber(tHistoryData.getVideoNumber());
                }
            }
            tFavorite.setState(0);
            tFavoriteDao.insert(tFavorite);
            return true;
        } else {
            // 移除收藏
            tFavoriteDao.deleteByVideoId(videoId);
            return false;
        }
    }

    /**
     * 更新收藏影视最后播放地址
     * @param lastVideoPlayNumberUrl
     * @param lastVideoPlayNumber
     * @param videoId
     */
    public static void updateFavorite(String lastVideoPlayNumberUrl, String lastVideoPlayNumber, String videoId) {
        TFavorite tFavorite = tFavoriteDao.queryByVideoId(videoId);
        if (!Utils.isNullOrEmpty(tFavorite)) {
            tFavorite.setLastVideoPlayNumberUrl(lastVideoPlayNumberUrl);
            tFavorite.setLastVideoUpdateNumber(lastVideoPlayNumber);
            tFavoriteDao.update(tFavorite);
            EventBus.getDefault().post(new RefreshFavoriteEvent(videoId, lastVideoPlayNumber));
        }
    }

    /**
     * 更新收藏影视信息
     * @param videoDescUrl
     * @param videoImgUrl
     * @param videoDesc
     * @param videoId
     */
    public static void updateFavorite(String videoDescUrl, String videoImgUrl, String videoDesc, String videoId) {
        TFavorite tFavorite = tFavoriteDao.queryByVideoId(videoId);
        if (!tFavorite.getVideoImgUrl().contains("base64"))
            tFavorite.setVideoImgUrl(videoImgUrl);
        tFavorite.setVideoUrl(videoDescUrl);
        tFavorite.setVideoDesc(videoDesc);
        tFavoriteDao.update(tFavorite);
    }

    /**
     * 分页查询用户收藏的影视
     * @param directoryId
     * @param offset
     * @param limit
     * @param updateOrder
     * @return
     */
    public static List<TFavoriteWithFields> queryFavorite(String directoryId, int offset, int limit, boolean updateOrder) {
        return tFavoriteDao.queryFavorite(source, directoryId, offset, limit);
    }

    /**
     * 根据目录id查询目录下数据总数
     * @param directoryId
     * @return
     */
    public static int queryFavoriteCountByDirectoryId(String directoryId) {
        return tFavoriteDao.queryFavoriteCountByDirectoryId(source, directoryId);
    }

    /**
     * 查询收藏表总数
     * @return
     */
    public static int queryFavoriteCount() {
        return tFavoriteDao.queryFavoriteCount(source);
    }

    /**
     * 删除收藏
     * @param videoId
     */
    public static void deleteFavorite(String videoId) {
        tFavoriteDao.deleteByVideoId(videoId);
    }

    /**
     * 变更域名时更新相关域名
     * @param oldDomain 旧域名
     * @param newDomain 新域名
     */
    public static void updateUrlByChangeDomain(String oldDomain, String newDomain) {
        tFavoriteDao.updateUrlByChangeDomain(oldDomain, newDomain);
        tHistoryDao.updateUrlByChangeDomain(oldDomain, newDomain);
        tHistoryDataDao.updateUrlByChangeDomain(oldDomain, newDomain);
    }

    /**
     * 更新收藏影视清单位置
     * @param videoId
     * @param directoryId
     */
    public static void updateFavoriteDirectoryId(String videoId, String directoryId) {
        TFavorite tFavorite = tFavoriteDao.queryByVideoId(videoId);
        tFavorite.setDirectoryId(directoryId.isEmpty() ? null : directoryId);
        tFavoriteDao.update(tFavorite);
    }
}
