package my.project.moviesbox.config;

import androidx.annotation.Nullable;

import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.IKeyUrlConverter;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.core.processor.IVodTsUrlConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.VideoUtils;
/**
  * @包名: my.project.moviesbox.config
  * @类名: M3U8DownloadConfig
  * @描述: M3U8下载配置
  * @作者: Li Z
  * @日期: 2024/1/22 19:48
  * @版本: 1.0
 */
public class M3U8DownloadConfig {
    /**
     * M3U8下载配置
     * @return
     */
    public M3U8VodOption setM3U8Option() {
        M3U8VodOption m3U8VodOption = new M3U8VodOption();
        boolean ignoreTs = SharedPreferencesUtils.getIgnoreTs();
        if (ignoreTs)
            m3U8VodOption.ignoreFailureTs();
        m3U8VodOption.setUseDefConvert(false);
        m3U8VodOption.setBandWidthUrlConverter(new M3U8DownloadConfig.BandWidthUrlConverter());
        m3U8VodOption.setVodTsUrlConvert(new M3U8DownloadConfig.VodTsUrlConverter());
        m3U8VodOption.setKeyUrlConverter(new KeyUrlConverter());
        m3U8VodOption.setMergeHandler(new M3U8DownloadConfig.TsMergeHandler());
        m3U8VodOption.setMaxTsQueueNum(SharedPreferencesUtils.getMaxTsQueueNum());
//        m3U8VodOption.generateIndexFile();
        return m3U8VodOption;
    }

    /**
      * @包名: my.project.moviesbox.config
      * @类名: M3U8DownloadConfig
      * @描述: 获取bandWidthUrl
      * @作者: Li Z
      * @日期: 2024/1/22 19:49
      * @版本: 1.0
     */
    public static class BandWidthUrlConverter implements IBandWidthUrlConverter {
        @Override
        public String convert(String m3u8Url, String bandWidthUrl) {
            /*
             * 第一种情况
             * 直接是链接
             */
            if (bandWidthUrl.startsWith("http")) {
                LogUtil.logInfo("bandWidthUrl", bandWidthUrl);
                return bandWidthUrl;
            }
            // 第二种情况
            /*
             * 第二种情况
             * bandWidthUrl 可能存在二级域名
             */
            else if (bandWidthUrl.startsWith("/")) {
                try {
                    URL url = new URL(m3u8Url);
                    String protocol = url.getProtocol();
                    String hostname = url.getHost();
                    m3u8Url = protocol + "://" + hostname;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                LogUtil.logInfo("bandWidthUrl", m3u8Url + bandWidthUrl);
                return m3u8Url + bandWidthUrl;
            } else {
                int lastSlashIndex = m3u8Url.lastIndexOf("/");
                if (lastSlashIndex != -1) {
                    m3u8Url = m3u8Url.substring(0, lastSlashIndex + 1);
                }
                LogUtil.logInfo("bandWidthUrl", m3u8Url + bandWidthUrl);
                return m3u8Url + bandWidthUrl;
            }
        }
    }
    /**
      * @包名: my.project.moviesbox.config
      * @类名: M3U8DownloadConfig
      * @描述: 获取TS URL
      * @作者: Li Z
      * @日期: 2024/1/22 19:49
      * @版本: 1.0
     */
    public static class VodTsUrlConverter implements IVodTsUrlConverter {
        @Override
        public List<String> convert(String m3u8Url, List<String> tsUrls) {
            LogUtil.logInfo("m3u8Url", m3u8Url);
            // 转换ts文件的url地址
            String domain = "";
            try {
                URL url = new URL(m3u8Url);
                String protocol = url.getProtocol();
                String hostname = url.getHost();
                domain = protocol + "://" + hostname;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String domainLastSlash = "";
            int lastSlashIndex = m3u8Url.lastIndexOf("/");
            if (lastSlashIndex != -1)
                domainLastSlash = m3u8Url.substring(0, lastSlashIndex + 1);
            List<String> newTsList = new ArrayList<>();
            boolean removeAdTsConfig = SharedPreferencesUtils.getRemoveAdTs();
            String startWith = "";
            List<String> adTsUrl = new ArrayList<>();
            if (removeAdTsConfig) {
                // 如果开启了尝试移除广告切片规则
                String firstTsUrl = tsUrls.get(0);
                if (firstTsUrl.startsWith("/")) {
                    // 第一种格式 /20230401/85Q5roRa/hls/mTQUpttO.ts
                    int num = countSlashes(firstTsUrl);
                    if (num >= 2) {
                        startWith = getDataBeforeSecondSlash(firstTsUrl);
                    }
                } else if (firstTsUrl.startsWith("http")){
                    // 第二种格式带域名 https://hey07.cjkypo.com/20220519/zpFJBZ3s/1100kb/hls/cI5Ty8nv.ts
                    try {
                        URI uri = new URI(firstTsUrl);
                        String protocol = uri.getScheme(); // 获取协议
                        String host = uri.getHost(); // 获取主机名
                        startWith = protocol + "://" + host;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else
                    startWith = "";
            }
            for (String tsUrl : tsUrls) {
                if (removeAdTsConfig) {
                    // 开启移除广告规则
                    if (startWith.isEmpty()) {
                        if (!tsUrl.startsWith("http"))
                            newTsList.add(domainLastSlash + tsUrl);
                    } else if (startWith.startsWith("/") && tsUrl.startsWith(startWith)) {
                        newTsList.add(domain + tsUrl);
                    } else if (startWith.startsWith("http") && tsUrl.startsWith(startWith)) {
                        newTsList.add(tsUrl);
                    }
                } else {
                    if (tsUrl.startsWith("http"))
                        newTsList.add(tsUrl);
                    else if (tsUrl.startsWith("/"))
                        newTsList.add(domain + tsUrl);
                    else
                        newTsList.add(domainLastSlash + tsUrl);
                }
            }
            return newTsList; // 返回有效的ts文件url集合
        }

        public static int countSlashes(String str) {
            int count = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '/') {
                    count++;
                }
            }
            return count;
        }

        private static String getDataBeforeSecondSlash(String url) {
            // 找到第一个斜杠的位置
            int firstSlashIndex = url.indexOf('/');
            if (firstSlashIndex != -1) {
                // 找到第二个斜杠的位置
                int secondSlashIndex = url.indexOf('/', firstSlashIndex + 1);
                if (secondSlashIndex != -1) {
                    // 提取第一个斜杠到第二个斜杠之间的子字符串，包含第一个斜杠
                    return url.substring(0, secondSlashIndex);
                }
            }
            return null; // 如果没有找到第二个斜杠，则返回null或者可以根据需要返回其他值
        }
    }

