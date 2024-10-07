package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.TextListContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/24 15:40
 */
public class TextListModel extends BaseModel implements TextListContract.Model {
    private TextListContract.LoadDataCallback callback;

    @Override
    public void getData(String url, TextListContract.LoadDataCallback callback) {
        url = parserInterface.getTextUrl(url);
        this.callback = callback;
        if (SharedPreferencesUtils.getByPassCF()) {
            App.startMyService(url, FuckCFEnum.TEXT_LIST.name());
        } else {
            if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
                // 使用http post
            } else
                OkHttpUtils.getInstance().doGet(url, new Callback() {

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
        List<TextDataBean> textDataBeans = parserInterface.parserTextList(html);
        if (Utils.isNullOrEmpty(textDataBeans))
            callback.error(response != null ? parserErrorMsg(response, html) : html);
        else if (textDataBeans.size() > 0)
            callback.success(textDataBeans);
        else
            callback.empty(Utils.getString(R.string.emptyData));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.TEXT_LIST.name())) {
            parserHtml(event.getSource(), null);
        }
    }
}
