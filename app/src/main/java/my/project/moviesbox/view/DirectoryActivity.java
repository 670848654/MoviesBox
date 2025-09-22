package my.project.moviesbox.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DirectoryAdapter;
import my.project.moviesbox.custom.FabExtendingOnScrollListener;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.enums.DirectoryTypeEnum;
import my.project.moviesbox.database.manager.TDirectoryManager;
import my.project.moviesbox.databinding.ActivityDirectoryListBinding;
import my.project.moviesbox.databinding.DialogDirectoryBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/11/1 15:42
 */
public class DirectoryActivity extends BaseActivity<ActivityDirectoryListBinding> {
    protected DirectoryAdapter directoryAdapter;
    protected String type;
    protected boolean showConfigBtn = false;
    protected int fabHeight = 0;
    protected List<TDirectory> list = new ArrayList<>();
    protected DirectoryTypeEnum directoryTypeEnum;


    @Override
    protected void init() {
        getData();
        setToolbar(toolbar, showConfigBtn ? getString(R.string.defaultToolbarTitle) : getString(R.string.saveToList), "");
        initSwipe();
        initAdapter();
        initFab();
    }

    @Override
    protected void initBeforeView() {

    }

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityDirectoryListBinding inflateBinding(LayoutInflater inflater) {
        return ActivityDirectoryListBinding.inflate(inflater);
    }

    protected AppBarLayout appBar;
    protected Toolbar toolbar;
    protected RecyclerView recyclerView;
    SwipeRefreshLayout mSwipe;
    protected ExtendedFloatingActionButton addDirectory;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.contentLayout.rvList;
        mSwipe = binding.contentLayout.mSwipe;
        addDirectory = binding.addDirectory;
    }

    @Override
    public void initClickListeners() {
        addDirectory.setOnClickListener(view -> {
            Utils.setVibration(view);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
            builder.setTitle(getString(R.string.createNewList));
            DialogDirectoryBinding dialogBinding = DialogDirectoryBinding.inflate(LayoutInflater.from(this));
            TextInputLayout textInputLayout = dialogBinding.name;
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    textInputLayout.setError(null);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            final InputMethodManager imm = Objects.requireNonNull((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE));
            textInputLayout.getEditText().postDelayed(() -> imm.showSoftInput(textInputLayout.getEditText(), InputMethodManager.SHOW_IMPLICIT), 100);
            textInputLayout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus)
                    Objects.requireNonNull(alertDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            });
            builder.setPositiveButton("创建并保存", (dialog, which) -> {
                String text = textInputLayout.getEditText().getText().toString().replaceAll(" ", "");
                if (text.equals(getString(R.string.defaultList))) {
                    textInputLayout.setError(getString(R.string.listNameCannotBeTheDefaultName));
                    application.showToastMsg(getString(R.string.listNameCannotBeTheDefaultName), DialogXTipEnum.ERROR);
                } else if (!Utils.isNullOrEmpty(text)) {
                    alertDialog.dismiss();
                    String directoryId = TDirectoryManager.insert(text, parserInterface.getSource(), directoryTypeEnum);
                    closeActivity(directoryId);
                    onCreateResult(directoryId);
                }
                else {
                    textInputLayout.setError(getString(R.string.listNameCannotBeEmpty));
                    application.showToastMsg(getString(R.string.listNameCannotBeEmpty), DialogXTipEnum.ERROR);
                }
            });
            builder.setNegativeButton(getString(R.string.defaultNegativeBtnText), null);
            builder.setCancelable(false);
            alertDialog = builder.setView(dialogBinding.getRoot()).create();
            alertDialog.show();
            Utils.dialogSetRenderEffect(this);
            alertDialog.setOnDismissListener(dialog -> Utils.dialogClearRenderEffect(this));
        });
    }

    @Override
    protected void setConfigurationChanged() {

    }

    /**
     * 点击重试抽象方法
     *
     * @return
     */
    @Override
    protected void retryListener() {

    }

    protected void getData() {
        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");
        showConfigBtn = bundle.getBoolean("showConfigBtn");
        if (Objects.equals(type, DirectoryTypeEnum.FAVORITE.getName())) {
            directoryTypeEnum = DirectoryTypeEnum.FAVORITE;
            list = TDirectoryManager.queryFavoriteDirectoryList(showConfigBtn, false);
        }
        else {
            directoryTypeEnum = DirectoryTypeEnum.DOWNLOAD;
            list = TDirectoryManager.queryDownloadDirectoryList(showConfigBtn, false);
        }
    }

    private void initSwipe() {
        mSwipe.setEnabled(false);
    }

    protected void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        directoryAdapter = new DirectoryAdapter(true, list);
        setAdapterAnimation(directoryAdapter);
        directoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
            TDirectory tDirectory = (TDirectory) adapter.getItem(position);
            closeActivity(tDirectory.getId());
        });
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this) + 15);
        recyclerView.setAdapter(directoryAdapter);
        recyclerView.addOnScrollListener(new FabExtendingOnScrollListener(addDirectory));
    }

    private void initFab() {
        if (Utils.checkHasNavigationBar(this))
        {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) addDirectory.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 1);
            addDirectory.setLayoutParams(params);
        }
        if (fabHeight == 0) {
            // 添加布局完成监听器
            ViewTreeObserver viewTreeObserver = addDirectory.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // 获取 FloatingActionButton 的高度
                    fabHeight = addDirectory.getHeight();
                    recyclerView.setPadding(0,0,0, fabHeight+Utils.dpToPx(DirectoryActivity.this, 32));
                    // 由于已经获取了高度，因此可以移除监听器，避免重复调用
                    addDirectory.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    protected void onCreateResult (String directoryId) {

    }

    protected void closeActivity(String directoryId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("type", type);
        resultIntent.putExtra("directoryId", directoryId);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
