package my.project.moviesbox.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryWithFields;

/**
  * @包名: my.project.moviesbox.database.dao
  * @类名: THistoryDao
  * @描述: 历史记录主表相关SQL
  * @作者: Li Z
  * @日期: 2024/2/20 16:28
  * @版本: 1.0
 */
@Dao
public interface THistoryDao {
    @Query("select * from THistory")
    List<THistory> queryAll();

    /**
     * 批量新增
     * @param tHistories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistories(List<THistory> tHistories);

    /**
     * 清空表
     */
    @Query("delete from THistory")
    void deleteAll();
    /**
     * 新增
     * @param tHistory
     */
    @Insert
    void insert(THistory... tHistory);

    /**
     * 更新
     * @param tHistory
     */
    @Update
    void update(THistory... tHistory);

    /**
     * 通过影视ID查询是否在历史记录中
     * @param videoId
     * @return
     */
    @Query("select * from THistory where linkId =:videoId")
    THistory queryByVideoId(String videoId);

    /**
     * 通过影视ID查询所有已播放过的剧集信息
     * @param videoSource
     * @param videoId
     * @return
     */
    @Query("select t2.videoUrl from THistory t1 INNER JOIN THistoryData t2 ON t1.historyId = t2.linkId AND t2.videoPlaySource=:videoSource where t1.linkId =:videoId")
    List<String> queryAllVideoUrlBySourceVideoId(int videoSource, String videoId);

    /**
     * 通过影视ID查询所有已播放过的剧集信息
     * @param videoId
     * @param videoId
     * @return
     */
    @Query("select t2.videoUrl from THistory t1 INNER JOIN THistoryData t2 ON t1.historyId = t2.linkId where t1.linkId =:videoId")
    List<String> queryAllVideoUrlByVideoId(String videoId);

    /**
     * 获取所有有效历史记录
     * @param limit
     * @param offset
     * @return
     */
    @Query("SELECT t2.videoId as videoId,\n" +
            "       t1.*,\n" +
            "       t2.videoTitle as videoTitle,\n" +
            "       t2.videoSource as videoSource\n" +
            "  FROM THistory t1\n" +
            "       INNER JOIN\n" +
            "       TVideo t2 ON t1.linkId = t2.videoId AND \n" +
            "                     t2.videoSource = :videoSource\n" +
            " WHERE t1.visible = 1\n" +
            " ORDER BY t1.updateTime DESC\n" +
            " LIMIT :limit OFFSET :offset\n")
    List<THistoryWithFields> queryAllHistory(int videoSource, int limit, int offset);

    /**
     * 查询历史记录总数
     * @param videoSource
     * @return
     */
    @Query("SELECT count(t1.historyId) FROM THistory t1 INNER JOIN TVideo t2 ON t1.linkId = t2.videoId AND t2.videoSource =:videoSource WHERE t1.visible = 1")
    int queryHistoryCount(int videoSource);

    /**
     * 隐藏单个历史记录
     * @param historyId
     */
    @Query("update THistory set visible = 0 where historyId =:historyId")
    void deleteSingleHistory(String historyId);

    /**
     * 隐藏全部历史记录
     * @param videoSource 当前源
     */
    @Query("UPDATE THistory\n" +
            "SET visible = 0\n" +
            "WHERE linkId IN (\n" +
            "    SELECT t1.linkId\n" +
            "    FROM THistory t1\n" +
            "    INNER JOIN TVideo t2 ON t1.linkId = t2.videoId \n" +
            "    WHERE t2.videoSource = :videoSource)")
    void deleteAllHistory(int videoSource);
}
