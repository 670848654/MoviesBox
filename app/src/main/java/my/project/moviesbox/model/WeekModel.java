package my.project.moviesbox.model;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.WeekContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeekModel extends BaseModel implements WeekContract.Model {
    private WeekContract.LoadDataCallback callback;

    @Override
    public void getWeekData(String url, WeekContract.LoadDataCallback callback) {
        url = getHttpUrl(url);
        this.callback = callback;
        if (SharedPreferencesUtils.getByPassCF()) {
            App.startMyService(url, FuckCFEnum.WEEK.name());
        } else {
            if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
                // 使用http post
            } else {
                OkHttpUtils.getInstance().doGet(url, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        callback.error(e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
    }

    /**
     * 解析数据
     * @param html
     * @param response
     */
    private void parserHtml(String html, Response response) {
        List<WeekDataBean> weekDataBeans = parserInterface.parserWeekDataList(html);
        if (Utils.isNullOrEmpty(weekDataBeans))
            callback.error(response != null ? parserErrorMsg(response, html) : html);
        else if (weekDataBeans.size() > 0)
            callback.weekSuccess(weekDataBeans);
        else
            callback.error(Utils.getString(R.string.loadErrorMsg));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.WEEK.name())) {
            parserHtml(event.getSource(), null);
        }
    }
}
