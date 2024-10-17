package my.project.moviesbox.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DanmuDataBean;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 弹幕
 */
public class DanmuModel extends BaseModel implements DanmuContract.Model {
    private DanmuContract.LoadDataCallback callback;
    @Override
    public void getDanmu(DanmuContract.LoadDataCallback callback, String... params) {
        if (Utils.isNullOrEmpty(params))
            return;
        String url = parserInterface.getDanmuUrl(params);
        if (Utils.isNullOrEmpty(url)) {
            callback.errorDanmu("获取弹幕接口出错");
            return;
        }
        this.callback = callback;
        if (SharedPreferencesUtils.getByPassCF()) {
            switch (sourceEnum) {
                // 只有ANFUS才走这里
                case ANFUNS:
                    App.startMyService(url, FuckCFEnum.DANMU.name());
                    break;
            }
        }
        else if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
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
                        parserDamu(danmu);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.errorDanmu(e.getMessage());
                    }
                }
            });
    }

    /**
     * VIP解析使用的弹幕 自用
     * @param url
     * @param callback
     */
    @Override
    public void getVipDanmu(String url, DanmuContract.LoadDataCallback callback) {
        if (Utils.isNullOrEmpty(url))
            callback.errorDanmu("获取弹幕接口出错");
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
                        List<DanmuDataBean> danmuDataBeanList;
                        try {
                            danmuDataBeanList = new ArrayList<>();
                            JSONObject jsonObject = JSON.parseObject(danmu);
                            if (jsonObject.containsKey("danmuku")) {
                                JSONArray danmukuArray = jsonObject.getJSONArray("danmuku");
                                for (int i=0,size=danmukuArray.size(); i<size; i++) {
                                    JSONArray danmukuItem = danmukuArray.getJSONArray(i);
                                    // 弹幕时间
                                    String timeText = danmukuItem.getString(0);
                                    float time = Float.parseFloat(timeText) * 1000L;
                                    // 弹幕方向 貌似只有right
                                    // String direction = danmukuItem.getString(1);
                                    // 弹幕颜色
                                    String color = danmukuItem.getString(2);
                                    // 弹幕字体大小
                                    String fontSizeText = danmukuItem.getString(3);
                                    int fontSize = DanmuDataBean.DEFAULT_TEXT_SIZE;
                                    try {
                                        fontSize = Integer.parseInt(fontSizeText.replaceAll("px", ""));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    // 弹幕内容
                                    String content = danmukuItem.getString(4);
                                    danmuDataBeanList.add(
                                            new DanmuDataBean(
                                                    content,
                                                    color,
                                                    DanmuDataBean.TYPE_ROLL,
                                                    (long) time,
                                                    fontSize
                                            )
                                    );
                                }
                                callback.successDanmuJson(danmuDataBeanList);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.errorDanmu("弹幕接口返回JSON格式异常，内容解析失败！");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.errorDanmu(e.getMessage());
                }
            }
        });
    }

    /**
     * 解析弹幕内容
     * @param source
     */
    public void parserDamu(String source) {
        if (Utils.isNullOrEmpty(source))
            callback.errorDanmu(Utils.getString(R.string.errorDanmuMsg));
        else {
            if (parserInterface.getDanmuResultJson()) {
                /**
                 * <p>这里是返回JSON，需要自己实现JSON处理封装实体对象</p>
                 * @see DanmuDataBean
                 * <p>示例：</p>
                 */
                List<DanmuDataBean> danmuDataBeanList;
                try {
                    danmuDataBeanList = new ArrayList<>();
                    switch (sourceEnum) {
                        case YJYS:
                            // 缘觉影视
                            JSONArray jsonArray = JSONArray.parseArray(source);
                            LogUtil.logInfo("danmu", JSON.toJSONString(jsonArray));
                            for (int i=0,size=jsonArray.size(); i<size; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                float time = jsonObject.getFloat("time") * 1000;
                                danmuDataBeanList.add(
                                        new DanmuDataBean(
                                                jsonObject.getString("text"),
                                                jsonObject.getString("color"),
                                                jsonObject.getInteger("mode"),
                                                (long) time,
                                                DanmuDataBean.DEFAULT_TEXT_SIZE
                                        )
                                );
                            }
                            callback.successDanmuJson(danmuDataBeanList);
                            break;
                    }
                } catch (Exception e) {
                    callback.errorDanmu("弹幕接口返回JSON格式异常，内容解析失败！");
                }
            } else {
                // 返回XML
                switch (sourceEnum) {
                    case SILISILI:
                    case ANFUNS:
                    case NYYY:
                        callback.successDanmuXml(source);
                        break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.DANMU.name())) {
            switch (sourceEnum) {
                case ANFUNS:
                    // 解析HTML
                    Document doc = Jsoup.parse(event.getSource());
                    // 获取存放XML的div
                    Element xmlElement = doc.getElementById("webkit-xml-viewer-source-xml");
                    if (xmlElement != null) {
                        // 提取i标签中的XML内容
                        String xmlContent = xmlElement.html();
                        parserDamu(xmlContent);
                    }
                    break;
            }
        }
    }
}
