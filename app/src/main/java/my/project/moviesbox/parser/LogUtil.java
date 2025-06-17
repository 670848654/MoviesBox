package my.project.moviesbox.parser;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import my.project.moviesbox.bean.ParserLogBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.parser
  * @类名: LogUtil
  * @描述: 日志类
  * @作者: Li Z
  * @日期: 2024/1/23 18:50
  * @版本: 1.0
 */
public class LogUtil {
    private static final List<ParserLogBean> logs = new ArrayList<>();
    private static final int MAX_LOG_COUNT = 50; // 最多保存50条

    static {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    private LogUtil() {}

    /**
     * 打印方法
     * @param message
     * @param args
     */
    public static void logInfo(String message, String args) {
        if (Utils.isNullOrEmpty(message) && Utils.isNullOrEmpty(args))
            return;
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.getDefault()).format(new Date());
        String content = Utils.isNullOrEmpty(args) ? message : String.format((Utils.isNullOrEmpty(message) ? "" : message + " -> ") + "%s", args);
        logs.add(0, new ParserLogBean(dateTime, content));
        if (logs.size() >= MAX_LOG_COUNT) {
            logs.remove(logs.size() - 1); // 删除最后一条，保留最新
        }
        /*if (SharedPreferencesUtils.getSaveParserLogs())
            saveLogToFile(logs);*/
        Logger.d(dateTime + content);
    }

    public static List<ParserLogBean> getLogs() {
        return logs;
    }

    public static void clearLogs() {
        logs.clear();
    }

    // 保存日志到文件
    /*public static void saveLogToFile(String log) {
        String fileName = String.format(FILE_NAME, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        File logFile = new File(Utils.APP_PARSER_LOGS_PATH + File.separator + fileName);
        FileOutputStream fos = null;
        try {
            if (!logFile.exists())
                logFile.createNewFile();
            fos = new FileOutputStream(logFile, true);
            fos.write((log + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    /**
     * 删除当天日志
     */
    /*public static void deleteLogFile() {
        String fileName = String.format(FILE_NAME, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        File logFile = new File(Utils.APP_PARSER_LOGS_PATH + File.separator + fileName);
        if (logFile.exists())
            logFile.delete();
    }*/
}
