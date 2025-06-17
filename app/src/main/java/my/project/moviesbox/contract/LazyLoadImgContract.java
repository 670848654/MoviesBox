package my.project.moviesbox.contract;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/4/10 13:55
 */
public interface LazyLoadImgContract {
    interface Model {
        void getImage(LoadDataCallback callback, String key, String iv, String[] imageUrls);
    }

    interface View extends BaseView {
        void imageSuccess(String imageUrl, String base64);
        void imageError(String imageUrl);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void imageSuccess(String imageUrl, String base64);
        void imageError(String imageUrl);
    }
}
