package my.project.moviesbox.contract;

/**
  * @包名: my.project.moviesbox.contract
  * @类名: UpdateImgContract
  * @描述: 当图片地址变更时尝试更新图片接口
  * @作者: Li Z
  * @日期: 2024/2/17 20:55
  * @版本: 1.0
 */
public interface UpdateImgContract {
    interface Model {
        void getImg(String oldImgUrl, String descUrl, UpdateImgContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void successImg(String descUrl, String imgUrl);
        void errorImg(String descUrl);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successImg(String descUrl, String imgUrl);
        void errorImg(String descUrl);
    }
}
