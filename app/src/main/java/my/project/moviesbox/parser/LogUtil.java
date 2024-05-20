package my.project.moviesbox.parser;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

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
    static {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    /**
     * 打印方法
     * @param message
     * @param args
     */
    public static void logInfo(String message, String args) {
        if (Utils.isNullOrEmpty(message) && Utils.isNullOrEmpty(args))
            return;
        Logger.d(Utils.isNullOrEmpty(args) ? message : String.format((Utils.isNullOrEmpty(message) ? "" : message + " -> ") + "%s", args));
    }
}
