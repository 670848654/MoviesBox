package my.project.moviesbox.database.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.project.moviesbox.application.App;
import my.project.moviesbox.database.dao.TFavoriteDao;
import my.project.moviesbox.database.dao.THistoryDao;
import my.project.moviesbox.database.dao.THistoryDataDao;
import my.project.moviesbox.database.dao.TVideoDao;
import my.project.moviesbox.database.entity.TFavorite;
import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.database.entity.TVideo;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/3/31 20:56
 */
public class BackupsManager extends BaseManager {
    public static TVideoDao tVideoDao = getInstance().tVodDao();
    public static TFavoriteDao tFavoriteDao = getInstance().tFavoriteDao();
    public static THistoryDao tHistoryDao = getInstance().tHistoryDao();
    public static THistoryDataDao tHistoryDataDao = getInstance().tHistoryDataDao();

    /**
     * 查询所有待备份数据
     * @return
     */
    public static void createBackupsFile(String filePath) {
        try {
            JSONObject jsonObject = new JSONObject();
            setSharedPreferences2JSONObject("appData", jsonObject);
            setSharedPreferences2JSONObject("white_night_mode_sp", jsonObject);
            String jsonStr = jsonObject.toJSONString();
            jsonStr = jsonStr.substring(0, jsonStr.length() - 1) + ",";
            appendTextToFile(jsonStr, filePath);
            List<TVideo> tVideoList = tVideoDao.queryAll();
            appendTextToFile("\"videoList\":[", filePath);
            appendJSONObjectToFile(tVideoList, filePath);
            appendTextToFile("],", filePath);

            List<TFavorite> tFavorites = tFavoriteDao.queryAll();
            appendTextToFile("\"favoriteList\":[", filePath);
            appendJSONObjectToFile(tFavorites, filePath);
            appendTextToFile("],", filePath);

            List<THistory> tHistories = tHistoryDao.queryAll();
            appendTextToFile("\"historyList\":[", filePath);
            appendJSONObjectToFile(tHistories, filePath);
            appendTextToFile("],", filePath);

            List<THistoryData> tHistoryData = tHistoryDataDao.queryAll();
            appendTextToFile("\"historyDataList\":[", filePath);
            appendJSONObjectToFile(tHistoryData, filePath);
            appendTextToFile("]", filePath);

            appendTextToFile("}", filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendJSONObjectToFile(List<?> data, String filePath) throws IOException {
        for (int i=0,size=data.size(); i<size; i++) {
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(data.get(i)));
            jsonObject.remove("index");
            appendTextToFile(jsonObject.toJSONString(), filePath);
            if (i != size - 1) {
                appendTextToFile(",", filePath);
            }
        }
    }

    public static boolean appendTextToFile(String text, String targetFilePath) throws IOException {
        BufferedWriter writer = null;
        try {
            File targetFile = new File(targetFilePath);
            writer = new BufferedWriter(new FileWriter(targetFile, true)); // true 表示追加模式
            writer.write(text);
            return true;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * 封装JSONObject
     * @param spName
     * @return
     */
    private static void setSharedPreferences2JSONObject(String spName, JSONObject jsonObject) {
        SharedPreferences sharedPreferences = App.getInstance().getSharedPreferences(spName, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if(key.equals("data_name_suffix"))
                continue;
            jsonObject.put(key, value);
        }
    }

    /**
     * 封装JSONObject
     * @param key
     * @param list
     * @return
     */
    private static void setTableData2JSONArray(String filePath, List<?> data) {
        try {
            JSONWriter writer = new JSONWriter(new FileWriter(filePath));
            writer.startArray();
            for (Object obj : data) {
                writer.writeValue(obj);
            }
            writer.endArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 恢复数据
     * @param tVideoList
     * @param tFavorites
     * @param tHistories
     * @param tHistoryData
     */
    public static void restoreBackup(List<TVideo> tVideoList, List<TFavorite> tFavorites, List<THistory> tHistories, List<THistoryData> tHistoryData) {
        tVideoDao.deleteAll();
        tFavoriteDao.deleteAll();
        tHistoryDao.deleteAll();
        tHistoryDataDao.deleteAll();
        tVideoDao.insertVideos(tVideoList);
        tFavoriteDao.insertFavorites(tFavorites);
        tHistoryDao.insertHistories(tHistories);
        tHistoryDataDao.insertHistoryDatas(tHistoryData);
    }
}
