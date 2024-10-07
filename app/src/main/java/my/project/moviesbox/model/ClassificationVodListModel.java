package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.ClassificationVodListContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
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
public class ClassificationVodListModel extends BaseModel implements ClassificationVodListContract.Model {
    private ClassificationVodListContract.LoadDataCallback callback;
    private boolean firstTimeData;
    @Override
    public void getData(boolean firstTimeData, ClassificationVodListContract.LoadDataCallback callback, String... param) throws UnsupportedEncodingException {
        this.callback = callback;
        this.firstTimeData = firstTimeData;
        String url = parserInterface.getClassificationUrl(param);
        if (SharedPreferencesUtils.getByPassCF()) {
            App.startMyService(url, FuckCFEnum.CLASSIFICATION_VOD_LIST.name());
        } else {
            if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
                // 使用http post
            } else
                OkHttpUtils.getInstance().doGet(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.errorVodList(firstTimeData, e.getMessage());
                        callback.errorClassList(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String source = getBody(response);
                            parserHtml(source, response);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.errorVodList(firstTimeData, e.getMessage());
                            callback.errorClassList(e.getMessage());
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
            List<VodDataBean> vodDataBean = parserInterface.parserClassificationVodList(html);
        int pageCount = firstTimeData ? parserInterface.parserPageCount(html) : parserInterface.startPageNum();
        String errorMsg = response != null ? parserErrorMsg(response, html) : html;
        if (vodDataBean == null)
            callback.errorVodList(firstTimeData, errorMsg);
        else if (vodDataBean.size() > 0)
            callback.successVodList(firstTimeData, vodDataBean, pageCount);
        else
            callback.emptyVodList(firstTimeData, firstTimeData ? Utils.getString(R.string.emptyData) : errorMsg);
        if (firstTimeData) {
            List<ClassificationDataBean> classificationDataBeans = parserInterface.parserClassificationList(html);
            if (Utils.isNullOrEmpty(classificationDataBeans))
                callback.error(response != null ? parserErrorMsg(response, html) : html);
            else if (classificationDataBeans.size() > 0)
                callback.successClassList(classificationDataBeans);
            else
                callback.emptyClassList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.CLASSIFICATION_VOD_LIST.name())) {
            parserHtml(event.getSource(), null);
        }
    }
}
