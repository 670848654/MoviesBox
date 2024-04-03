package my.project.moviesbox.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import my.project.moviesbox.R;
import my.project.moviesbox.config.NotificationUtils;
import my.project.moviesbox.event.CheckUpdateEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 检查更新服务
 * @date 2024/3/25 22:54
 */
public class CheckUpdateService extends Service {
    private Context context;
    private NotificationUtils mNotify;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mNotify = new NotificationUtils(context);
        int notificationId = mNotify.showCheckUpdateNotification(99, getString(R.string.checkUpdateTitle), getString(R.string.checkForUpdates), "");
        LogUtil.logInfo(getClass().getName(), "Service onCreate");
        OkHttpUtils.getInstance().doGet(getString(R.string.githubApi), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mNotify.uploadCheckUpdateInfo(notificationId, getString(R.string.checkUpdateTitle), getString(R.string.connection2GithubTimedOut), "");
                EventBus.getDefault().post(new CheckUpdateEvent().fail(getString(R.string.alreadyTheLatestVersion)));
                stopSelf();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonBody = response.body().string();
                try {
                    JSONObject jsonObject = JSONObject.parseObject(jsonBody);
                    String latestVersion = jsonObject.getString("tag_name");
                    if (latestVersion.equals(Utils.getASVersionName())) {
                        mNotify.cancelNotification(notificationId);
                        EventBus.getDefault().post(new CheckUpdateEvent().fail(getString(R.string.connection2GithubTimedOut)));
                    } else {
                        String url = jsonObject.getString("html_url");
                        String body = jsonObject.getString("body");
                        mNotify.uploadCheckUpdateInfo(notificationId, getString(R.string.checkUpdateTitle), String.format(getString(R.string.newVersionFound), latestVersion, getString(R.string.go2TheReleasePage)), url);
                        String newVersionMsg = String.format(getString(R.string.newVersionFound), latestVersion, "");
                        EventBus.getDefault().post(new CheckUpdateEvent().success(newVersionMsg, newVersionMsg, body, url));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mNotify.uploadCheckUpdateInfo(notificationId, getString(R.string.checkUpdateTitle), String.format(getString(R.string.checkUpdateError), e.getMessage()), "");
                    EventBus.getDefault().post(new CheckUpdateEvent().fail(String.format(getString(R.string.checkUpdateError), e.getMessage())));
                }
                stopSelf();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
