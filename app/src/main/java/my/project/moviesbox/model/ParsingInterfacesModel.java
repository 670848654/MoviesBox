package my.project.moviesbox.model;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.digest.DigestUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 虾米解析[https://jx.xmflv.cc/]接口 v2.0
 * @date 2024/2/21 8:37
 */
public class ParsingInterfacesModel extends BaseModel implements ParsingInterfacesContract.Model {
    private final static String XMJX = "https://59.153.166.174:4433/xmflv.js";
    private static final String IV = "https://t.me/xmflv666";
    private static final String TIME_URL = "https://data.video.iqiyi.com/v.f4v";

    @Override
    public void parser(String url, ParsingInterfacesContract.LoadDataCallback callback) {
        try {
            String encodedUrl = URLEncoder.encode(url, "UTF-8");
            // Step 1: 构造公共请求头
            Headers headers = buildCommonHeaders(false);
            // Step 2: 请求时间戳和 area
            OkHttpUtils.getInstance().doGet(TIME_URL, headers, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.error("请求获取时间戳接口失败：" + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        String responseStr = getBody(response);
                        JSONObject respJson = JSONObject.parseObject(responseStr);
                        String time = respJson.getString("time");
                        String area = respJson.getString("t");
                        // Step 3: 生成签名
                        String sign = generateSign(time, encodedUrl);
                        LogUtil.logInfo("生成签名", sign);
                        // Step 4: 构造POST请求
                        Headers postHeaders = buildCommonHeaders(true);
                        FormBody body = new FormBody.Builder()
                                .add("wap", "1")
                                .add("url", encodedUrl)
                                .add("time", time)
                                .add("key", sign)
                                .add("area", area)
                                .build();
                        OkHttpUtils.getInstance().doPostDefault(XMJX, postHeaders, body, new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                callback.error("POST请求失败：" + e.getMessage());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                try {
                                    String result = getBody(response);
                                    JSONObject json = JSONObject.parseObject(result);
                                    LogUtil.logInfo("解析返回数据", json.toJSONString());
                                    callback.success(json);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    callback.error("解析响应失败：" + e.getMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error("解析获取时间戳接口响应失败：" + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.error("异常：" + e.getMessage());
        }
    }

    /**
     * 抽离方法
     * @param isPost
     * @return
     */
    private Headers buildCommonHeaders(boolean isPost) {
        Headers.Builder builder = new Headers.Builder();
        builder.set("Accept", "application/json, text/javascript, */*; q=0.01");
        builder.set("Accept-Encoding", "gzip, deflate, br");
        builder.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        builder.set("Origin", "https://jx.xmflv.com");
        builder.set("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
        builder.set("Sec-Ch-Ua-Mobile", "?0");
        builder.set("Sec-Ch-Ua-Platform", "\"Windows\"");
        builder.set("Sec-Fetch-Dest", "empty");
        builder.set("Sec-Fetch-Mode", "cors");
        builder.set("Sec-Fetch-Site", "cross-site");
        builder.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
        if (isPost) {
            builder.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        }
        return builder.build();
    }


    /**
     * 生成参数签名
     * @param time  // 服务器来的验证时间
     * @param url   // 待解析的视频地址
     * @return
     * @throws Exception
     */
    public static String generateSign(String time, String url) throws Exception {
        String input = DigestUtils.md5Hex(time + url);         // MD5(time + url)
        String keyStr = DigestUtils.md5Hex(input);                  // 再 MD5 一次，作为 key
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = IV.getBytes(StandardCharsets.UTF_8);

        // 固定 IV 长度为 16 字节
        if (ivBytes.length != 16) {
            byte[] fixedIv = new byte[16];
            System.arraycopy(ivBytes, 0, fixedIv, 0, Math.min(ivBytes.length, 16));
            ivBytes = fixedIv;
        }

        byte[] inputBytes = padZero(input.getBytes(StandardCharsets.UTF_8), 16);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(inputBytes);
        return Base64.encodeBase64String(encrypted);
    }

    private static byte[] padZero(byte[] input, int blockSize) {
        int padding = blockSize - (input.length % blockSize);
        if (padding == blockSize) padding = 0;
        byte[] padded = new byte[input.length + padding];
        System.arraycopy(input, 0, padded, 0, input.length);
        return padded;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
