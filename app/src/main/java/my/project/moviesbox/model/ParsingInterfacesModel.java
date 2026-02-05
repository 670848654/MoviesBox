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
 * @description: 虾米解析[https://jx.xmflv.cc/]接口 v3.0
 * @date 2024/2/21 8:37
 */
public class ParsingInterfacesModel extends BaseModel implements ParsingInterfacesContract.Model {
    // 接口地址 v3
    public final static String PARSER_API = "https://202.189.8.170/Api";
    // 弹幕、剧集使用的接口地址前缀
    public final static String NORMAL_API_START = "https://dmku.hls.one";
    // 获取弹幕接口
    public final static String DMKU_API = "https://dmku.hls.one/?ac=dm&url=%s";
    // 获取剧集接口
    public final static String EPISODES_API = "https://dmku.hls.one/?ac=list&url=%s";
    // 偏移量（可能随时会变）
    private static final String IV = "fUU9eRmkYzsgbkEK";
    // 获取时间接口
    private static final String TIME_URL = "https://data.video.iqiyi.com/v.f4v";

    @Override
    public void parser(String url, boolean isEpisodes, ParsingInterfacesContract.LoadDataCallback callback) {
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
                        String key = DigestUtils.md5Hex(time + encodedUrl);
                        LogUtil.logInfo("hex_md5(tm + url)", key);
                        String sign = generateSign(time, encodedUrl);
                        LogUtil.logInfo("生成签名", sign);
                        // Step 4: 构造POST请求
                        Headers postHeaders = buildCommonHeaders(true);
                        FormBody body = new FormBody.Builder()
                                .add("url", encodedUrl)
                                .add("tm", time)
                                .add("key", key)
                                .add("sign", sign)
                                .build();
                        OkHttpUtils.getInstance().doPostDefault(PARSER_API, postHeaders, body, new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                callback.error("POST请求失败：" + e.getMessage());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                try {
                                    String result = getBody(response);
                                    // {
                                    //    "code": 200,
                                    //    "key": "EBkTzatiwnWcu52m",
                                    //    "iv": "QNovsWSq4YkEjJd2",
                                    //    "data": "zu2C2hYueYYgmpKogaEiPYOUuFl+uWFuj8rEmcpyWrvpyZPNz+F7Zlmq/mLE6xi/G7UwnHh4XSfAvHf8JbQeb/qaOvB2HGoFmZV90V8KyJlrA0Y9oVU7tibA/K13id7oZVYSLDnvutoHiOane4ggBUF34dcDssm7KnUCMEkzyhIwLJaRDPi8N41gnz/ASdqqVhhQSM8W8fsJ2VN/HCF85unaDRe9Bb+3zV1Jd5Db3lWimQbWiJrA70y1hGOPHR6KyUdr0qTVQgbKZ3MoRBqp3IOL+O8KWS2Psa2QUHHkdHHyjDFWquBDfgoqnc/QYzcbYnlOLHjUDtj41ELd9C5d9wuTd3Jkywi3A+f+oDwxKeWHrotZ9QpM8Z6w4naDFEMvXkcVZAC/4+T0TSw1ifILifuExmt3ltd9PytWyLkMOPIimIE8eZOZbexrdPGtsavqfO58TFj+4nMGUDma59os914iYE9mBVU1vPHaDDmOP3Q1eUQRxFt6klrBunazy3CDdnaB5zP/X1WoPiNtmtWEnPwc3OISy/Z2PO5dkJ9QSmY9o9Ms75AC4TXYidJ3B1J8OQ8+aD8UsqdcEHU003kFXtmHGUVJbV3PqfBQGmJR1Bi0PpDpmUkbU4uhwbnOoJH0EKIm+34iZ3JJwaNu38WLP0VNnDwHNg1Emb7MqQCsu/gzXiH107/dwkxjwuOA3KwFQy1bu5P+wkEvqgB3/PO11CqsWlycOXacSmyIyTfMr88=",
                                    //    "message": "🔔当前IP[10]分钟内第[2]次请求.[10]分钟内请可请求[30]次.重置时间剩余[1793]秒",
                                    //    "ip": "182.242.74.146",
                                    //    "iptime": "✅输出成功!"
                                    //}
                                    LogUtil.logInfo("接口返回数据", result);
                                    JSONObject json = JSONObject.parseObject(result);
                                    int code = json.getInteger("code");
                                    if (code == 200) {
                                        // 限制消息
                                        String message = json.getString("message");
                                        // 解密加密数据
                                        String data = json.getString("data");
                                        String aes_key = json.getString("key");
                                        String aes_iv = json.getString("iv");
                                        String normalData = getData(aes_iv, aes_key, data);
                                        LogUtil.logInfo("解密数据", normalData);
                                        // tg:@xmflv{"name":"赘婿第1集苏檀儿宁毅签订契约","pic":"https:\/\/pic5.iqiyipic.com\/image\/20231110\/1d\/8e\/a_100430302_m_601_m9.jpg","type":"hls","url":"https:\/\/202.189.8.170\/Cache\/qiyi\/8ed31f49ec6f08804259da8eea57e566.m3u8?vkey=353934335551414155514541427756574141745455674e5a566c5148415638444251634a57675546447755465851745843414e55","time":"2026-01-08 09:47:15","dmid":"ec6f08804259da8e","form":"https:\/\/www.iqiyi.com\/v_260uudpmizo.html","dmkuapi":"https:\/\/dmku.hls.one","dmku":1,"list":1,"mem":"on","telegraph":"@xmflv"}
                                        // 下面获取上面字符串中的json对象
                                        int start = normalData.indexOf('{');
                                        String normalJsonStr = normalData.substring(start);
                                        JSONObject normalJson = JSONObject.parseObject(normalJsonStr);
                                        normalJson.put("ip-message", message);
                                        if (isEpisodes) {
                                            // 获取剧集列表
                                            Headers.Builder builder = new Headers.Builder();
                                            builder.set("origin", "https://jx.xmflv.cc");
                                            builder.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
                                            OkHttpUtils.getInstance().doGet(String.format(EPISODES_API, normalJson.getString("form")), builder.build(), new Callback() {
                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                    String result = getBody(response);
                                                    // {
                                                    //    "vod_code": 200,
                                                    //    "vod_type": "剧集",
                                                    //    "vod_title": "赘婿",
                                                    //    "vod_year": "视频时长:45:54",
                                                    //    "vod_previous": "",
                                                    //    "vod_next": "https://www.iqiyi.com/v_19vub7y9ztk.html",
                                                    //    "vod_updateTo": "总36集",
                                                    //    "vod_pic": "https://pic7.iqiyipic.com/image/20230628/7f/2e/v_157272864_m_601_m2_405_540.jpg",
                                                    //    "vod_form": "https://www.iqiyi.com/v_260uudpmizo.html",
                                                    //    "vod_desc": "苏檀儿是苏家的大房独女，经商有为，想要将苏家布行发扬光大，却苦于女儿身，为留在苏家，只好想出招婿的法子。苏家的二房苏仲堪和苏文兴父子一直觊觎家产，四处为难苏檀儿。为了名正言顺地继承家业，经营布行，苏檀儿和宁毅签订契约，约法三章——待苏檀儿得到苏家掌印后，宁毅即可恢复自由身。就这样，二人成为了契约夫妻。",
                                                    //    "vod_episodes": [
                                                    //        {
                                                    //            "name": "第1集",
                                                    //            "url": "https://www.iqiyi.com/v_260uudpmizo.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第2集",
                                                    //            "url": "https://www.iqiyi.com/v_19vub7y9ztk.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第3集",
                                                    //            "url": "https://www.iqiyi.com/v_audmpi1img.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第4集",
                                                    //            "url": "https://www.iqiyi.com/v_2cwtww5x494.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第5集",
                                                    //            "url": "https://www.iqiyi.com/v_1zsk921lu50.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第6集",
                                                    //            "url": "https://www.iqiyi.com/v_283s5b4g6h8.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第7集",
                                                    //            "url": "https://www.iqiyi.com/v_nirx83mikk.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第8集",
                                                    //            "url": "https://www.iqiyi.com/v_hzb9aehe5g.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第9集",
                                                    //            "url": "https://www.iqiyi.com/v_gxrs1ye700.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第10集",
                                                    //            "url": "https://www.iqiyi.com/v_ihlf0vic1w.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第11集",
                                                    //            "url": "https://www.iqiyi.com/v_1zerui3wr30.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第12集",
                                                    //            "url": "https://www.iqiyi.com/v_2fluft12vxc.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第13集",
                                                    //            "url": "https://www.iqiyi.com/v_1afd1l9czhs.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第14集",
                                                    //            "url": "https://www.iqiyi.com/v_lnx53i0c38.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第15集",
                                                    //            "url": "https://www.iqiyi.com/v_2ai00l8hv4o.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第16集",
                                                    //            "url": "https://www.iqiyi.com/v_jui23h7eu0.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第17集",
                                                    //            "url": "https://www.iqiyi.com/v_1b0tk1mmdqo.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第18集",
                                                    //            "url": "https://www.iqiyi.com/v_bujkichbk0.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第19集",
                                                    //            "url": "https://www.iqiyi.com/v_230favmiasg.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第20集",
                                                    //            "url": "https://www.iqiyi.com/v_jeqaw0xe84.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第21集",
                                                    //            "url": "https://www.iqiyi.com/v_tqu1138kuo.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第22集",
                                                    //            "url": "https://www.iqiyi.com/v_sb5m7p5748.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第23集",
                                                    //            "url": "https://www.iqiyi.com/v_vyp61v8xx8.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第24集",
                                                    //            "url": "https://www.iqiyi.com/v_15rtcei5o7s.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第25集",
                                                    //            "url": "https://www.iqiyi.com/v_1gibteyycto.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第26集",
                                                    //            "url": "https://www.iqiyi.com/v_m6nbtei2io.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第27集",
                                                    //            "url": "https://www.iqiyi.com/v_1br0x5u2jvw.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第28集",
                                                    //            "url": "https://www.iqiyi.com/v_eijm10wnkc.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第29集",
                                                    //            "url": "https://www.iqiyi.com/v_9twei0skhw.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第30集",
                                                    //            "url": "https://www.iqiyi.com/v_m4mjl0ah20.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第31集",
                                                    //            "url": "https://www.iqiyi.com/v_1viws9dg8g4.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第32集",
                                                    //            "url": "https://www.iqiyi.com/v_1lvicldqf2c.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第33集",
                                                    //            "url": "https://www.iqiyi.com/v_1f3j6z7bmig.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第34集",
                                                    //            "url": "https://www.iqiyi.com/v_1vi7oe51xd0.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第35集",
                                                    //            "url": "https://www.iqiyi.com/v_fzzi9qmcwo.html"
                                                    //        },
                                                    //        {
                                                    //            "name": "第36集",
                                                    //            "url": "https://www.iqiyi.com/v_1fqqcplvpw4.html"
                                                    //        }
                                                    //    ]
                                                    //}
                                                    LogUtil.logInfo("剧集数据", result);
                                                    JSONObject json = JSONObject.parseObject(result);
                                                    json.put("ip-message", message);
                                                    callback.success(json, true);
                                                }

                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                    callback.error("请求接口失败：" + e.getMessage(), true);
                                                }
                                            });
                                        } else
                                            // 直接返回当前剧集解析信息
                                            callback.success(normalJson, false);
                                    } else
                                        callback.error("解析响应失败：" + json.toJSONString(), isEpisodes);
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
        builder.set("Origin", "https://jx.xmflv.cc");
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

    /**
     * 解密数据
     * @param aes_iv
     * @param aes_key
     * @param data
     * @return
     */
    private String getData(String aes_iv, String aes_key, String data) {
        try {
            IvParameterSpec iv = new IvParameterSpec(aes_iv.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec sKeySpec = new SecretKeySpec(aes_key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(data));
            String result = new String(original, StandardCharsets.UTF_8).replaceAll("\u000F", "");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
