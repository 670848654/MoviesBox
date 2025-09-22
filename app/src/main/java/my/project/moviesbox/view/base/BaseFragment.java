package my.project.moviesbox.view.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DirectoryAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.databinding.BaseEmntyViewBinding;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoAlertUtils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: BaseFragment
  * @描述: fragment基类
  * @作者: Li Z
  * @日期: 2024/2/4 17:06
  * @版本: 1.0
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {
    protected VB binding;
    protected App application;
    protected static final int DIRECTORY_REQUEST_CODE = 0x10010;
    protected static final int DIRECTORY_CONFIG_RESULT_CODE = 0x10011;
    protected int position;
    protected int change;
    protected boolean isPortrait;
    protected ParserInterface parserInterface = ParserInterfaceFactory.getParserInterface();
    protected PopupWindow popupWindow;
    protected DirectoryPopupWindowAdapterClickListener directoryPopupWindowAdapterClickListener;
    protected VideoAlertUtils videoAlertUtils;
    /* 空布局视图相关 */
    protected View rvView;
    protected LinearProgressIndicator linearProgressIndicator;
    protected RelativeLayout errorView;
    protected Button refDataBtn;
    protected TextView errorMsgView;
    private LinearLayout emptyView;
    private TextView emptyMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (application == null) application = (App) getActivity().getApplication();
        Configuration mConfiguration = getResources().getConfiguration();
        change = mConfiguration.orientation;
        if (change == mConfiguration.ORIENTATION_LANDSCAPE) isPortrait = false;
        else if (change == mConfiguration.ORIENTATION_PORTRAIT) isPortrait = true;
        binding = inflateBinding(inflater, container);
        videoAlertUtils = new VideoAlertUtils(this.getActivity());
        EventBus.getDefault().register(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCustomViews();
        initViews();
        loadData();
        initClickListeners();
    }

    public abstract void initViews();

    public abstract void initClickListeners();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != videoAlertUtils)
            videoAlertUtils.release();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 初始化自定义视图
     */
    protected BaseEmntyViewBinding emptyBinding;
    protected void initCustomViews() {
        emptyBinding = BaseEmntyViewBinding.inflate(getLayoutInflater());
        rvView = emptyBinding.getRoot();
        linearProgressIndicator = emptyBinding.progress;
        errorView = emptyBinding.errorView;
        refDataBtn = emptyBinding.refData;
        errorMsgView = emptyBinding.errorMsg;
        emptyView = emptyBinding.emptyView;
        emptyMsg = emptyBinding.emptyMsg;
        refDataBtn.setOnClickListener(view -> retryListener());
    }

    /**
     * 刷新视图
     */
    protected void rvLoading(boolean showProgress) {
        if (showProgress) {
            if (linearProgressIndicator.getVisibility() != View.VISIBLE) {
                linearProgressIndicator.setVisibility(View.VISIBLE);
            }
            linearProgressIndicator.show();
        }
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    /**
     * 隐藏加载进度条
     */
    protected void hideProgress() {
        linearProgressIndicator.hide();
    }

    /**
     * 空视图
     * @return
     */
    protected void rvEmpty(String msg) {
        hideProgress();
        errorView.setVisibility(View.GONE);
        emptyMsg.setText(msg);
        emptyView.setVisibility(View.VISIBLE);
    }

    /**
     * 错误视图
     * @return
     */
    protected void rvError(String msg) {
        hideProgress();
        errorMsgView.setText(msg);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 防止两次调用
        if (newConfig.orientation == change) return;
        change = newConfig.orientation;
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setConfigurationChanged();
    }

    /**
     * 设置菜单通用方法
     * @param view 依附视图
     * @param menuRes 菜单布局
     * @param menuId 需要设置颜色的文本ID
     * @param listener 点击监听事件
     */
    @SuppressLint("RestrictedApi")
    protected void setMenu(View view, @MenuRes int menuRes, int menuId, PopupMenu.OnMenuItemClickListener listener) {
        final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());
        if (menuId != -1) {
            SpannableString ss = new SpannableString(popupMenu.getMenu().findItem(menuId).getTitle());
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getActivity().getColor(R.color.delete_color));
            ss.setSpan(foregroundColorSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            popupMenu.getMenu().findItem(menuId).setTitle(ss);
        }
        MenuBuilder menuBuilder = (MenuBuilder) popupMenu.getMenu();
        menuBuilder.setOptionalIconsVisible(true);
        popupMenu.setOnMenuItemClickListener(listener);
        popupMenu.show();
        registerForContextMenu(view);
    }

    protected abstract void setConfigurationChanged();

    protected abstract VB inflateBinding(LayoutInflater inflater, ViewGroup container);

    protected abstract void loadData();

    /**
     * 点击重试接口
     * @return
     */
    protected abstract void retryListener();

    public abstract void onEvent(RefreshEnum refresh);

    /**
     * 目录清单列表适配器点击接口
     */
    public interface DirectoryPopupWindowAdapterClickListener {
        void onItemClickListener(TDirectory tDirectory);
    }

    public void setDirectoryPopupWindowAdapterClickListener(DirectoryPopupWindowAdapterClickListener listener) {
        this.directoryPopupWindowAdapterClickListener = listener;
    }

    /**
     * 显示目录清单PopupWindow
     * @param tDirectories
     * @param showAsView
     */
    protected void showDirectoryPopupWindow(List<TDirectory> tDirectories, View showAsView) {
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_directory_selection, null);
        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DirectoryAdapter directoryAdapter = new DirectoryAdapter(false, tDirectories);
        recyclerView.setAdapter(directoryAdapter);
        int maxItemWidth = getMaxItemWidth(getActivity(), tDirectories, 16); // 16sp 对应 textSize
        LogUtil.logInfo("maxItemWidth", maxItemWidth+"");
        int finalWidth = maxItemWidth + Utils.dpToPx(getActivity(), 16); // 补上左右 padding
        LogUtil.logInfo("finalWidth", finalWidth+"");
        popupWindow = new PopupWindow(popupView, finalWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置 PopupWindow 背景变暗
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = 0.5f; // 设置透明度，值越小背景越暗
        getActivity().getWindow().setAttributes(layoutParams);
        // 当 PopupWindow 消失时恢复背景亮度
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.alpha = 1.0f; // 恢复透明度
            getActivity().getWindow().setAttributes(lp);
        });
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setAnimationStyle(R.style.PopupTopAnim);
        // 显示 PopupWindow
        popupWindow.showAsDropDown(showAsView);
        directoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            directoryPopupWindowAdapterClickListener.onItemClickListener(tDirectories.get(position));
        });
    }

    private int getMaxItemWidth(Context context, List<TDirectory> tDirectories, float textSizeSp) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics()));
        paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        int maxTextWidth = 0;
        for (TDirectory d : tDirectories) {
            if (d.getName() != null) {
                int w = (int) paint.measureText(d.getName());
                if (w > maxTextWidth) maxTextWidth = w;
            }
        }

        // TextView padding
        int textPadding = Utils.dpToPx(context, 8 + 8); // paddingStart + paddingEnd

        // 两个按钮宽度（假设每个按钮 36dp + margin，可根据实际调整）
        int buttonWidth = Utils.dpToPx(context, 36);

        int totalWidth = maxTextWidth + textPadding + buttonWidth;

        // 限制最大宽度为屏幕宽度 90%
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        return Math.min(totalWidth, (int) (screenWidth * 0.9f));
    }

}
