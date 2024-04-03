package my.project.moviesbox.model;

import java.io.IOException;
import java.util.List;

import my.project.moviesbox.contract.HomeContract;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/30 20:59
 */
public class HomeModel extends BaseModel implements HomeContract.Model {
    @Override
    public void getData(HomeContract.LoadDataCallback callback) {
        if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
            // 使用http post
        } else
            OkHttpUtils.getInstance().doGet(parserInterface.getDefaultDomain(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.error(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        List<MainDataBean> mainDataBeans = parserInterface.parserMainData(source);
                        if (!Utils.isNullOrEmpty(mainDataBeans))
                            callback.success(mainDataBeans);
                        else
                            callback.error(parserErrorMsg(response, source));
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });
    }
}
