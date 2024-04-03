package my.project.moviesbox.database.dao;

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
    @Query("SELECT\n" +
            "       t1.*,\n" +
            "       t2.videoTitle as videoTitle,\n" +
            "       t2.videoSource as videoSource,\n" +
            "       count(t3.downloadDataId) as downloadDataSize,\n" +
            "       sum(t3.videoFileSize) as filesSize,\n" +
            "       count(CASE WHEN t3.complete != 1 THEN t3.downloadDataId ELSE NULL END) as noCompleteSize, \n" +
            "       GROUP_CONCAT(t3.ariaTaskId, ',') as ariaTaskIds  \n" +
            "  FROM TDownload t1\n" +
            "       LEFT JOIN\n" +
            "       TVideo t2 ON t1.linkId = t2.videoId\n" +
            "       LEFT JOIN\n" +
            "       TDownloadData T3 ON t1.downloadId = T3.linkId\n" +
            "       GROUP BY \n" +
            "       t1.downloadId,\n" +
            "       t1.videoImgUrl,\n" +
            "       t1.videoDescUrl,\n" +
            "       t2.videoTitle\n" +
            " ORDER BY t1.createTime DESC limit :limit offset :offset")
    List<TDownloadWithFields> queryAllDownloads(int limit, int offset);

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
}
