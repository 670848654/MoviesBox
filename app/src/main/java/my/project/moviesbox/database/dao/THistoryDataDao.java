package my.project.moviesbox.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import my.project.moviesbox.database.entity.THistoryData;

/**
  * @包名: my.project.moviesbox.database.dao
  * @类名: THistoryDataDao
  * @描述: 历史记录子表相关SQL
  * @作者: Li Z
  * @日期: 2024/2/20 16:28
  * @版本: 1.0
 */
@Dao
public interface THistoryDataDao {
    /**
     * 查询全部
     * @return
     */
    @Query("select * from THistoryData")
    List<THistoryData> queryAll();

    /**
     * 批量新增
     * @param tHistoryData
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistoryDatas(List<THistoryData> tHistoryData);

    /**
     * 清空表
     */
    @Query("delete from THistoryData")
    void deleteAll();
    /**
     * 新增
     * @param tHistoryData
     */
    @Insert
    void insert(THistoryData... tHistoryData);

    /**
     * 更新
     * @param tHistoryData
     */
    @Update
    void update(THistoryData... tHistoryData);

    /**
     * 根据主表ID查询数据
     * @param historyId
     * @return
     */
    @Query("select * from THistoryData where linkId =:historyId")
    THistoryData queryByLinkId(String historyId);

    /**
     * 通过历史记录ID集数播放地址查询历史记录子表
     * @param historyId
     * @param videoUrl
     * @return
     */
    @Query("select * from THistoryData where linkId =:historyId AND videoUrl=:videoUrl")
    THistoryData queryByLinkIdVideoUrl(String historyId, String videoUrl);

    /**
     * 根据主表ID查询子表最新的一条数据
     * @param historyId
     * @return
     */
    @Query("select * from THISTORYDATA where linkId =:historyId order by updateTime DESC LIMIT 1")
    THistoryData querySingleData(String historyId);

    /**
     * 变更域名时更新相关域名
     * @param oldDomain 旧域名
     * @param newDomain 新域名
     */
    @Query("UPDATE THistoryData\n" +
            "SET \n" +
            "    videoUrl = REPLACE(videoUrl, :oldDomain, :newDomain)\n" +
            "WHERE \n" +
            "    videoUrl LIKE :oldDomain || '%'")
    void updateUrlByChangeDomain(String oldDomain, String newDomain);
}
