package my.project.moviesbox.database.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.database.dao.TDirectoryDao;
import my.project.moviesbox.database.dao.TDownloadDao;
import my.project.moviesbox.database.dao.TFavoriteDao;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.enums.DirectoryTypeEnum;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/11/1 11:21
 */
public class TDirectoryManager extends BaseManager {
    public static TDirectoryDao tDirectoryDao = database.tDirectoryDao();
    public static TFavoriteDao tFavoriteDao = database.tFavoriteDao();
    public static TDownloadDao tDownloadDao = database.tDownloadDao();

    /**
     * 查询当前源收藏夹目录列表
     * @param showConfigBtn 是否显示配置按钮
     * @param showAllSelect 是否显示全部
     * @return
     */
    public static List<TDirectory> queryFavoriteDirectoryList(boolean showConfigBtn, boolean showAllSelect) {
        List<TDirectory> tDirectories = new ArrayList<>();
        TDirectory tDirectory;
        if (showAllSelect) {
            // 显示全部
            tDirectory = new TDirectory();
            tDirectory.setIndex(-2);
            tDirectory.setId("all");
            tDirectory.setName(Utils.getString(R.string.allList));
            tDirectory.setSource(source);
            tDirectory.setType(DirectoryTypeEnum.FAVORITE.getName());
            tDirectory.setShowConfigBtn(false);
            tDirectories.add(tDirectory);
        }
        // 添加默认清单
        tDirectory = new TDirectory();
        tDirectory.setIndex(-1);
        tDirectory.setId("");
        tDirectory.setName(Utils.getString(R.string.defaultList));
        tDirectory.setSource(source);
        tDirectory.setType(DirectoryTypeEnum.FAVORITE.getName());
        tDirectory.setShowConfigBtn(false);
        tDirectories.add(tDirectory);
        List<TDirectory> tDirectoryList = tDirectoryDao.queryFavoriteDirectoryList(source, DirectoryTypeEnum.FAVORITE.getName());
        if (showConfigBtn) {
            for (TDirectory directory : tDirectoryList) {
                directory.setShowConfigBtn(true);
            }
        }
        tDirectories.addAll(tDirectoryList);
        return tDirectories;
    }

    /**
     * 查询下载目录列表
     * @param showConfigBtn 是否显示配置按钮
     * @param showAllSelect 是否显示全部
     * @return
     */
    public static List<TDirectory> queryDownloadDirectoryList(boolean showConfigBtn, boolean showAllSelect) {
        List<TDirectory> tDirectories = new ArrayList<>();
        TDirectory tDirectory;
        if (showAllSelect) {
            // 显示全部
            tDirectory = new TDirectory();
            tDirectory.setIndex(-2);
            tDirectory.setId("all");
            tDirectory.setName(Utils.getString(R.string.allList));
            tDirectory.setSource(source);
            tDirectory.setType(DirectoryTypeEnum.FAVORITE.getName());
            tDirectory.setShowConfigBtn(false);
            tDirectories.add(tDirectory);
        }
        // 添加默认清单
        tDirectory = new TDirectory();
        tDirectory.setIndex(-1);
        tDirectory.setId("");
        tDirectory.setName(Utils.getString(R.string.defaultList));
        tDirectory.setType(DirectoryTypeEnum.DOWNLOAD.getName());
        tDirectory.setShowConfigBtn(false);
        tDirectories.add(tDirectory);
        List<TDirectory> tDirectoryList = tDirectoryDao.queryDownloadDirectoryList(DirectoryTypeEnum.DOWNLOAD.getName());
        if (showConfigBtn) {
            for (TDirectory directory : tDirectoryList) {
                directory.setShowConfigBtn(true);
            }
        }
        tDirectories.addAll(tDirectoryList);
        return tDirectories;
    }

    /**
     * 新增数据
     * @param name
     * @param source
     */
    public static String insert(String name, int source, DirectoryTypeEnum directoryTypeEnum) {
        TDirectory tDirectory = new TDirectory();
        tDirectory.setId(getUUID());
        tDirectory.setName(name);
        if (Objects.equals(directoryTypeEnum.getName(), DirectoryTypeEnum.FAVORITE.getName()))
            tDirectory.setSource(source);
        tDirectory.setType(directoryTypeEnum.getName());
        tDirectory.setCreateTime(getDateTimeStr());
        tDirectoryDao.insert(tDirectory);
        return tDirectory.getId();
    }

    /**
     * 通过ID获取数据
     * @param id
     * @return
     */
    public static TDirectory queryById(String id, boolean showConfigBtn) {
        TDirectory tDirectory = tDirectoryDao.queryById(id);
        if (showConfigBtn)
            tDirectory.setShowConfigBtn(true);
        return tDirectory;
    }

    /**
     * 检查清单名称是否存在重复
     * @param name
     * @param source
     * @param type
     * @return
     */
    public static boolean checkNameExist(String name, int source, String type) {
        int count;
        if (Objects.equals(type, DirectoryTypeEnum.FAVORITE.getName())) {
            count = tDirectoryDao.queryFavoriteDirectoryNameExist(source, type, name);
        } else {
            count = tDirectoryDao.queryDownloadDirectoryNameExist(type, name);
        }
        return count > 0;
    }

    public static void update(TDirectory tDirectory) {
        tDirectoryDao.update(tDirectory);
    }

    public static void delete(String id, String type) {
        tDirectoryDao.deleteById(id);
        if (Objects.equals(type, DirectoryTypeEnum.FAVORITE.getName())) {
            tFavoriteDao.updateDirectoryId2Null(id);
        } else {
            tDownloadDao.updateDirectoryId2Null(id);
        }
    }
}
