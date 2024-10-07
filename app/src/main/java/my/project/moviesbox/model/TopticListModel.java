package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.TopTicListContract;
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
public class TopticListModel extends BaseModel implements TopTicListContract.Model {
    private TopTicListContract.LoadDataCallback callback;
    private boolean firstTimeData;
    private boolean isVodList;
    @Override
    public void getData(boolean firstTimeData, String url, boolean isVodList, int page, TopTicListContract.LoadDataCallback callback) throws UnsupportedEncodingException {
//        url = getHttpUrl(url);
        url = parserInterface.getTopticUrl(url, page);
        this.callback = callback;
        this.firstTimeData = firstTimeData;
        this.isVodList = isVodList;
        if (SharedPreferencesUtils.getByPassCF()) {
            App.startMyService(url, FuckCFEnum.TOPTIC_LIST.name());
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
        int pageCount = firstTimeData ? parserInterface.parserPageCount(html) : parserInterface.startPageNum();
        List<VodDataBean> vodDataBean = isVodList ? parserInterface.parserTopticVodList(html) : parserInterface.parserTopticList(html);
        if (Utils.isNullOrEmpty(vodDataBean))
            callback.error(response != null ? parserErrorMsg(response, html) : html);
        else if (vodDataBean.size() > 0)
            callback.success(firstTimeData, vodDataBean, pageCount);
        else
            callback.empty(Utils.getString(R.string.emptyData));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.TOPTIC_LIST.name())) {
            parserHtml(event.getSource(), null);
        }
    }
}
