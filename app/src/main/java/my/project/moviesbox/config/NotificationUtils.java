package my.project.moviesbox.config;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import my.project.moviesbox.R;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.DetailsActivity;

/**
  * @包名: my.project.moviesbox.config
  * @类名: DownloadNotification
  * @描述: 通知工具类
  * @作者: Li Z
  * @日期: 2024/1/22 19:45
  * @版本: 1.0
 */
public class NotificationUtils {

    private NotificationManager mManager;
    private Context context;
    private Notification.Builder oldBuilder;
    private NotificationCompat.Builder newBuilder;
    private Map<Integer, Notification> notifications;
    private static final String CHANNEL_ID = Utils.getString(R.string.channelId);
    private static final CharSequence CHANNEL_NAME = Utils.getString(R.string.channelName);
    private static final String CHANNEL_DESCRIPTION = Utils.getString(R.string.channelDescription);

    private static final String CHANNEL_INFO_ID = Utils.getString(R.string.channelInfoId);
    private static final CharSequence CHANNEL_INFO_NAME = Utils.getString(R.string.channelInfoName);
    private static final String CHANNEL_INFO_DESCRIPTION = Utils.getString(R.string.channelInfoDescription);

    private static final String CHANNEL_SERVICE_INFO_ID = Utils.getString(R.string.channelServiceInfoId);
    private static final CharSequence CHANNEL_SERVICE_INFO_NAME = Utils.getString(R.string.channelServiceInfoName);
    private static final String CHANNEL_SERVICE_INFO_Description = Utils.getString(R.string.channelServiceInfoDescription);

    private static final String CHANNEL_CHECK_SERVICE_INFO_ID = Utils.getString(R.string.channelCheckServiceInfoId);
    private static final CharSequence CHANNEL_CHECK_SERVICE_INFO_NAME = Utils.getString(R.string.channelCheckServiceInfoName);
    private static final String CHANNEL_CHECK_SERVICE_INFO_Description = Utils.getString(R.string.channelCheckServiceInfoDescription);

    private static final String CHANNEL_RSS_SERVICE_INFO_ID = Utils.getString(R.string.channelRssServiceInfoId);
    private static final CharSequence CHANNEL_RSS_SERVICE_INFO_NAME = Utils.getString(R.string.channelRssServiceInfoName);
    private static final String CHANNEL_RSS_SERVICE_INFO_DESCRIPTION = Utils.getString(R.string.channelServiceServiceInfoDescription);
    private Set<Integer> generatedNumbers;
    private Random random;

    private int progressMax = 100;

     /**
      * @方法名称: DownloadNotification
      * @方法描述: 初始化
      * @日期: 2024/1/22 19:45
      * @作者: Li Z
      * @return
      */
    public NotificationUtils(Context context) {
        this.context = context;
        notifications = new HashMap<>();
        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        generatedNumbers = new HashSet<>();
        random = new Random();
    }

     /**
      * @方法名称: showServiceNotification
      * @方法描述: 下载操作通知
      * @日期: 2024/1/22 19:45
      * @作者: Li Z
      * @return
      */
    public void showServiceNotification(int id, String content) {
        if (!notifications.containsKey(id)) {
            Notification notification = null;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                oldBuilder = new Notification.Builder(context);
                oldBuilder.setAutoCancel(false).setPriority(Notification.PRIORITY_HIGH).setSmallIcon(R.drawable.round_download_for_offline_24);
                notification = oldBuilder.setContentTitle(CHANNEL_SERVICE_INFO_NAME).setContentText(content).build();
                mManager.notify(id, notification);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_SERVICE_INFO_ID, CHANNEL_SERVICE_INFO_NAME, NotificationManager.IMPORTANCE_HIGH);
                mChannel.setDescription(CHANNEL_SERVICE_INFO_Description);
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.RED);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mChannel.setShowBadge(false);
                mManager.createNotificationChannel(mChannel);

