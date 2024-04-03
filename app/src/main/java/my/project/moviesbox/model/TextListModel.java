package my.project.moviesbox.model;

import java.io.IOException;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.TextListContract;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/1/24 15:40
 */
public class TextListModel extends BaseModel implements TextListContract.Model {
    @Override
    public void getData(String url, TextListContract.LoadDataCallback callback) {
        url = parserInterface.getTextUrl(url);
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
                        List<TextDataBean> textDataBeans = parserInterface.parserTextList(source);
                        if (Utils.isNullOrEmpty(textDataBeans))
                            callback.error(parserErrorMsg(response, source));
                        else if (textDataBeans.size() > 0)
                            callback.success(textDataBeans);
                        else
                            callback.empty(Utils.getString(R.string.emptyData));
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });
    }
}
