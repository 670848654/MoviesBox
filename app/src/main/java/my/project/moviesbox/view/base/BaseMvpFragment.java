package my.project.moviesbox.view.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import my.project.moviesbox.model.BaseModel;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.lazyLoadImage.LazyLoadImgListener;

/**
  * @包名: my.project.moviesbox.view
  * @类名: BaseFragment
  * @描述: fragment基类
  * @作者: Li Z
  * @日期: 2024/2/4 17:06
  * @版本: 1.0
 */
public abstract class BaseMvpFragment<M extends BaseModel,V, P extends Presenter<V, M>, VB extends ViewBinding> extends BaseFragment<VB> {
    protected P mPresenter;
    protected LazyLoadImgListener lazyLoadImgListener;

    protected void setLazyLoadImgListener(LazyLoadImgListener lazyLoadImgListener) {
        this.lazyLoadImgListener = lazyLoadImgListener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mPresenter = createPresenter();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mPresenter)
            mPresenter.registerEventBus();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != mPresenter)
            mPresenter.unregisterEventBus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消View的关联
        if (null != mPresenter)
            mPresenter.detachView();
    }

    /**
     * 图片懒加载
     */
    protected void lazyLoadImg() {
        if (!Utils.isNullOrEmpty(lazyLoadImgListener))
            lazyLoadImgListener.loadImg();
    }

    protected abstract P createPresenter();
}
