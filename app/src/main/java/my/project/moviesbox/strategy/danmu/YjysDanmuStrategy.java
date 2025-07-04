package my.project.moviesbox.strategy.danmu;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.DanmuDataBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: YJYS 弹幕策略实现
 * @date 2025/6/20 9:17
 */
public class YjysDanmuStrategy extends BaseDanmuStrategy {

    public YjysDanmuStrategy(boolean resultIsJson) {
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

    /**
     * JSON 格式解析
     * @param jsonSource
     * @return
     */
    @Override
    protected List<DanmuDataBean> parseDanmuJson(String jsonSource) {
        JSONArray jsonArray = JSONArray.parseArray(jsonSource);
        List<DanmuDataBean> list = new ArrayList<>();
        for (Object object : jsonArray) {
            if (!(object instanceof JSONObject)) continue;
            JSONObject obj = (JSONObject) object;
            float time = obj.getFloat("time") * 1000;
            // 转换格式
            list.add(new DanmuDataBean(
                    obj.getString("text"),
                    obj.getString("color"),
                    obj.getInteger("mode"),
                    (long) time
            ));
        }
        return list;
    }
}