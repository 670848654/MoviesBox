package my.project.moviesbox.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import my.project.moviesbox.database.entity.TDirectory;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/11/1 10:38
 */
@Dao
public interface TDirectoryDao {

    /**
     * 查询全部
     * @return
     */
    @Query("select * from TDirectory")
    List<TDirectory> queryAll();

    /**
     * 批量新增
     * @param tDirectories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDirectories(List<TDirectory> tDirectories);

    /**
     * 清空表
     */
    @Query("delete from TDirectory")
    void deleteAll();

    /**
     * 新增
     * @param tDirectories
     */
    @Insert
    void insert(TDirectory... tDirectories);

    /**
     * 更新
     * @param tDirectories
     */
    @Update
    void update(TDirectory... tDirectories);

    /**
     * 查询当前源收藏夹目录列表
     * @param source
     * @param type
     * @return
     */
    @Query("select * from TDirectory where source=:source and type=:type \n" +
            " ORDER BY createTime DESC\n")
    List<TDirectory> queryFavoriteDirectoryList(int source, String type);

    /**
     * 查询下载目录列表
     * @param type
     * @return
     */
    @Query("select * from TDirectory where type=:type \n" +
            " ORDER BY createTime DESC\n")
    List<TDirectory> queryDownloadDirectoryList(String type);

    /**
     * 通过主键获取数据
     * @param id
     * @return
     */
    @Query("select * from TDirectory where id=:id")
    TDirectory queryById(String id);

    /**
     * 查询当前源收藏是否存在相同目录名称
     * @param source
     * @param type
     * @param name
     * @return
     */
    @Query("select count(1) from TDirectory where source=:source and type=:type \n" +
            " and name=:name\n")
    int queryFavoriteDirectoryNameExist(int source, String type, String name);

    /**
     * 查询下载是否存在相同目录名称
     * @param source
     * @param type
     * @param name
     * @return
     */
    @Query("select count(1) from TDirectory where type=:type \n" +
            " and name=:name\n")
    int queryDownloadDirectoryNameExist(String type, String name);

    /**
     * 通过主键删除
     * @param id
     */
    @Query("delete from TDirectory where id=:id")
    void deleteById(String id);
}
