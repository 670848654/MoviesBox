package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.HomeContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/30 20:59
 */
public class HomeModel extends BaseModel implements HomeContract.Model {
    private HomeContract.LoadDataCallback callback;
    @Override
    public void getData(HomeContract.LoadDataCallback callback) {
        this.callback = callback;
        String defaultDomain = parserInterface.getDefaultDomain();
        if (parserInterface.getSource() == SourceEnum.SourceIndexEnum.FIVE_MOVIE.index && defaultDomain.endsWith(".shop")) {
            // 555电影特殊处理
            defaultDomain += "/index/home.html";
        }

        if (SharedPreferencesUtils.getByPassCF()) {
            // 使用webview
            App.startMyService(defaultDomain, FuckCFEnum.HOME.name());
        } else {
            if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
                // 使用http post
            } else
                OkHttpUtils.getInstance().doGet(defaultDomain, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.error(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String source = getBody(response);
                            parserHtml(source, response);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.error(e.getMessage());
                        }
                    }
                });
        }
    }

    /**
     * 解析数据
     * @param html
     * @param response
     */
    private void parserHtml(String html, Response response) {
        List<MainDataBean> mainDataBeans = parserInterface.parserMainData(html);
        if (!Utils.isNullOrEmpty(mainDataBeans))
            callback.success(mainDataBeans);
        else
            callback.error(response != null ? parserErrorMsg(response, html) : truncateString(html));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.HOME.name())) {
            parserHtml(event.getSource(), null);
        }
    }
}
