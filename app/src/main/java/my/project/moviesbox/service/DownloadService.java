package my.project.moviesbox.service;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOWNLOAD;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.ALog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.config.NotificationUtils;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.event.DownloadEvent;
import my.project.moviesbox.event.DownloadStateEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoConverter;
import my.project.moviesbox.utils.VideoUtils;

/**
  * @包名: my.project.moviesbox.parser.service
  * @类名: DownloadService
  * @描述: 下载服务
  * @作者: Li Z
  * @日期: 2024/1/23 16:25
  * @版本: 1.0
 */
public class DownloadService extends Service {
    private Context context;
    private NotificationUtils mNotify;
    PowerManager.WakeLock wakeLock = null;
    private List<Long> taskIds = new ArrayList<>();

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
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, DownloadService.class.getName());
        if (null != wakeLock)  {
            wakeLock.acquire();
        }
        context = this;
        mNotify = new NotificationUtils(context);
        mNotify.cancelNotification(-2);
//        mNotify.showServiceNotification(-1, "下载服务运行中");
        LogUtil.logInfo(getClass().getName(), "Service onCreate");
        Aria.download(this).register();
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        // 服务关闭时存在未下载完成的任务，停止下载
        List<DownloadEntity> downloadEntities = Aria.download(this).getAllNotCompleteTask();
        if (downloadEntities != null && downloadEntities.size() > 0) {
//            mNotify.showServiceNotification(-2, "下载服务关闭");
            Aria.download(this).stopAllTask();
        }
        mNotify.cancelNotification(-1);
        for (Long id : taskIds) {
            mNotify.cancelNotification(id.intValue());
        }
        Aria.download(this).unRegister();
        LogUtil.logInfo(getClass().getName(), "Service onDestroy");
        super.onDestroy();
    }

    @Download.onWait
    public void onTaskWait(DownloadTask downloadTask) {
        EventBus.getDefault().post(REFRESH_DOWNLOAD);
    }

    @Download.onTaskResume
    public void onTaskResume(DownloadTask downloadTask) {
        Long taskId = downloadTask.getEntity().getId();
        mNotify.showDefaultNotification(taskId.intValue(), (String) VideoUtils.getVodInfo(downloadTask, 0), downloadTask.getTaskName());
    }


    @Download.onTaskStart
    public void onTaskStart(DownloadTask downloadTask) {
        Long taskId = downloadTask.getEntity().getId();
        taskIds.add(taskId);
        mNotify.showDefaultNotification(taskId.intValue(), (String) VideoUtils.getVodInfo(downloadTask, 0), downloadTask.getTaskName());
    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask downloadTask) {
        shouldUnRegister();
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        Long taskId = downloadTask.getEntity().getId();
        taskIds.add(taskId);
        mNotify.cancelNotification(taskId.intValue());
        shouldUnRegister();
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask, Exception e) {
        Long taskId = downloadTask.getEntity().getId();
        String vodEpisodes = downloadTask.getTaskName();
        String savePath = downloadTask.getFilePath();
        String vodTitle = (String) VideoUtils.getVodInfo(downloadTask, 0);
        mNotify.uploadInfo(taskId.intValue(), vodTitle, downloadTask.getTaskName(), String.format(Utils.getString(R.string.downloadErrorMsg), (e == null ? Utils.getString(R.string.downloadDefaultErrorMsg) :  ALog.getExceptionString(e))));
        TDownloadManager.updateDownloadError(downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
        EventBus.getDefault().post(new DownloadEvent(taskId, vodTitle, vodEpisodes, savePath, 0, 2));
        shouldUnRegister();
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        Long taskId = downloadTask.getEntity().getId();
        String vodEpisodes = downloadTask.getTaskName();
        long fileSize = downloadTask.getFileSize();
        String vodTitle = (String) VideoUtils.getVodInfo(downloadTask, 0);
        String savePath = downloadTask.getFilePath();
        boolean isM3U8 = savePath.contains("m3u8");
        taskIds.remove(downloadTask.getEntity().getId());
        Aria.download(this).load(taskId).ignoreCheckPermissions().cancel(false); // 下载完成删除任务
        if (!isM3U8) {
            // MP4
            mNotify.uploadInfo(taskId.intValue(), vodTitle, vodEpisodes, Utils.getString(R.string.downloadSuccessMsg));
            TDownloadManager.updateDownloadSuccess(savePath, taskId, fileSize);
            EventBus.getDefault().post(new DownloadEvent(taskId, vodTitle, vodEpisodes, savePath, fileSize, 1));
        } else {
            // TS转换MP4
            String inputPath = savePath.replaceAll("m3u8", "ts");
            String outputPath = savePath.replaceAll("m3u8", "mp4");
            int notificationId = mNotify.uploadInfo(taskId.intValue(), vodTitle, vodEpisodes, Utils.getString(R.string.downloadTsSuccessMsg));
            TDownloadDataManager.updateDownloadState(3, taskId);
            EventBus.getDefault().post(new DownloadStateEvent(taskId, vodEpisodes, 3));
            VideoConverter.convertTSToMP4(notificationId, savePath, inputPath, outputPath, taskId, vodTitle, vodEpisodes, mNotify);
        }
        shouldUnRegister();
    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask downloadTask) {
        Long taskId = downloadTask.getEntity().getId();
        mNotify.upload(taskId.intValue(), downloadTask.getPercent());
    }

    private void shouldUnRegister() {
        List<DownloadEntity> list = Aria.download(this).getDRunningTask();
        if (list == null || list.size() == 0) {
            // 没有正在执行的任务
            mNotify.cancelNotification(-1);
        }
    }
}
