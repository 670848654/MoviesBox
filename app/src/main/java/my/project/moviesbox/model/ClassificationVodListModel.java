package my.project.moviesbox.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.ClassificationVodListContract;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/31 21:46
 */
public class ClassificationVodListModel extends BaseModel implements ClassificationVodListContract.Model {
    @Override
    public void getData(boolean firstTimeData, ClassificationVodListContract.LoadDataCallback callback, String... param) throws UnsupportedEncodingException {
        String url = parserInterface.getClassificationUrl(param);
        if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
            // 使用http post
        } else
            OkHttpUtils.getInstance().doGet(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.errorVodList(firstTimeData, e.getMessage());
                    callback.errorClassList(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        VodDataBean vodDataBean = parserInterface.parserClassificationVodList(source);
                        int pageCount = firstTimeData ? parserInterface.parserPageCount(source) : parserInterface.startPageNum();
                        if (Utils.isNullOrEmpty(vodDataBean))
                            callback.errorVodList(firstTimeData, parserErrorMsg(response, source));
                        else if (vodDataBean.getItemList().size() > 0)
                            callback.successVodList(firstTimeData, vodDataBean, pageCount);
                        else
                            callback.emptyVodList(Utils.getString(R.string.emptyData));
                        if (firstTimeData) {
                            List<ClassificationDataBean> classificationDataBeans = parserInterface.parserClassificationList(source);
                            if (Utils.isNullOrEmpty(classificationDataBeans))
                                callback.error(parserErrorMsg(response, source));
                            else if (classificationDataBeans.size() > 0)
                                callback.successClassList(classificationDataBeans);
                            else
                                callback.emptyClassList();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.errorVodList(firstTimeData, e.getMessage());
                        callback.errorClassList(e.getMessage());
                    }
                }
            });
    }
}
