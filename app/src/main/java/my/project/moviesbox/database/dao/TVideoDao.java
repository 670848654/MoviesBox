package my.project.moviesbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import my.project.moviesbox.database.entity.TVideo;

/**
  * @包名: my.project.moviesbox.database.dao
  * @类名: TVideoDao
  * @描述: 影视主表相关SQL
  * @作者: Li Z
  * @日期: 2024/2/20 16:28
  * @版本: 1.0
 */
@Dao
public interface TVideoDao {
    /**
     * 查询全部内容
     * @return
     */
    @Query("select * from TVideo")
    List<TVideo> queryAll();

    /**
     * 批量新增
     * @param tVideoList
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVideos(List<TVideo> tVideoList);

    /**
     * 清空表
     */
    @Query("delete from TVideo")
    void deleteAll();
    /**
     * 通过影视名称和数据源查询总数
     * @param videoTitle
     * @param videoSource
     * @return
     */
    @Query("select COUNT(*) from TVideo where videoTitle = :videoTitle AND videoSource = :videoSource")
    int queryIsExistByTitleAndSource(String videoTitle, int videoSource);

    /**
     * 访问影视详情时就新增数据
     * @param tVod
     */
    @Insert
    void insert(TVideo... tVod);

    /**
     * 获取影视ID
     * @param videoTitle
     * @param videoSource
     * @return
     */
    @Query("select videoId from TVideo where videoTitle = :videoTitle AND videoSource = :videoSource")
    String queryId(String videoTitle, int videoSource);

    /**
     * 根据下载ID查询
     * 影视名称
     * 影视来源
     * @param ariaTaskId
     * @return
     */
    @Query("SELECT t3.* \n" +
            "  FROM TDOWNLOADDATA t1\n" +
            "       LEFT JOIN\n" +
            "       TDownload t2 ON t1.linkId = t2.downloadId\n" +
            "       LEFT JOIN\n" +
            "       TVideo t3 ON t2.linkId = t3.videoId\n" +
            " WHERE t1.ariaTaskId =:ariaTaskId\n")
    TVideo queryDownloadVodInfo(long ariaTaskId);
}
