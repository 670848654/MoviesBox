package my.project.moviesbox.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import my.project.moviesbox.config.NotificationUtils;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.TodayUpdateBean;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: RSS订阅查询服务
 * @date 2024/3/25 22:54
 */
public class RssService extends Service {
    private NotificationUtils mNotify;
    private static final ParserInterface parserInterface;

    static {
        parserInterface = ParserInterfaceFactory.getParserInterface();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
        {
            mNotify = new NotificationUtils(this);
            String rssUrl = intent.getStringExtra("url");
            OkHttpUtils.getInstance().doGet(rssUrl, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mNotify.showRSSNotification("获取RSS订阅失败", e.getMessage(), "");
                    stopSelf();
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String body = new String(response.body().bytes(), StandardCharsets.UTF_8);
                    List<TodayUpdateBean> todayUpdateBeanList = parserInterface.parserRss(body);
                    if (todayUpdateBeanList.size() > 0) {
                        for (TodayUpdateBean todayUpdateBean : todayUpdateBeanList) {
                            mNotify.showRSSNotification(todayUpdateBean.getTitle(), todayUpdateBean.getInfo(), todayUpdateBean.getUrl());
                        }
                    }
                    stopSelf();
                }
            });
        }
        else
            stopSelf();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.logInfo(getClass().getName(), "RssService onCreate");
    }
}
