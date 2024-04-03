package my.project.moviesbox.model;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.WeekContract;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeekModel extends BaseModel implements WeekContract.Model {
    @Override
    public void getWeekData(String url, WeekContract.LoadDataCallback callback) {
        url = getHttpUrl(url);
        if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
            // 使用http post
        } else {
            OkHttpUtils.getInstance().doGet(url, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.error(e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        List<WeekDataBean> weekDataBeans = parserInterface.parserWeekDataList(source);
                        if (Utils.isNullOrEmpty(weekDataBeans))
                            callback.error(parserErrorMsg(response, source));
                        else if (weekDataBeans.size() > 0)
                            callback.weekSuccess(weekDataBeans);
                        else
                            callback.error(Utils.getString(R.string.loadErrorMsg));
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(e.getMessage());
                    }
                }
            });
        }
    }
}
