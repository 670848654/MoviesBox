package my.project.moviesbox.presenter;

import java.lang.ref.WeakReference;

import my.project.moviesbox.model.BaseModel;

public class Presenter<V, M extends BaseModel> {
    //View的弱引用
    protected WeakReference<V> mViewRef;
    protected M model;

    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public Presenter(V view) {
        //关联View
        attachView(view);
    }

    /**
     * 关联View
     *
     * @param view 需要关联的View
     */
    public void attachView(V view) {
        mViewRef = new WeakReference<>(view);
    }

    /**
     * 取消关联的View
     */
    public void detachView() {
        if (null != mViewRef)
            mViewRef.clear();
        if (null != model)
            model.unregister();
    }

    /**
     * 获取将当前关联的View
     *
     * @return 当前关联的View
     */
    public V getView() {
        if (null != mViewRef) {
            return mViewRef.get();
        }
        return null;
    }
}
