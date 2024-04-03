package my.project.moviesbox.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import my.project.moviesbox.database.entity.TDownloadData;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;

/**
  * @包名: my.project.moviesbox.database.dao
  * @类名: TDownloadDataDao
  * @描述: 下载子表相关SQL
  * @作者: Li Z
  * @日期: 2024/2/20 16:27
  * @版本: 1.0
 */
@Dao
public interface TDownloadDataDao {

    /**
     * 根据ID查询
     * @param downloadDataId
     * @return
     */
    @Query("select * FROM TDownloadData where downloadDataId=:downloadDataId")
    TDownloadData queryById(String downloadDataId);

    /**
     * 新增
     * @param tDownloadData
     */
    @Insert
    void insert(TDownloadData... tDownloadData);

    /**
     * 更新
     * @param tDownloadData
     */
    @Update
    void update(TDownloadData... tDownloadData);

    /**
     * 通过下载ID 集数名称查找
     * @param downloadId
     * @param videoNumber
     * @return
     */
    @Query("select * from TDownloadData where linkId =:downloadId AND videoNumber=:videoNumber")
    TDownloadData queryByLinkIdVideoUrl(String downloadId, String videoNumber);

    /**
     * 根据下载主表ID查询子表数据
     * @param downloadId
     * @param limit
     * @param offset
     * @return
     */
    @Query("SELECT t1.*,\n" +
            "       t3.videoTitle as videoTitle,\n" +
            "       t2.videoImgUrl as videoImgUrl,\n" +
            "       t3.videoSource as videoSource\n" +
            "  FROM TDownloadData t1\n" +
            "       LEFT JOIN\n" +
            "       TDownload t2 ON t1.linkId = t2.downloadId\n" +
            "       LEFT JOIN\n" +
            "       TVideo t3 ON t2.linkId = t3.videoId\n" +
            " WHERE t1.linkId = :downloadId\n" +
            " ORDER BY t1.videoPlaySource ASC,\n" +
            "          t1.videoNumber ASC\n" +
            " LIMIT :limit OFFSET :offset")
    List<TDownloadDataWithFields> queryDownloadDataByDownloadId(String downloadId, int limit, int offset);

    /**
     * 查询当前下载任务状态
     * @param videoId
     * @param playNumber
     * @param playSource
     * @return
     */
    @Query("select t2.* from TDownload t1 LEFT JOIN TDOWNLOADDATA t2 ON t1.downloadId = t2.linkId where t1.linkId =:videoId AND t2.videoNumber=:playNumber AND t2.videoPlaySource=:playSource")
    TDownloadData queryDownloadDataIsDownloadError(String videoId, String playNumber, int playSource);

    /**
     * 更新下载成功数据
     * @param savePath
     * @param ariaTaskId
     * @param videoFileSize
     */
    @Query("update TDownloadData set complete = 1, savePath=:savePath, videoFileSize=:videoFileSize, ariaTaskId = -99  where ariaTaskId=:ariaTaskId")
    void updateDownloadVideoSuccess(String savePath, long ariaTaskId, long videoFileSize);

    /**
     * 更新下载失败数据
     * @param savePath
     * @param ariaTaskId
     * @param videoFileSize
     */
    @Query("update TDownloadData set complete = 2, savePath=:savePath, videoFileSize=:videoFileSize where ariaTaskId=:ariaTaskId")
    void updateDownloadVideoError(String savePath, long ariaTaskId, long videoFileSize);

    /**
     * 更新待下载信息
     * @param downloadId
     * @param ariaTaskId
     */
    @Query("update TDownloadData set complete = 0 where linkId=:downloadId AND ariaTaskId=:ariaTaskId")
    void updateDownloadVideoInsert(String downloadId, long ariaTaskId);

    /**
     * 获取下载的影视下所有剧集总数
     * @param downloadId
     * @return
     */
    @Query("SELECT count(*) FROM TDownloadData where linkId=:downloadId")
    int queryDownloadDataCount(String downloadId);

    /**
     * 更新下载信息
     * @param ariaTaskId
     */
    @Query("update TDownloadData set complete = 0 where ariaTaskId=:ariaTaskId")
    void updateDownloadState(long ariaTaskId);

    /**
     * 删除下载数据
     * @param id
     */
    @Query("delete from TDownloadData where downloadDataId =:id")
    void deleteDownloadData(String id);

    /**
     * 获取已下载的文件总大小
     * @param downloadId
     * @return
     */
    @Query("select sum(videoFileSize) from TDownloadData where linkId =:downloadId")
    String queryDownloadFilesSize(String downloadId);

    /**
     * 获取当前任务下未完成的数量
     * @param downloadId
     * @return
     */
    @Query("select count(*) from TDownloadData where linkId =:downloadId AND complete != 1")
    int queryDownloadNotCompleteCount(String downloadId);

    /**
     * 删除所有下载
     */
    @Query("delete from TDownloadData")
    void deleteAllDownloadData();

    /**
     * 获取当前播放进度
     * @param id
     * @return
     */
    @Query("select watchProgress from TDownloadData where downloadDataId=:id")
    long queryDownloadDataProgressById(String id);
}
