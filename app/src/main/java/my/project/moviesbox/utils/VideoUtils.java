package my.project.moviesbox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.arialyy.aria.core.task.DownloadTask;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.JZExoPlayer;
import my.project.moviesbox.config.JZMediaIjk;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.view.PlayerActivity;

/**
  * @包名: my.project.moviesbox.utils
  * @类名: VideoUtils
  * @描述: 视频相关工具类
  * @作者: Li Z
  * @日期: 2024/2/4 16:57
  * @版本: 1.0
 */
public class VideoUtils {
    private static AlertDialog alertDialog;

    /**
     * 发现多个播放地址时弹窗 下载用
     *
     * @param context
     * @param list
     * @param listener
     */
    public static void showMultipleVideoSources4Download(Context context,
                                                         List<String> list,
                                                         DialogInterface.OnClickListener listener) {
        String[] items = list.toArray(new String[0]);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.downloadMultipleVideoDialogTitle));
        builder.setCancelable(false);
        builder.setItems(items, listener);
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 打开播放器
     * @param isDescActivity
     * @param activity
     * @param dramaTitle
     * @param url
     * @param vodTitle
     * @param dramaUrl
     * @param list
     * @param clickIndex
     * @param vodId
     * @param nowSource
     */
    public static void openPlayer(boolean isDescActivity, Activity activity, String dramaTitle, String url, String vodTitle, String dramaUrl,
                                  List<DetailsDataBean.DramasItem> list, int clickIndex, String vodId, int nowSource) {
        Bundle bundle = new Bundle();
        bundle.putString("dramaTitle", dramaTitle);
        bundle.putString("url", url);
        bundle.putString("vodTitle", vodTitle);
        bundle.putString("dramaUrl", dramaUrl);
        bundle.putSerializable("list", (Serializable) list);
        bundle.putInt("clickIndex", clickIndex);
        bundle.putString("vodId", vodId);
        bundle.putInt("nowSource", nowSource);
        App.destoryActivity("player");
        if (isDescActivity)
            activity.startActivityForResult(new Intent(activity, PlayerActivity.class).putExtras(bundle), 0x10);
        else {
            activity.startActivity(new Intent(activity, PlayerActivity.class).putExtras(bundle));
            activity.finish();
        }
    }

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
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
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
     * 合并ts
     * @param savePath
     * @param fileList
     * @return
     */
    public static boolean merge(String savePath, List<File> fileList) {
        try {
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
        int position = 0;
        StringBuffer sb = new StringBuffer("");
        if (src == null || src.length <= 0) {
            return position;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv);
        }
        Matcher matcher = Pattern.compile("47401110").matcher(sb.toString());
        if(matcher.find()){
            position = matcher.start()/2;
        }
        return position;
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
     * 相关提示
     * @param context
     * @param msg
     */
    public static void showInfoDialog(Context context, String msg) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
        builder.setCancelable(true);
        builder.setTitle(Utils.getString(R.string.otherOperation));
        builder.setMessage(msg);
        builder.setPositiveButton(Utils.getString(R.string.defaultPositiveBtnText), (dialog, which) -> dialog.dismiss());
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 发现多个播放地址时弹窗
     *
     * @param context
     * @param list
     * @param listener
     * @param isPlayerActivity 是否是播放界面
     */
    public static void showMultipleVideoSources(Context context,
                                                List<String> list,
                                                DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listener2, boolean isPlayerActivity) {
        String[] items = list.toArray(new String[0]);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.selectVideoSource));
        builder.setCancelable(false);
        builder.setItems(items, listener);
        if (!isPlayerActivity)
            builder.setNegativeButton(Utils.getString(R.string.defaultNegativeBtnText), listener2);
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 播放器内核
     * @return
     */
    public static Class getUserPlayerKernel() {
        if (SharedPreferencesUtils.getUserSetPlayerKernel() == 1)
            return JZMediaIjk.class;
        return JZExoPlayer.class;
    }
}
