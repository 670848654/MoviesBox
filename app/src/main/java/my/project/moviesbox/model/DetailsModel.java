package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Objects;

import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.DetailsContract;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 12:07
 */
public class DetailsModel extends BaseModel implements DetailsContract.Model {
    private DetailsContract.LoadDataCallback callback;
    private String finalUrl;

    @Override
    public void getData(String url, DetailsContract.LoadDataCallback callback) {
        url = getHttpUrl(url);
        LogUtil.logInfo("DetailsModel", url);
        finalUrl = url;
        this.callback = callback;
        if (SharedPreferencesUtils.getByPassCF()) {
            App.startMyService(url, FuckCFEnum.DETAILS.name());
        } else {
            if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
                // 使用http post
            } else {
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
    }

    /**
     * 解析数据
     * @param html
     * @param response
     */
    private void parserHtml(String html, Response response) {
        DetailsDataBean detailsDataBean = parserInterface.parserDetails(finalUrl, html);
        if (Utils.isNullOrEmpty(detailsDataBean)) {
            callback.error(response != null ? parserErrorMsg(response, html) : html);
            return;
        }
        String title = detailsDataBean.getTitle();
        //创建剧集索引
        String vodId = TVideoManager.insertVod(title);
        callback.getVodId(vodId);
        // 添加历史记录
        THistoryManager.addOrUpdateHistory(vodId, detailsDataBean.getUrl(), detailsDataBean.getImg());
        //是否收藏
        callback.favorite(TFavoriteManager.checkFavorite(vodId));
        String dramaStr = THistoryManager.queryAllIndex(vodId, false, 0);
        if (!Utils.isNullOrEmpty(detailsDataBean.getDramasList()) && !Utils.isNullOrEmpty(dramaStr) && detailsDataBean.getDramasList().size() > 0) {
            for (DetailsDataBean.Dramas dramas : detailsDataBean.getDramasList()) {
                for (DetailsDataBean.DramasItem item : dramas.getDramasItemList()) {
                    if (dramaStr.contains(item.getUrl())) {
                        item.setSelected(true);
                    }
                }
            }
        }
        callback.success(detailsDataBean);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.DETAILS.name())) {
            parserHtml(event.getSource(), null);
        }
    }
}