    public static class KeyUrlConverter implements IKeyUrlConverter {

        /**
         * 将被加密的密钥下载地址转换为可使用的http下载地址
         *
         * @param m3u8Url   主m3u8的url地址
         * @param tsListUrl m3u8切片列表url地址
         * @param keyUrl    加密的url地址
         * @return 可正常访问的http地址
         */
        @Override
        public String convert(String m3u8Url, String tsListUrl, String keyUrl) {
            if (keyUrl.startsWith("http")) {
                LogUtil.logInfo("keyUrl", keyUrl);
                return keyUrl;
            }

            try {
                URL baseUrl = new URL(m3u8Url);
                if (keyUrl.startsWith("/")) {
                    // 绝对路径相对 host，例如：/videos/xxx.key
                    String resolved = baseUrl.getProtocol() + "://" + baseUrl.getHost() + keyUrl;
                    LogUtil.logInfo("keyUrl", resolved);
                    return resolved;
                } else {
                    // 相对路径相对 m3u8 的路径
                    String basePath = m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1);
                    String resolved = basePath + keyUrl;
                    LogUtil.logInfo("keyUrl", resolved);
                    return resolved;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return keyUrl;
            }
        }
    }

    /**
      * @包名: my.project.moviesbox.config
      * @类名: M3U8DownloadConfig
      * @描述: 合并TS
      * @作者: Li Z
      * @日期: 2024/1/22 19:50
      * @版本: 1.0
     */
    public static class TsMergeHandler implements ITsMergeHandler {
        public boolean merge(@Nullable M3U8Entity m3U8Entity, List<String> tsPath) {
            LogUtil.logInfo("TsMergeHandler", "开始合并TS....");
//            String tsKey = m3U8Entity.getKeyPath() == null ? "" : VideoUtils.readKeyInfo2String(new File(m3U8Entity.getKeyPath()));
//            String keyPath = m3U8Entity.getKeyPath();
            String keyPath = replaceWithKeyExtension(m3U8Entity.getFilePath());
            File keyFile = new File(keyPath);
            OutputStream outputStream = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            List<File> finishedFiles = new ArrayList<>();
            for (String path : tsPath) {
                try {
                    File pathFile = new File(path);
                    if (keyFile.exists()) {
                        byte[] tsKey = null;
                        byte[] tsIv = null;
//            tsKey = VideoUtils.readKeyInfo2Byte(new File(m3U8Entity.getKeyPath()));
                        tsKey = VideoUtils.readKeyInfo2Byte(new File(keyPath));
                        tsIv = m3U8Entity.getIv() == null ? new byte[16] : m3U8Entity.getIv().getBytes();
                        String encryptedInformation = "TS分片存在加密; key=%s; iv=%s";
                        LogUtil.logInfo("TsMergeHandler", String.format(encryptedInformation, new String(tsKey), new String(tsIv)));
                        // 存在加密
                        inputStream= new FileInputStream(pathFile);
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes);
                        fileOutputStream = new FileOutputStream(pathFile);
                        // 解密ts片段
                        fileOutputStream.write(VideoUtils.decrypt(bytes, tsKey, tsIv));
                    }
                    finishedFiles.add(pathFile);
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) outputStream.close();
                        if (inputStream != null) inputStream.close();
                        if (fileOutputStream != null) fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return VideoUtils.merge(m3U8Entity.getFilePath(), finishedFiles);
        }
    }

    public static String replaceWithKeyExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;

        File file = new File(filePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');

        String baseName = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
        String parentPath = file.getParent();

        return new File(parentPath, baseName + ".key").getAbsolutePath();
    }
}
