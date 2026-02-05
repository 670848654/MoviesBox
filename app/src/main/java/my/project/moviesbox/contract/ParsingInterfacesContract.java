package my.project.moviesbox.contract;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/21 8:37
 */
public interface ParsingInterfacesContract {
    interface Model {
        void parser(String url, boolean isEpisodes, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void success(JSONObject object, boolean isEpisodes);

        void error(String msg, boolean isEpisodes);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(JSONObject object, boolean isEpisodes);

        void error(String msg, boolean isEpisodes);
    }
}
