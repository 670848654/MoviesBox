package my.project.moviesbox.view.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import my.project.moviesbox.R;
import my.project.moviesbox.databinding.DialogPagePickerBinding;
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

    /**
     * 弹出页码选择对话框
     * @param currentPage 当前页码
     * @param totalPage 总页数
     * @param onPageSelected 用户选择回调
     */
    protected void showSelectPage(int currentPage, int totalPage, Consumer<Integer> onPageSelected) {
        if (isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        DialogPagePickerBinding dialogPagePickerBinding = DialogPagePickerBinding.inflate(LayoutInflater.from(this));
        dialog.setContentView(dialogPagePickerBinding.getRoot());

        NumberPicker picker = dialogPagePickerBinding.pagePicker;
        MaterialTextView pageInfo = dialogPagePickerBinding.pageInfo;
        MaterialButton btnCancel = dialogPagePickerBinding.btnCancel;
        MaterialButton btnConfirm = dialogPagePickerBinding.btnConfirm;

        picker.setMinValue(1);
        picker.setMaxValue(totalPage);
        picker.setValue(currentPage);
        picker.setWrapSelectorWheel(false);

        pageInfo.setText("第 " + currentPage + " / " + totalPage + " 页");

        picker.setOnValueChangedListener((pickerView, oldVal, newVal) ->
                pageInfo.setText("第 " + newVal + " / " + totalPage + " 页"));

        btnCancel.setOnClickListener(v -> {
            Utils.setVibration(v);
            dialog.dismiss();
        });
        btnConfirm.setOnClickListener(v -> {
            Utils.setVibration(v);
            dialog.dismiss();
            if (onPageSelected != null)
                onPageSelected.accept(picker.getValue());
        });
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.show();
    }
}
