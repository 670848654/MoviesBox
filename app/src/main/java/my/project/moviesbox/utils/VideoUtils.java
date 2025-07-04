package my.project.moviesbox.utils;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.arialyy.aria.core.task.DownloadTask;
import com.arthenica.mobileffmpeg.FFmpeg;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.jzvd.JZMediaInterface;
import my.project.moviesbox.R;
import my.project.moviesbox.config.JZExoPlayer;
import my.project.moviesbox.config.JZMediaIjk;
import my.project.moviesbox.config.NotificationUtils;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.event.DownloadEvent;
import my.project.moviesbox.parser.LogUtil;

/**
  * @包名: my.project.moviesbox.utils
  * @类名: VideoUtils
  * @描述: 视频相关工具类
  * @作者: Li Z
  * @日期: 2024/2/4 16:57
  * @版本: 1.0
 */
public class VideoUtils {

    /**
     * 读取key内容
     * @param file
     * @return
     * @deprecated 请使用 {@link #readKeyInfo2Byte}
     */
    @Deprecated
    public static String readKeyInfo2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!=null){
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString().replaceAll("\\n", "").replaceAll(" ","");
    }

    /**
     * 读取key内容
     * @param file
     * @return
     */
    public static byte[] readKeyInfo2Byte(File file){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * AES解密
     * @param fileBytes ts文件字节数组
     * @param key 密钥
     * @param iv IV
     * @return 解密后的字节数组
     * @deprecated 请使用 {@link #decrypt}
     */
    @Deprecated
    public static byte[] decrypt(byte[] fileBytes, String key, byte[] iv) {
        try {
            if (key.isEmpty())
                return null;
            if (key.length() != 16) {
                LogUtil.logInfo("KeyError", "Key长度不是16位");
                return null;
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            if (iv.length != 16)
                iv = new byte[16];
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            return cipher.doFinal(fileBytes);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * AES解密
     * @param fileBytes ts文件字节数组
     * @param key 密钥
     * @param iv IV
     * @return 解密后的字节数组
     */
    public static byte[] decrypt(byte[] fileBytes, byte[] key, byte[] iv) {
        try {
            if (key.length == 0)
                return null;
            if (key.length != 16) {
                LogUtil.logInfo("KeyError", "Key长度不是16位");
                return null;
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            if (iv.length != 16)
                iv = new byte[16];
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            return cipher.doFinal(fileBytes);
        } catch (Exception ex) {
            LogUtil.logInfo("DecryptError", ex.toString());
            return null;
        }
    }

    /**
     * 合并ts
     * @param savePath
     * @param fileList
     * @return
     */
    public static boolean merge(String savePath, List<File> fileList) {
        /*try {
            File file = new File(savePath.replaceAll("m3u8", "ts"));
            if (file.exists()) file.delete();
            else file.createNewFile();
            FileOutputStream fs = new FileOutputStream(file);
            byte[] b = new byte[4096];
            for (File f : fileList) {
                FileInputStream fileInputStream = new FileInputStream(f);
                ByteArrayOutputStream temp = new ByteArrayOutputStream();
                int len = 0;
                int position = 0;
                while ((len = fileInputStream.read(b)) != -1) {
                    if (len == b.length) {
                        temp.write(b, 0, len);
                        position = getPosition(temp.toByteArray());
                        break;
                    }
                }
                fileInputStream.close();
                fileInputStream = new FileInputStream(f);
                if (position != 0) fileInputStream.skip(position);
                while ((len = fileInputStream.read(b)) != -1) {
                    fs.write(b, 0, len);
                }
                fileInputStream.close();
                fs.flush();
            }
            fs.close();
            LogUtil.logInfo("TsMergeHandler", "合并TS成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.logInfo("TsMergeHandler", "合并TS失败，请重新下载....");
            return false;
        }*/
        try {
            File file = new File(savePath.replaceAll("m3u8", "ts"));
            if (file.exists()) file.delete();
            file.createNewFile();

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                byte[] buffer = new byte[8192]; // 使用更大的缓冲区

                for (File f : fileList) {
                    int position = 0;

                    // 第一次读取，获取位置
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
                        ByteArrayOutputStream temp = new ByteArrayOutputStream();
                        int bytesRead;

                        while ((bytesRead = bis.read(buffer)) != -1) {
                            temp.write(buffer, 0, bytesRead);
                            if (temp.size() >= buffer.length) {
                                position = getPosition(temp.toByteArray());
                                break;
                            }
                        }
                    }

                    // 第二次读取，从指定位置开始写入
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
                        if (position != 0) {
                            bis.skip(position);
                        }
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                bos.flush();
            }

            LogUtil.logInfo("TsMergeHandler", "合并TS成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.logInfo("TsMergeHandler", "合并TS失败，请重新下载....");
            return false;
        }
    }

    /**
     * 获取标准mpeg-ts开始的位置，需要记住位置跳过TS伪装成图片的文件头
     * <p>2022年6月1日22:07:50</p>
     * <p>参考 https://blog.csdn.net/feiyu361/article/details/121196667</p>
     * @param src
     * @return
     */
    public static int getPosition (byte[] src) {
        /*StringBuffer sb = new StringBuffer("");
        if (src == null || src.length == 0) {
            return 0;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv);
        }
        Matcher matcher = Pattern.compile("47401110").matcher(sb.toString());
        return matcher.find() ? matcher.start() / 2 : 0;*/
        if (src == null || src.length < 4) return 0;

        for (int i = 0; i < src.length - 3; i++) {
            // 注意：byte 是有符号的，需要和 0xFF 做位运算
            if ((src[i] & 0xFF) == 0x47 &&
                    (src[i + 1] & 0xFF) == 0x40 &&
                    (src[i + 2] & 0xFF) == 0x11 &&
                    (src[i + 3] & 0xFF) == 0x10) {
                return i; // 直接返回匹配位置
            }
        }
        return 0; // 未找到，默认返回开头
    }

    /**
     * 根据任务ID查询数据库信息
     * @param downloadTask
     * @param choose 0 返回影视标题 1 返回影视来源
     * @return
     */
    public static Object getVodInfo(DownloadTask downloadTask, int choose) {
        List<Object> objects = TVideoManager.queryDownloadVodInfo(downloadTask.getEntity().getId());
        return objects.get(choose);
    }

    /**
     * 获取用户设置的播放器内核类型
     * @return
     */
    public static Class<? extends JZMediaInterface> getUserPlayerKernel() {
        int userSetPlayerKernel = SharedPreferencesUtils.getUserSetPlayerKernel();
        if (userSetPlayerKernel == 1) {
            return JZMediaIjk.class;
        } else {
            return JZExoPlayer.class;
        }
    }

    /**
     * 移除某些M3U8广告切片
     * @param filePath
     * @return
     */
    public static boolean removeLocalM3U8Ad(String filePath) {
        try {
            boolean hasAd = false;
            String targetExtinf = "#EXTINF:3.366667,";
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();

            // 处理文件内容
            List<String> modifiedLines = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(targetExtinf)) {
                    hasAd = true;
                    // 跳过当前EXTINF和它下一行的URL
                    i++; // 跳过URL
                } else {
                    // 保留其他行
                    modifiedLines.add(lines.get(i));
                }
            }
            if (hasAd) {
                // 写回文件
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                for (String modifiedLine : modifiedLines) {
                    writer.write(modifiedLine);
                    writer.newLine();
                }
                writer.close();
                LogUtil.logInfo("广告分片删除成功！", "");
            }
            return hasAd;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 使用FFmpeg TS转MP4
     * @param notificationId        通知ID
     * @param m3u8Path              M3U8保存地址
     * @param inputPath             ts地址
     * @param outputPath            mp4地址
     * @param taskId                任务ID
     * @param vodTitle              影视标题
     * @param vodEpisodes           影视集数
     * @param notificationUtils     通知
     */
    public static void convertTSToMP4(int notificationId, String m3u8Path, String inputPath, String outputPath, Long taskId, String vodTitle, String vodEpisodes, NotificationUtils notificationUtils) {
        String[] cmd = {"-i", inputPath, "-acodec", "copy", "-vcodec", "copy", "-absf", "aac_adtstoasc", outputPath};
        long startTime = System.nanoTime();
        // 执行FFmpeg命令
        FFmpeg.executeAsync(cmd, (executionId, returnCode) -> {
            long endTime = System.nanoTime();
            double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
            String timeConsuming = String.format("转码耗时: %.2f 秒", durationInSeconds);
            if (returnCode == RETURN_CODE_SUCCESS) {
                LogUtil.logInfo("转换MP4成功，" + timeConsuming, null);
                // 转换完成删除TS文件
                File file = new File(inputPath);
                if (file.exists()) {
                    boolean delete = file.delete();
                    LogUtil.logInfo(delete ? inputPath + "文件删除成功" : inputPath + "文件删除失败", null);
                }
                // 删除M3U8文件
                file= new File(m3u8Path);
                if (file.exists()) {
                    boolean delete = file.delete();
                    LogUtil.logInfo(delete ? m3u8Path + "文件删除成功" : m3u8Path + "文件删除失败", null);
                }
                file = new File(outputPath);
                TDownloadManager.updateDownloadSuccess(m3u8Path, taskId, file.length());
                EventBus.getDefault().post(new DownloadEvent(taskId, vodTitle, vodEpisodes, m3u8Path, file.length(), 1));
                notificationUtils.uploadInfo(notificationId, vodTitle, vodEpisodes, Utils.getString(R.string.convertTSToMP4SuccessMsg) + "，" + timeConsuming);
            } else {
                LogUtil.logInfo("转换出错，错误代码:", returnCode+"");
                TDownloadManager.updateDownloadError(m3u8Path, taskId, 0);
                EventBus.getDefault().post(new DownloadEvent(taskId, vodTitle, vodEpisodes, m3u8Path, 0, 2));
                notificationUtils.uploadInfo(notificationId, vodTitle, vodEpisodes, String.format(Utils.getString(R.string.convertTSToMP4ErrorMsg), returnCode));
            }
        });
    }

    public static void requestAudioFocus(Context context) {
        Context appContext = context.getApplicationContext(); // 避免万一传入 Activity
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(attributes)
                    .setWillPauseWhenDucked(false)
                    .setAcceptsDelayedFocusGain(false)
                    .build();

            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
    }
}
