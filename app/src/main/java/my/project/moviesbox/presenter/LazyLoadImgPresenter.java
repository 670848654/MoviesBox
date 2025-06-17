package my.project.moviesbox.presenter;

import my.project.moviesbox.contract.LazyLoadImgContract;
import my.project.moviesbox.model.LazyLoadImgModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/4/10 11:21
 */
public class LazyLoadImgPresenter extends Presenter<LazyLoadImgContract.View, LazyLoadImgModel> implements BasePresenter, LazyLoadImgContract.LoadDataCallback {
    private LazyLoadImgContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public LazyLoadImgPresenter(LazyLoadImgContract.View view) {
        super(view);
        this.view = view;
        model = new LazyLoadImgModel();
    }

    public void getImage(String key, String iv, String[] imageUrls) {
        model.getImage(this, key, iv, imageUrls);
    }

    /**
     * @param msg 错误信息
     * @return
     * @方法名称: error
     * @方法描述: 失败回调方法
     * @日期: 2024/1/22 19:51
     * @作者: Li Z
     */
    @Override
    public void error(String msg) {

    }

    /**
     * 加载数据
     *
     * @param firstTimeData 是否第一次加载数据
     */
    @Override
    public void loadData(boolean firstTimeData) {}

    @Override
    public void imageSuccess(String imageUrl, String base64) {
        view.imageSuccess(imageUrl, base64);
    }

    @Override
    public void imageError(String imageUrl) {
        view.imageError(imageUrl);
    }
}
