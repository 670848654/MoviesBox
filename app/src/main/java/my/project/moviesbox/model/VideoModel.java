package my.project.moviesbox.model;

import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.fromIndex;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.parser.parserImpl.SilisiliImpl;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 16:20
 */
public class VideoModel extends BaseModel implements VideoContract.Model {
    private VideoContract.LoadDataCallback callback;
    private boolean onlyGetPlayUrl;
    private String title;
    private int listSource;

    @Override
    public void getData(boolean onlyGetPlayUrl, String title, String url, int listSource, String playNumber, VideoContract.LoadDataCallback callback) {
        url = getHttpUrl(url);
        this.callback = callback;
        this.onlyGetPlayUrl = onlyGetPlayUrl;
        this.title = title;
        this.listSource = listSource;
        if (SharedPreferencesUtils.getByPassCF())
            App.startMyService(url, FuckCFEnum.VIDEO_URL.name());
        else if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
            // 使用http post
            FormBody body = parserInterface.getPostFormBodyByClassName(this.getClass().getName());
            OkHttpUtils.getInstance().doPost(getHttpUrl(url), body, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.errorNet(onlyGetPlayUrl, e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        parserHtml(source);
                    } catch (Exception e) {
                        callback.errorNet(onlyGetPlayUrl, e.getMessage());
                    }
                }
            });
        } else {
            OkHttpUtils.getInstance().doGet(getHttpUrl(url), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.errorNet(onlyGetPlayUrl, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        parserHtml(source);
                    } catch (Exception e) {
                        callback.errorNet(onlyGetPlayUrl, e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 解析数据
     * @param html
     */
    private void parserHtml(String html) {
        String vodId = TVideoManager.queryId(title);
        String dramaStr = THistoryManager.queryAllIndex(vodId, true, listSource);
        int interfaceSource = parserInterface.getSource();
        SourceEnum.SourceIndexEnum sourceEnum = fromIndex(interfaceSource);
        switch (sourceEnum) {
            case SILISILI:
                // 嘶哩嘶哩站点视频播放地址解析方案
                String decodeData = SilisiliImpl.getDecodeData(html);
                if (decodeData.isEmpty())
                    callback.errorPlayUrl();
                else {
                    List<DialogItemBean> urls = parserInterface.getPlayUrl(decodeData, false);
                    if (onlyGetPlayUrl) {
                        if (!Utils.isNullOrEmpty(urls))
                            callback.successOnlyPlayUrl(urls);
                        else
                            callback.errorOnlyPlayUrl();
                    } else {
                        String fenjihtml = SilisiliImpl.getJsonData(false, decodeData);
                        List<DetailsDataBean.DramasItem> dramasItems = parserInterface.parserNowSourcesDramas(fenjihtml, listSource, dramaStr);
                        if (!Utils.isNullOrEmpty(dramasItems)) {
                            for (DetailsDataBean.DramasItem item : dramasItems) {
                                if (dramaStr.contains(item.getUrl())) {
                                    item.setSelected(true);
                                }
                            }
                            callback.successDramasList(dramasItems);
                        }
                        else
                            callback.errorDramasList();
                        if (!Utils.isNullOrEmpty(urls))
                            callback.successPlayUrl(urls);
                        else
                            callback.errorPlayUrl();
                    }
                }
                break;
            default:
                List<DialogItemBean> urls = parserInterface.getPlayUrl(html, false);
                if (!onlyGetPlayUrl) {
                    List<DetailsDataBean.DramasItem> dramasItems = parserInterface.parserNowSourcesDramas(html, listSource, dramaStr);
                    if (!Utils.isNullOrEmpty(dramasItems)) {
                        for (DetailsDataBean.DramasItem item : dramasItems) {
                            if (dramaStr.contains(item.getUrl())) {
                                item.setSelected(true);
                            }
                        }
                        callback.successDramasList(dramasItems);
                    }
                    else
                        callback.errorDramasList();
                    if (!Utils.isNullOrEmpty(urls))
                        callback.successPlayUrl(urls);
                    else
                        callback.errorPlayUrl();
                } else {
                    if (!Utils.isNullOrEmpty(urls))
                        callback.successOnlyPlayUrl(urls);
                    else
                        callback.errorOnlyPlayUrl();
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.VIDEO_URL.name())) {
            parserHtml(event.getSource());
        }
    }
}
