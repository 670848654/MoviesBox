package my.project.moviesbox.model;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 弹幕
 */
public class DanmuModel extends BaseModel implements DanmuContract.Model {
    @Override
    public void getDanmu(DanmuContract.LoadDataCallback callback, String... params) {
        String url = parserInterface.getDanmuUrl(params);
        if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
            // 使用http post
        } else
            OkHttpUtils.getInstance().doGet(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.errorDanmu(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String danmu = response.body().string();
                        if (Utils.isNullOrEmpty(danmu))
                            callback.errorDanmu(Utils.getString(R.string.errorDanmuMsg));
                        else {
                            if (parserInterface.getDanmuResultJson()) {
                                // 这里是返回JSON，需要自己实现JSON处理 示例：
                                try {
                                    JSONObject jsonObject = JSONObject.parseObject(danmu);
                                    if (jsonObject.getInteger("code") == 200)
                                        callback.successDanmuJson(jsonObject);
                                    else
                                        callback.errorDanmu("接口服务返回异常，请稍后再试！");
                                } catch (Exception e) {
                                    callback.errorDanmu("弹幕接口返回JSON格式异常，内容解析失败！");
                                }
                            } else {
                                // 返回XML
                                /*switch (parserInterface.getSource()) {
                                    case ParserInterfaceFactory.SOURCE_SILISILI:
                                    case ParserInterfaceFactory.SOURCE_ANFUNS:
                                        callback.successDanmuXml(danmu);
                                        break;
                                }*/
                                callback.successDanmuXml(danmu);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.errorDanmu(e.getMessage());
                    }
                }
            });
    }
}
