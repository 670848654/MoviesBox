package my.project.moviesbox.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import my.project.moviesbox.R;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.config
  * @类名: MyExceptionHandler
  * @描述: 统一异常管理
  * @作者: Li Z
  * @日期: 2024/1/22 19:50
  * @版本: 1.0
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String FILE_NAME = "crash_log_%s.txt";
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public MyExceptionHandler() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String fileName = String.format(FILE_NAME, new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(Utils.getString(R.string.exceptionType), throwable.getClass().getName()));
        stringBuilder.append(String.format(Utils.getString(R.string.exceptionInformation), throwable.getMessage()));
        stringBuilder.append(Utils.getString(R.string.exceptionStackTrace));
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            stringBuilder.append(element + "\n");
        }
        Throwable cause = throwable.getCause();
        if (cause != null) {
            stringBuilder.append(String.format(Utils.getString(R.string.whatCausedTheException), cause));
        }
        try {
            File crashLogFile = new File(Utils.APP_CRASH_LOGS_PATH + File.separator + fileName);
            PrintWriter writer = new PrintWriter(new FileWriter(crashLogFile));
            writer.println(stringBuilder);
            // 写入异常信息
            throwable.printStackTrace(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 调用系统默认的处理异常方法，例如弹出异常对话框，关闭应用等
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, throwable);
        }
    }
}