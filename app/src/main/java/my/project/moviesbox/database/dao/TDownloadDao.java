package my.project.moviesbox.database.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import my.project.moviesbox.database.entity.TDownload;
import my.project.moviesbox.database.entity.TDownloadWithFields;

/**
  * @包名: my.project.moviesbox.database.dao
  * @类名: TDownloadDao
  * @描述: 下载主表相关SQL
  * @作者: Li Z
  * @日期: 2024/2/20 16:26
  * @版本: 1.0
 */
@Dao
public interface TDownloadDao {

    /**
     * 新增数据
     * @param tDownload
     */
    @Insert
    void insert(TDownload... tDownload);

    /**
     * 更新数据
     * @param tDownload
     */
    @Update
    void update(TDownload... tDownload);

    /**
     * 根据ID查询下载信息
     * @param downloadId
     * @return
     */
    @Query("select * from TDownload where downloadId=:downloadId")
    TDownload queryByDownloadId(String downloadId);

    /**
     * 通过影视ID查询下载信息
     * @param videoId
     * @return
     */
    @Query("select * from TDownload where linkId=:videoId")
    TDownload queryByVideoId(String videoId);

    /**
     * 查询所有下载任务
     * @param limit
     * @param offset
     * @return
     */
    @Query("SELECT t1.*, " +
            "       t2.videoTitle as videoTitle, " +
            "       t2.videoSource as videoSource, " +
            "       count(t3.downloadDataId) as downloadDataSize, " +
            "       sum(t3.videoFileSize) as filesSize, " +
            "       count(CASE WHEN t3.complete != 1 THEN t3.downloadDataId ELSE NULL END) as noCompleteSize, " +
            "       GROUP_CONCAT(t3.ariaTaskId, ',') as ariaTaskIds " +
            "  FROM TDownload t1 " +
            "       LEFT JOIN TVideo t2 ON t1.linkId = t2.videoId " +
            "       LEFT JOIN TDownloadData t3 ON t1.downloadId = t3.linkId " +
            " WHERE (:directoryId = 'all' OR " +
            "        (:directoryId = '' AND t1.directoryId IS NULL) OR " +
            "        (:directoryId != '' AND :directoryId != 'all' AND t1.directoryId = :directoryId)) " +
            " GROUP BY t1.downloadId, t1.videoImgUrl, t1.videoDescUrl, t2.videoTitle " +
            " ORDER BY t1.createTime DESC " +
            " LIMIT :limit OFFSET :offset")
    List<TDownloadWithFields> queryAllDownloads(@Nullable String directoryId, int limit, int offset);

    /**
     * 根据目录id查询目录下数据总数
     * @param directoryId
     * @return
     */
    @Query("SELECT count(t1.downloadId) " +
            "  FROM TDownload t1 " +
            "       INNER JOIN TVideo t2 ON t1.linkId = t2.videoId " +
            " WHERE (:directoryId = 'all' OR " +
            "        (:directoryId = '' AND t1.directoryId IS NULL) OR " +
            "        (:directoryId != '' AND :directoryId != 'all' AND t1.directoryId = :directoryId))")
    int queryDownloadCountByDirectoryId(String directoryId);

    /**
     * 删除下载数据
     * @param id
     */
    @Query("delete from TDownload where downloadId =:id")
    void deleteDownload(String id);

    /**
     * 查询下载列表总数
     * @return
     */
    @Query("select count(*) from tdownload")
    int queryDownloadCount();

    /**
     * 查询下载总数
     * @return
     */
    @Query("SELECT count(t2.downloadDataId) FROM TDownload t1 INNER JOIN TDOWNLOADDATA t2 ON t1.downloadId = t2.linkId")
    int queryAllDownloadCount();

    /**
     * 删除所有下载
     */
    @Query("delete from TDownload")
    void deleteAllDownload();

    /**
     * 更新到默认清单
     * @param directoryId
     */
    @Query("update TDownload set directoryId = null where directoryId =:directoryId")
    void updateDirectoryId2Null(String directoryId);
}
