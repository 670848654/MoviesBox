package my.project.moviesbox.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.moviesbox.parser.LogUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/5/18 1:43
 */
public class KeyDownloader {

    public static void downloadKey(String m3u8Url, String outputDir) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // 获取 m3u8 内容
                String m3u8Content = client.newCall(new Request.Builder().url(m3u8Url).build()).execute().body().string();

                // 匹配 #EXT-X-KEY 的 URI
                Pattern pattern = Pattern.compile("#EXT-X-KEY:METHOD=AES-128,URI=\"(.*?)\"");
                Matcher matcher = pattern.matcher(m3u8Content);
                if (!matcher.find()) throw new RuntimeException("未找到加密 KEY URI");

                String keyUrl = matcher.group(1);
                if (!keyUrl.startsWith("http")) {
                    // 拼接相对路径
                    URL base = new URL(m3u8Url);
                    if (keyUrl.startsWith("/")) {
                        keyUrl = base.getProtocol() + "://" + base.getHost() + keyUrl;
                    } else {
                        String basePath = m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1);
                        keyUrl = basePath + keyUrl;
                    }
                }

                // 下载 .key 文件
                byte[] keyBytes = client.newCall(new Request.Builder().url(keyUrl).build()).execute().body().bytes();

                // 保存到本地
                File keyFile = new File(outputDir+ ".key");
                try (FileOutputStream fos = new FileOutputStream(keyFile)) {
                    fos.write(keyBytes);
                }
                LogUtil.logInfo("key下载成功", keyFile.getAbsolutePath());
            } catch (Exception e) {
                LogUtil.logInfo("key下载失败", e.getMessage());
            }
        }).start();
    }
}