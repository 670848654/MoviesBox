package my.project.moviesbox.strategy.danmu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.net.OkHttpUtils;
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
    public GiriGiriLoveDanmuStrategy(boolean resultIsJson) {
        super(resultIsJson);
    }

    @Override
    protected void doRequest(String url, DanmuContract.LoadDataCallback callback) {
        // 这个,是自定义的属性 用于区分弹幕接口和参数
        String danmuApi = url.split(",")[0];
        JSONObject json = new JSONObject();
        json.put("play_url", url.split(",")[1]);

        Headers headers = new Headers.Builder()
                .set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .set("Origin", "https://m3u8.girigirilove.com")
                .build();

        OkHttpUtils.getInstance().doPostJson(danmuApi, headers, json.toJSONString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.errorDanmu(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                JSONObject result = JSON.parseObject(body);
                if (result.getInteger("code") == 1 && result.getString("info").contains(".xml")) {
                    OkHttpUtils.getInstance().doGet(result.getString("info"), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callback.errorDanmu(e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            parseDanmu(response.body().string(), callback);
                        }
                    });
                } else {
                    callback.errorDanmu("不支持的格式：" + result.getString("info"));
                }
            }
        });
    }
}
