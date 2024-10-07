package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.SearchContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 21:46
 */
public class SearchModel extends BaseModel implements SearchContract.Model {
    private SearchContract.LoadDataCallback callback;
    private boolean firstTimeData;
    private String[] param;
    @Override
    public void getData(boolean firstTimeData, SearchContract.LoadDataCallback callback, String... param) throws UnsupportedEncodingException {
        String url = parserInterface.getSearchUrl(param);
        this.callback = callback;
        this.firstTimeData = firstTimeData;
        this.param = param;
        if (SharedPreferencesUtils.getByPassCF()) {
            App.startMyService(url, FuckCFEnum.SEARCH_VOD_LIST.name());
        } else {
            if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
                // 使用http post
            } else
                OkHttpUtils.getInstance().doGet(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.error(firstTimeData, e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String source = getBody(response);
                            parserHtml(source, response);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.error(firstTimeData, e.getMessage());
                        }
                    }
                });
        }

    }

    private void parserHtml(String html, Response response) {
        List<VodDataBean> vodDataBean = parserInterface.parserSearchVodList(html);
        int pageCount = firstTimeData ? parserInterface.parserPageCount(html) : parserInterface.startPageNum();
        String errorMsg = response != null ? parserErrorMsg(response, html) : html;
        if (vodDataBean == null) {
            callback.error(firstTimeData, errorMsg);
        } else if (vodDataBean.size() > 0)
            callback.success(firstTimeData, vodDataBean, pageCount);
        else
            callback.empty(firstTimeData, firstTimeData ? Utils.getString(R.string.emptyData) : errorMsg);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.SEARCH_VOD_LIST.name())) {
            parserHtml(event.getSource(), null);
        }
    }
}
