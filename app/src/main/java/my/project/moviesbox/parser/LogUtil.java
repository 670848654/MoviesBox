package my.project.moviesbox.parser;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    private static final List<ParserLogBean> logs = Collections.synchronizedList(new ArrayList<>());
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

        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.getDefault())
                .format(new Date());
        String content = Utils.isNullOrEmpty(args)
                ? message
                : String.format(
                (Utils.isNullOrEmpty(message) ? "" : message + " -> ") + "%s",
                args
        );

        synchronized (logs) {
            // 在头部插入最新日志
            logs.add(0, new ParserLogBean(dateTime, content));

            // 删除多余日志（双重检查，避免并发越界）
            while (logs.size() > MAX_LOG_COUNT) {
                int lastIndex = logs.size() - 1;
                if (lastIndex >= 0 && lastIndex < logs.size()) {
                    logs.remove(lastIndex);
                } else {
                    break; // 安全退出
                }
            }
        }

        Logger.d(dateTime + content);
    }

    public static List<ParserLogBean> getLogs() {
        return logs;
    }
}
