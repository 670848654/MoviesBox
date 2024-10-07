package my.project.moviesbox.model;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import my.project.moviesbox.contract.DomainListContract;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.DomainDataBean;
import my.project.moviesbox.parser.config.SourceEnum;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/9/16 14:24
 */
public class DomainListModel extends BaseModel implements DomainListContract.Model {
    @Override
    public void getData(DomainListContract.LoadDataCallback callback) {
        String url = SourceEnum.getWebsiteReleaseBySource(parserInterface.getSource());
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
                        DomainDataBean domainDataBean = parserInterface.parserDomain(source);
                        if (domainDataBean.isSuccess())
                            callback.success(domainDataBean.getDomainList());
                        else
                            callback.error(domainDataBean.getMsg());
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
