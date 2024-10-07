package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import my.project.moviesbox.contract.UpdateImgContract;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.DetailsDataBean;
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
    @Override
    public void getImg(String oldImgUrl, String descUrl, UpdateImgContract.LoadDataCallback callback) {
        String url = getHttpUrl(descUrl);
        if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {

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
                        DetailsDataBean detailsDataBean = parserInterface.parserDetails(url, source);
                        if (Utils.isNullOrEmpty(detailsDataBean))
                            callback.errorImg();
                        else
                            callback.successImg(oldImgUrl, detailsDataBean.getImg());
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
