package my.project.moviesbox.model;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.config.ParserInterfaceFactory;
import my.project.moviesbox.parser.parserImpl.SilisiliImpl;
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
    @Override
    public void getData(boolean onlyGetPlayUrl, String title, String url, int listSource, String playNumber, VideoContract.LoadDataCallback callback) {
        if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
            // 使用http post
            FormBody body = parserInterface.getPostFormBodyByClassName(this.getClass().getName());
            OkHttpUtils.getInstance().doPost(getHttpUrl(url), body, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.errorNet(e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        String vodId = TVideoManager.queryId(title);
                        String dramaStr = THistoryManager.queryAllIndex(vodId, true, listSource);
                        switch (parserInterface.getSource()) {
                            case ParserInterfaceFactory.SOURCE_SILISILI:
                                // 嘶哩嘶哩站点视频播放地址解析方案
                                String decodeData = SilisiliImpl.getDecodeData(source);
                                if (decodeData.isEmpty())
                                    callback.errorPlayUrl();
                                else {
                                    List<String> urls = parserInterface.getPlayUrl(decodeData);
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
                        }
                    } catch (Exception e) {
                        callback.errorNet(e.getMessage());
                    }
                }
            });
        } else {
            OkHttpUtils.getInstance().doGet(getHttpUrl(url), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.errorNet(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        String vodId = TVideoManager.queryId(title);
                        List<String> urls = parserInterface.getPlayUrl(source);
                        String dramaStr = THistoryManager.queryAllIndex(vodId, true, listSource);
                        if (!onlyGetPlayUrl) {
                            List<DetailsDataBean.DramasItem> dramasItems = parserInterface.parserNowSourcesDramas(source, listSource, dramaStr);
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

                    } catch (Exception e) {
                        callback.errorNet(e.getMessage());
                    }
                }
            });
        }
    }

}
