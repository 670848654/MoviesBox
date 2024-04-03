package my.project.moviesbox.model;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import my.project.moviesbox.contract.DownloadVideoContract;
import my.project.moviesbox.net.OkHttpUtils;
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
 * @date 2023/12/31 13:34
 */
public class DownloadVideoModel extends BaseModel implements DownloadVideoContract.Model {
    @Override
    public void getData(String url, String playNumber, DownloadVideoContract.LoadDataCallback callback) {
        url = getHttpUrl(url);
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
                    switch (parserInterface.getSource()) {
                        case ParserInterfaceFactory.SOURCE_SILISILI:
                            // 嘶哩嘶哩站点视频播放地址解析方案
                            String source = getBody(response);
                            String decodeData = SilisiliImpl.getDecodeData(source);
                            if (decodeData.isEmpty())
                                callback.error(playNumber);
                            else
                                parserPlayUrl(decodeData, playNumber, callback);
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
                    parserPlayUrl(source, playNumber, callback);
                }
            });
    }

    private  void parserPlayUrl(String source, String playNumber, DownloadVideoContract.LoadDataCallback callback) {
        try {
            List<String> urls = parserInterface.getPlayUrl(source);
            if (!Utils.isNullOrEmpty(urls))
                callback.downloadVodUrlSuccess(urls, playNumber);
            else
                callback.downloadVodUrlError(playNumber);
        } catch (Exception e) {
            e.printStackTrace();
            callback.downloadVodUrlError(playNumber);
        }
    }
}
