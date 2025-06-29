package my.project.moviesbox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arialyy.aria.core.task.DownloadTask;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.jzvd.JZMediaInterface;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VodTypeListAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.JZExoPlayer;
import my.project.moviesbox.config.JZMediaIjk;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.service.SniffingVideoService;
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
        App.destroyActivity("player");
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
        StringBuffer sb = new StringBuffer("");
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
        return matcher.find() ? matcher.start() / 2 : 0;
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
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
        builder.setCancelable(true);
        builder.setTitle(Utils.getString(R.string.otherOperation));
        builder.setMessage(msg);
        builder.setPositiveButton(Utils.getString(R.string.defaultPositiveBtnText), (dialog, which) -> dialog.dismiss());
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 发现多个播放地址时弹窗 下载用
     *
     * @param context
     * @param list
     * @param listener
     */
    public static AlertDialog showMultipleVideoSources4Download(Context context,
                                                                List<DialogItemBean> dialogItemBeans,
                                                                OnItemClickListener onItemClickListener) {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.downloadMultipleVideoDialogTitle));
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_dialog_recyclerview, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        VodTypeListAdapter adapter = new VodTypeListAdapter(dialogItemBeans);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    /**
     * 发现多个播放地址时弹窗
     *
     * @param context
     * @param list
     * @param listener
     * @param isPlayerActivity 是否是播放界面
     */
    public static AlertDialog showMultipleVideoSources(Context context,
                                                List<DialogItemBean> dialogItemBeans,
                                                OnItemClickListener onItemClickListener, boolean isPlayerActivity) {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.selectVideoSource));
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_dialog_recyclerview, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        VodTypeListAdapter adapter = new VodTypeListAdapter(dialogItemBeans);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        builder.setView(dialogView);
        if (!isPlayerActivity)
            builder.setNegativeButton(Utils.getString(R.string.defaultNegativeBtnText), null);
        alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
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
     * 启动嗅探服务
     * @param url 播放页地址
     * @param activityEnum EventBus订阅处理判断
     * @param sniffEnum 嗅探结果处理类型
     */
    public static void startSniffing(Context context, String url,
                                     VideoSniffEvent.ActivityEnum activityEnum,
                                     VideoSniffEvent.SniffEnum sniffEnum) {
        Intent intent = new Intent(context, SniffingVideoService.class);
        intent.putExtra("url", url);
        intent.putExtra("activityEnum", activityEnum.name());
        intent.putExtra("sniffEnum", sniffEnum.name());
        context.startService(intent);
    }

    /**
     * 嗅探失败弹窗
     * @param context
     */
    public static void sniffErrorDialog(Activity activity) {
        Utils.showAlert(activity,
                activity.getString(R.string.errorDialogTitle),
                activity.getString(R.string.sniffVodPlayUrlError),
                false,
                activity.getString(R.string.defaultPositiveBtnText),
                "",
                "",
                (dialog, which) -> dialog.dismiss(),
                null,
                null);
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
}
