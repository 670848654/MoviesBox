package my.project.moviesbox.contract;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/21 8:37
 */
public interface ParsingInterfacesContract {
    interface Model {
        void parser(String parserUrl, String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void success(Object object);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(Object object);
    }
}
