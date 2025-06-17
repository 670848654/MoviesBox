package my.project.moviesbox.model;

import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.fromIndex;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.DownloadVideoContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum;
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
 * @date 2023/12/31 13:34
 */
public class DownloadVideoModel extends BaseModel implements DownloadVideoContract.Model {
    private DownloadVideoContract.LoadDataCallback callback;
    private String playNumber;

    @Override
    public void getData(String url, String playNumber, DownloadVideoContract.LoadDataCallback callback) {
        url = getHttpUrl(url);
        this.callback = callback;
        this.playNumber = playNumber;
        if (SharedPreferencesUtils.getByPassCF()) {
            App.startMyService(url, FuckCFEnum.DOWNLOAD_URL.name());
        } else {
            if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
                // 使用http post
                FormBody body = parserInterface.getPostFormBodyByClassName(this.getClass().getName());
                OkHttpUtils.getInstance().doPost(url, body, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        callback.downloadVodUrlError(playNumber);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        int interfaceSource = parserInterface.getSource();
                        SourceIndexEnum sourceEnum = fromIndex(interfaceSource);
                        switch (sourceEnum) {
                            case SILISILI:
                                // 嘶哩嘶哩站点视频播放地址解析方案
                                String source = getBody(response);
                                String decodeData = SilisiliImpl.getDecodeData(source);
                                if (decodeData.isEmpty())
                                    callback.error(playNumber);
                                else
                                    parserHtml(decodeData);
                                break;
                        }
                    }
                });
            } else
                OkHttpUtils.getInstance().doGet(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.downloadVodUrlError(playNumber);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String source = getBody(response);
                        parserHtml(source);
                    }
                });
        }

    }

    /**
     * 解析数据
     * @param html
     */
    private void parserHtml(String html) {
        try {
            List<DialogItemBean> urls = parserInterface.getPlayUrl(html, true);
            if (!Utils.isNullOrEmpty(urls))
                callback.downloadVodUrlSuccess(urls, playNumber);
            else
                callback.downloadVodUrlError(playNumber);
        } catch (Exception e) {
            e.printStackTrace();
            callback.downloadVodUrlError(playNumber);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.DOWNLOAD_URL.name())) {
            parserHtml(event.getSource());
        }
    }
}
