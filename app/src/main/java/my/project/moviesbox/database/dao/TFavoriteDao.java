package my.project.moviesbox.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import my.project.moviesbox.database.entity.TFavorite;
import my.project.moviesbox.database.entity.TFavoriteWithFields;

/**
  * @包名: my.project.moviesbox.database.dao
  * @类名: TFavoriteDao
  * @描述: 收藏表相关SQL
  * @作者: Li Z
  * @日期: 2024/2/20 16:27
  * @版本: 1.0
 */
@Dao
public interface TFavoriteDao {
    /**
     * 查询全部
     * @return
     */
    @Query("select * from TFavorite")
    List<TFavorite> queryAll();

    /**
     * 批量新增
     * @param tFavorites
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorites(List<TFavorite> tFavorites);

    /**
     * 清空表
     */
    @Query("delete from tfavorite")
    void deleteAll();
    /**
     * 新增
     * @param tFavorite
     */
    @Insert
    void insert(TFavorite... tFavorite);

    /**
     * 更新
     * @param tFavorite
     */
    @Update
    void update(TFavorite... tFavorite);

    /**
     * 通过影视ID删除
     * @param videoId
     */
    @Query("delete from TFavorite where linkId=:videoId")
    void deleteByVideoId(String videoId);

    /**
     * 通过影视ID查询是否在收藏夹中
     * @param videoId
     * @return
     */
    @Query("select * from TFavorite where linkId =:videoId")
    TFavorite queryByVideoId(String videoId);

    /**
     * 通过影视ID更新最后一次播放集数地址
     * @param voidId
     * @param videoUrl
     */
    @Query("update TFavorite set lastVideoPlayNumberUrl =:videoUrl where linkId=:voidId")
    void updateLastPlayNumberUrlByVideoId(String voidId, String videoUrl);

    /**
     * 变更域名时更新相关域名
     * @param oldDomain 旧域名
     * @param newDomain 新域名
     */
    @Query("UPDATE TFavorite\n" +
            "SET \n" +
            "    videoUrl = REPLACE(videoUrl, :oldDomain, :newDomain),\n" +
            "    lastVideoPlayNumberUrl = REPLACE(lastVideoPlayNumberUrl, :oldDomain, :newDomain),\n" +
            "    videoDesc = REPLACE(videoDesc, :oldDomain, :newDomain)\n" +
            "WHERE \n" +
            "    videoUrl LIKE :oldDomain || '%' OR\n" +
            "    lastVideoPlayNumberUrl LIKE :oldDomain || '%' OR\n" +
            "    videoDesc LIKE :oldDomain || '%'")
    void updateUrlByChangeDomain(String oldDomain, String newDomain);

    /**
     * 分页查询用户收藏的影视
     * @param videoSource
     * @param offset
     * @param limit
     * @return
     */
    @Query("SELECT t2.videoId as videoId,\n" +
            "       t2.videoTitle as videoTitle,\n" +
            "       t1.* \n" +
            "  FROM TFavorite t1\n" +
            "       INNER JOIN\n" +
            "       TVideo t2 ON t1.linkId = t2.videoId AND \n" +
            "       t2.videoSource =:videoSource\n" +
            " WHERE (t1.directoryId IS NULL AND :directoryId = '') OR (t1.directoryId = :directoryId AND :directoryId != '')" +
            " ORDER BY t1.`index` DESC\n" +
            " LIMIT :limit OFFSET :offset")
    List<TFavoriteWithFields> queryFavorite(int videoSource, String directoryId, int offset, int limit);

    /**
     * 根据目录id查询目录下数据总数
     * @param videoSource
     * @param directoryId
     * @return
     */
    @Query("SELECT count(t1.favoriteId) \n" +
            "  FROM TFavorite t1\n" +
            "       INNER JOIN\n" +
            "       TVideo t2 ON t1.linkId = t2.videoId AND \n" +
            "       t2.videoSource =:videoSource\n" +
            " WHERE (t1.directoryId IS NULL AND :directoryId = '') OR (t1.directoryId = :directoryId AND :directoryId != '')")
    int queryFavoriteCountByDirectoryId(int videoSource, String directoryId);

    /**
     * 查询收藏表总数
     * @param videoSource
     * @return
     */
    @Query("select count(t1.favoriteId) from TFavorite t1 INNER JOIN TVideo t2 ON t1.linkId = t2.videoId AND t2.videoSource =:videoSource")
    int queryFavoriteCount(int videoSource);

    /**
     * 更新到默认清单
     * @param directoryId
     */
    @Query("update TFavorite set directoryId = null where directoryId =:directoryId")
    void updateDirectoryId2Null(String directoryId);
}