                newBuilder = new NotificationCompat.Builder(context, CHANNEL_SERVICE_INFO_ID);
                newBuilder.setAutoCancel(false).setSmallIcon(R.drawable.round_download_for_offline_24);
                notification = newBuilder.setContentTitle(CHANNEL_SERVICE_INFO_NAME).setContentText(content).build();
                mManager.notify(id, notification);
            }
            notifications.put(id, notification);
        }
    }

     /**
      * @方法名称: showDefaultNotification
      * @方法描述: 下载时通知
      * @日期: 2024/1/22 19:46
      * @作者: Li Z
      * @param 
      * @return 
      */
    public void showDefaultNotification(int notificationId, String title, String videoNumber) {
        if (!notifications.containsKey(notificationId)) {
            Notification notification;

            NotificationCompat.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 及以上创建 Channel
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_INFO_ID,
                        CHANNEL_INFO_NAME,
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription(CHANNEL_INFO_DESCRIPTION);
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setShowBadge(false);
                mManager.createNotificationChannel(channel);

                builder = new NotificationCompat.Builder(context, CHANNEL_INFO_ID);
            } else {
                // 低版本直接用 NotificationCompat.Builder
                builder = new NotificationCompat.Builder(context);
                builder.setPriority(NotificationCompat.PRIORITY_LOW);
            }

            // 通用属性
            builder.setAutoCancel(true)
                    .setSmallIcon(R.drawable.round_download_for_offline_24)
                    .setContentTitle(title + " · " + videoNumber)
                    .setOngoing(true)
                    .setProgress(progressMax, 0, false);

            // 构建通知并发送
            notification = builder.build();
            mManager.notify(notificationId, notification);

            notifications.put(notificationId, notification);
        }
    }

     /**
      * @方法名称: upload
      * @方法描述: 更新下载进度条
      * @日期: 2024/1/22 19:46
      * @作者: Li Z
      * @param 
      * @return 
      */
    public void upload(int notificationId, int progress) {
        Notification notification = notifications.get(notificationId);
        if (notification != null) {
            Notification.Builder builder = Notification.Builder.recoverBuilder(context, notification);
            builder.setProgress(100, progress,false);
            mManager.notify(notificationId, notification);
        }
    }

     /**
      * @方法名称: uploadInfo
      * @方法描述: 下载完成/失败更新通知
      * @日期: 2024/1/22 19:46
      * @作者: Li Z
      * @param 
      * @return 
      */
    public int uploadInfo(int notificationId, String title, String videoNumber, String msg) {
        Notification notification = notifications.get(notificationId);
        if (notification != null)
            cancelNotification(notificationId);
        return showUploadNotification(generateUniqueRandom(), title, videoNumber, msg);
    }

    /**
     * 生成唯一随机整数
     * @return
     */
    public int generateUniqueRandom() {
        int randomValue;
        do {
            randomValue = random.nextInt(); // 生成随机整数
        } while (!generatedNumbers.add(randomValue));
        return randomValue;
    }

     /**
      * @方法名称: showUploadNotification
      * @方法描述: 下载完成/失败通知
      * @日期: 2024/1/22 19:47
      * @作者: Li Z
      * @param 
      * @return 
      */
    public int showUploadNotification(int notificationId, String title, String videoNumber, String msg) {
        if (!notifications.containsKey(notificationId)) {
            Notification notification;

            NotificationCompat.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 及以上创建 Channel
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription(CHANNEL_DESCRIPTION);
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setShowBadge(false);
                mManager.createNotificationChannel(channel);

                builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            } else {
                builder = new NotificationCompat.Builder(context);
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            }

            // 通用属性
            builder.setAutoCancel(true)
                    .setSmallIcon(R.drawable.round_download_for_offline_24)
                    .setContentTitle(title + " · " + videoNumber)
                    .setContentText(msg);

            // 构建通知并发送
            notification = builder.build();
            mManager.notify(notificationId, notification);

            notifications.put(notificationId, notification);
        }
        return notificationId;
    }

     /**
      * @方法名称: cancelNotification
      * @方法描述: 取消通知
      * @日期: 2024/1/22 19:47
      * @作者: Li Z
      * @param 
      * @return 
      */
    public void cancelNotification(int notificationId) {
        if (!Utils.isNullOrEmpty(mManager))
            mManager.cancel(notificationId);
        if (!Utils.isNullOrEmpty(notifications))
            notifications.remove(notificationId);
    }

    /**
     * 检测更新通知
     * @param notificationId
     * @param title
     * @param content
     * @param url
     * @return
     */
    public int showCheckUpdateNotification(int notificationId, String title, String content, String url) {
        PendingIntent pendingIntent = null;
        if (!Utils.isNullOrEmpty(url)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }

        if (!notifications.containsKey(notificationId)) {
            Notification notification;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0及以上
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_CHECK_SERVICE_INFO_ID,
                        CHANNEL_CHECK_SERVICE_INFO_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription(CHANNEL_CHECK_SERVICE_INFO_Description);
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setShowBadge(false);
                mManager.createNotificationChannel(channel);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHECK_SERVICE_INFO_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.round_cloud_sync_24)
                        .setContentTitle(title)
                        .setContentText(content);

                if (pendingIntent != null) {
                    builder.setContentIntent(pendingIntent);
                }

                notification = builder.build();
                mManager.notify(notificationId, notification);

            } else {
                // Android 8.0以下
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.round_cloud_sync_24)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentTitle(title)
                        .setContentText(content);

                if (pendingIntent != null) {
                    builder.setContentIntent(pendingIntent);
                }

                notification = builder.build();
                mManager.notify(notificationId, notification);
            }

            notifications.put(notificationId, notification);
        }
        return notificationId;
    }

    public int uploadCheckUpdateInfo(int notificationId, String title, String content, String url) {
        Notification notification = notifications.get(notificationId);
        if (notification != null)
            cancelNotification(notificationId);
        return showCheckUpdateNotification(generateUniqueRandom(), title, content, url);
    }

    /**
     * 显示RSS订阅通知
     * @param title
     * @param content
     * @param url
     * @return
     */
    public int showRSSNotification(String title, String content, String url) {
        int notificationId = generateUniqueRandom();

        PendingIntent pendingIntent = null;
        if (!Utils.isNullOrEmpty(url)) {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("url", url);
            pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 高版本
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_RSS_SERVICE_INFO_ID,
                    CHANNEL_RSS_SERVICE_INFO_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(CHANNEL_RSS_SERVICE_INFO_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setShowBadge(false);
            mManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_RSS_SERVICE_INFO_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.round_fiber_new_24)
                    .setContentTitle(title)
                    .setContentText(content);

            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent);
            }

            notification = builder.build();
            mManager.notify(notificationId, notification);

        } else {
            // 低版本
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.round_fiber_new_24)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentTitle(title)
                    .setContentText(content);

            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent);
            }

            notification = builder.build();
            mManager.notify(notificationId, notification);
        }

        notifications.put(notificationId, notification);
        return notificationId;
    }
}
