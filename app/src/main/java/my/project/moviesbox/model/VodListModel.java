package my.project.moviesbox.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.VodListContract;
import my.project.moviesbox.net.OkHttpUtils;
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
public class VodListModel extends BaseModel implements VodListContract.Model {
    @Override
    public void getData(boolean firstTimeData, String url, int page, VodListContract.LoadDataCallback callback) throws UnsupportedEncodingException {
        url = parserInterface.getVodListUrl(url, page);
        if (parserInterface.getPostMethodClassName().contains(this.getClass().getName())) {
            // 使用http post
        } else
            OkHttpUtils.getInstance().doGet(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.error(firstTimeData, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String source = getBody(response);
                        VodDataBean vodDataBean = parserInterface.parserVodList(source);
                        int pageCount = firstTimeData ? parserInterface.parserPageCount(source) : parserInterface.startPageNum();
                        if (Utils.isNullOrEmpty(vodDataBean))
                            callback.error(firstTimeData, parserErrorMsg(response, source));
                        else if (vodDataBean.getItemList().size() > 0)
                            callback.success(firstTimeData, vodDataBean, pageCount);
                        else
                            callback.empty(Utils.getString(R.string.emptyData));
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(firstTimeData, e.getMessage());
                    }
                }
            });
    }
}
