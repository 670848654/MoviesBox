package my.project.moviesbox.utils;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import com.arthenica.mobileffmpeg.FFmpeg;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import my.project.moviesbox.R;
import my.project.moviesbox.config.NotificationUtils;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.event.DownloadEvent;
import my.project.moviesbox.parser.LogUtil;

/**
 * @author Li
 * @version 1.0
 * @description: FFmpeg TS转MP4
 * @date 2024/3/12 15:45
 */
public class VideoConverter {
    /**
     * TS转MP4
     * @param notificationId 通知ID
     * @param m3u8Path M3U8保存地址
     * @param inputPath ts地址
     * @param outputPath mp4地址
     * @param taskId 任务ID
     * @param vodTitle 影视标题
     * @param vodEpisodes 影视集数
     * @param notificationUtils 通知
     */
    public static void convertTSToMP4(int notificationId, String m3u8Path, String inputPath, String outputPath, Long taskId, String vodTitle, String vodEpisodes, NotificationUtils notificationUtils) {
        String[] cmd = {"-i", inputPath, "-acodec", "copy", "-vcodec", "copy", "-absf", "aac_adtstoasc", outputPath};
        // 执行FFmpeg命令
        FFmpeg.executeAsync(cmd, (executionId, returnCode) -> {
            if (returnCode == RETURN_CODE_SUCCESS) {
                LogUtil.logInfo("转换MP4成功", null);
                // 转换完成删除TS文件
                File file = new File(inputPath);
                if (file.exists()) {
                    boolean delete = file.delete();
                    LogUtil.logInfo(delete ? inputPath + "文件删除成功" : inputPath + "文件删除失败", null);
                }
                file = new File(outputPath);
                TDownloadManager.updateDownloadSuccess(m3u8Path, taskId, file.length());
                EventBus.getDefault().post(new DownloadEvent(taskId, vodTitle, vodEpisodes, m3u8Path, file.length(), 1));
                notificationUtils.uploadInfo(notificationId, vodTitle, vodEpisodes, Utils.getString(R.string.convertTSToMP4SuccessMsg));
            } else {
                LogUtil.logInfo("转换出错，错误代码:", returnCode+"");
                TDownloadManager.updateDownloadError(m3u8Path, taskId, 0);
                EventBus.getDefault().post(new DownloadEvent(taskId, vodTitle, vodEpisodes, m3u8Path, 0, 2));
                notificationUtils.uploadInfo(notificationId, vodTitle, vodEpisodes, String.format(Utils.getString(R.string.convertTSToMP4ErrorMsg), returnCode));
            }
        });
    }
}
