package my.project.moviesbox.view.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import my.project.moviesbox.model.BaseModel;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.lazyLoadImage.LazyLoadImgListener;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/8/28 16:27
 */
public abstract class BaseMvpActivity<M extends BaseModel, V, P extends Presenter<V, M>, VB extends ViewBinding> extends BaseActivity<VB> {
    protected P mPresenter;

    protected LazyLoadImgListener lazyLoadImgListener;

    protected void setLazyLoadImgListener(LazyLoadImgListener lazyLoadImgListener) {
        this.lazyLoadImgListener = lazyLoadImgListener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
        loadData();
    }

    protected abstract P createPresenter();

    protected abstract void loadData() ;

    @Override
    protected void onStart() {
        super.onStart();
        if (null != mPresenter)
            mPresenter.registerEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mPresenter)
            mPresenter.unregisterEventBus();
    }

    @Override
    protected void onDestroy() {
        //取消View的关联
        if (null != mPresenter)
            mPresenter.detachView();
        super.onDestroy();
    }

    /**
     * 图片懒加载
     */
    protected void lazyLoadImg() {
        if (!Utils.isNullOrEmpty(lazyLoadImgListener))
            lazyLoadImgListener.loadImg();
    }
}
