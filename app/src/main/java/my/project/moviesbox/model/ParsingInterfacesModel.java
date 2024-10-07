package my.project.moviesbox.model;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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
 * @description: 注释
 * @date 2024/2/21 8:37
 */
public class ParsingInterfacesModel extends BaseModel implements ParsingInterfacesContract.Model {
    private final static String XMJX = "https://122.228.8.29:4433/xmflv.js";
    private final static String AES_IV = "3cccf88181408f19";

    @Override
    public void parser(String url, ParsingInterfacesContract.LoadDataCallback callback) {
        try {
            url = URLEncoder.encode(url, "UTF-8");
            LogUtil.logInfo("url", url);
            // 时间戳
            long time = System.currentTimeMillis();
            String data = time+url;
            LogUtil.logInfo("data", data);
            String dataMd5 = MD5(data);
            LogUtil.logInfo("dataMd5", dataMd5);
            String aesKey = MD5(dataMd5);
            LogUtil.logInfo("aesKry", aesKey);
            String signData = "";
            try {
                IvParameterSpec iv = new IvParameterSpec(AES_IV.getBytes(StandardCharsets.UTF_8));
                SecretKeySpec sKeySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
                byte[] encrypted = cipher.doFinal(dataMd5.getBytes(StandardCharsets.UTF_8));
                signData = Base64.encodeBase64String(encrypted);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            LogUtil.logInfo("signData", signData);
            Headers.Builder headersBuilder = new Headers.Builder();
            headersBuilder.set("Accept", "application/json, text/javascript, */*; q=0.01");
            headersBuilder.set("Accept-Encoding", "gzip, deflate, br");
            headersBuilder.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
            headersBuilder.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            headersBuilder.set("Origin", "https://jx.xmflv.cc");
            headersBuilder.set("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
            headersBuilder.set("Sec-Ch-Ua-Mobile", "?0");
            headersBuilder.set("Sec-Ch-Ua-Platform", "\"Windows\"");
            headersBuilder.set("Sec-Fetch-Dest", "empty");
            headersBuilder.set("Sec-Fetch-Mode", "cors");
            headersBuilder.set("Sec-Fetch-Site", "cross-site");
            headersBuilder.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            formBodyBuilder.add("wap", "1")
                    .add("url", url)
                    .add("time", String.valueOf(time))
                    .add("key", signData);
            OkHttpUtils.getInstance().doPostDefault(XMJX, headersBuilder.build(), formBodyBuilder.build(), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.error("错误：" + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String responseBody = getBody(response);
                        JSONObject jsonObject = JSONObject.parseObject(responseBody);
                        LogUtil.logInfo("responseBody", jsonObject.toJSONString());
                        callback.success(jsonObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error("错误：" + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.error("错误："+e.getMessage());
        }
    }

    public static String MD5(String input) {
        try {
            // 创建MD5消息摘要对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 将输入字符串转换为字节数组，并计算摘要
            byte[] messageDigest = md.digest(input.getBytes());
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
