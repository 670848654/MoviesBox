package my.project.moviesbox.strategy.danmu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DanmuDataBean;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: GiriGiriLove 弹幕策略实现
 * @date 2025/6/20 9:14
 */
public class GiriGiriLoveDanmuStrategy extends BaseDanmuStrategy {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final String ORIGIN = "https://m3u8.girigirilove.com";

    @Override
    protected void doRequest(String url, DanmuContract.LoadDataCallback callback) {
        // 自定义属性，用于区分弹幕接口和参数
        String[] dataStr = url.split(",");
        if (dataStr.length < 3) {
            callback.errorDanmu("URL参数错误：" + url);
            return;
        }

        JSONObject json = new JSONObject();
        json.put("play_url", dataStr[2]);

        Headers headers = new Headers.Builder()
                .set("User-Agent", USER_AGENT)
                .set("Origin", ORIGIN)
                .build();

        parseVodOutScrollingApi(dataStr[0], dataStr[1], headers, json, callback);
    }

    /**
     * 外站弹幕数据
     * 如果外站弹幕数据为空则获取本站弹幕数据
     */
    private void parseVodOutScrollingApi(String vodOutScrollingApi, String scrollingApi, Headers headers, JSONObject json,
                           DanmuContract.LoadDataCallback callback) {

        postJson(vodOutScrollingApi, headers, json, (result) -> {
            Integer code = result.getInteger("code");
            if (!Integer.valueOf(1).equals(code)) {
                callback.errorDanmu("弹幕接口返回异常：" + result.getString("info"));
                return;
            }

            String info = result.getString("info");
            if (Utils.isNullOrEmpty(info)) {
                // 获取本站JSON格式弹幕
                parseScrollingApi(scrollingApi, headers, json, callback);
            } else if (info.contains(".xml")) {
                // XML格式弹幕
                get(info, (xml) -> parseDanmu(xml, DanmuResultEnum.XML, callback),
                        callback::errorDanmu);
            } else {
                callback.errorDanmu("未知弹幕格式：" + info);
            }
        }, callback::netErrorDanmu);
    }

    /**
     * 本站弹幕数据
     */
    private void parseScrollingApi(String scrollingApi, Headers headers, JSONObject json,
                           DanmuContract.LoadDataCallback callback) {

        postJson(scrollingApi, headers, json, (result) -> {
            Integer code = result.getInteger("code");
            if (!Integer.valueOf(1).equals(code)) {
                callback.errorDanmu("弹幕接口返回异常：" + result.getString("info"));
                return;
            }
            parseDanmu(result.getString("info"), DanmuResultEnum.JSON, callback);
        }, callback::netErrorDanmu);
    }

    @Override
    protected List<DanmuDataBean> parseDanmuJson(String jsonSource) {
        JSONArray jsonArray = JSONArray.parseArray(jsonSource);
        List<DanmuDataBean> list = new ArrayList<>();

        for (Object obj : jsonArray) {
            if (!(obj instanceof JSONObject)) continue;
            JSONObject scrollJson = ((JSONObject) obj).getJSONObject("scroll_json");
            if (scrollJson == null) continue;

            JSONObject styleJson = scrollJson.getJSONObject("style");
            long timeMs = (long) (scrollJson.getFloatValue("time") * 1000);
            list.add(new DanmuDataBean(
                    scrollJson.getString("content"),
                    Utils.isNullOrEmpty(styleJson) ? "" : styleJson.getString("color"),
                    scrollJson.getInteger("mode"),
                    timeMs
            ));
        }
        return list;
    }

    /* ===== 通用请求方法 ===== */
    private void postJson(String url, Headers headers, JSONObject json,
                          Consumer<JSONObject> onSuccess, Consumer<String> onError) {
        OkHttpUtils.getInstance().doPostJson(url, headers, json.toJSONString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError.accept("访问弹幕接口失败：" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onError.accept("获取弹幕接口响应失败：" + response);
                    return;
                }
                String body = response.body().string();
                LogUtil.logInfo("弹幕接口响应", body);
                onSuccess.accept(JSON.parseObject(body));
            }
        });
    }

    private void get(String url, Consumer<String> onSuccess, Consumer<String> onError) {
        OkHttpUtils.getInstance().doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError.accept("访问弹幕接口失败：" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onError.accept("获取弹幕接口响应失败：" + response);
                    return;
                }
                onSuccess.accept(response.body().string());
            }
        });
    }
}
