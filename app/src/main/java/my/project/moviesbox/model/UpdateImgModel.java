package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Objects;

import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.UpdateImgContract;
import my.project.moviesbox.enums.FuckCFEnum;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
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
 * @date 2024/2/17 20:33
 */
public class UpdateImgModel extends BaseModel implements UpdateImgContract.Model {
    private UpdateImgContract.LoadDataCallback callback;
    private String oldImgUrl;
    private String url;
    @Override
    public void getImg(String oldImgUrl, String descUrl, UpdateImgContract.LoadDataCallback callback) {
        String url = getHttpUrl(descUrl);
        this.oldImgUrl = oldImgUrl;
        this.url = url;
        this.callback = callback;
        if (SharedPreferencesUtils.getByPassCF())
            App.startMyService(url, FuckCFEnum.UPDATE_IMG.name());
        else if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {

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
                        parserData(source);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });
        }
    }

    public void parserData(String source) {
        DetailsDataBean detailsDataBean = parserInterface.parserDetails(url, source);
        if (Utils.isNullOrEmpty(detailsDataBean))
            callback.errorImg(url);
        else
            callback.successImg(url, detailsDataBean.getImg());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {
        if (Objects.equals(event.getType(), FuckCFEnum.UPDATE_IMG.name()))
            parserData(event.getSource());
    }
}
