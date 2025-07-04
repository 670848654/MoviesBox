package my.project.moviesbox.strategy.danmu;

import java.io.IOException;

import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.net.OkHttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 默认返回XML格式弹幕策略实现(GET请求) 例如：SILISILI、ANFUNS、NYYY
 * @date 2025/6/20 9:22
 */
public class DefaultXmlGetDanmuStrategy extends BaseDanmuStrategy {
    public DefaultXmlGetDanmuStrategy(boolean resultIsJson) {
        super(resultIsJson);
    }

    @Override
    protected void doRequest(String url, DanmuContract.LoadDataCallback callback) {
        OkHttpUtils.getInstance().doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.errorDanmu(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String danmu = response.body().string();
                    parseDanmu(danmu, callback);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.errorDanmu(e.getMessage());
                }
            }
        });
    }
}