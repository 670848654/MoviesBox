package my.project.moviesbox.view;

import static android.app.Activity.RESULT_OK;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOWNLOAD;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_FAVORITE;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_HISTORY;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_INDEX;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.TypeReference;
import com.arialyy.aria.core.Aria;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DomainListAdapter;
import my.project.moviesbox.adapter.SettingAboutAdapter;
import my.project.moviesbox.bean.SettingAboutBean;
import my.project.moviesbox.contract.DomainListContract;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.entity.TFavorite;
import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.database.entity.TVideo;
import my.project.moviesbox.database.manager.BackupsManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.enums.SettingEnum;
import my.project.moviesbox.event.CheckUpdateEvent;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.model.DomainListModel;
import my.project.moviesbox.parser.bean.DomainDataBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.presenter.DomainListPresenter;
import my.project.moviesbox.service.CheckUpdateService;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.utils.SAFUtils;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: SettingFragment
  * @描述: 设置Fragment
  * @作者: Li Z
  * @日期: 2024/1/23 19:55
  * @版本: 1.0
 */
public class SettingFragment extends BaseFragment<DomainListModel, DomainListContract.View, DomainListPresenter> implements DomainListContract.View {
    private static final int REQUEST_DOCUMENT_TREE = 10000;
    @BindView(R.id.title)
    TextView titleView;
    private View view;
    private final int source = parserInterface.getSource();
    private String defaultDomain;
//    private String defaultPrefix;
    private final String[] themeItems = Utils.getArray(R.array.theme);
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private SettingAboutAdapter adapter;
    private final List<SettingAboutBean> list = SettingEnum.getSettingAboutBeanList();
    private CheckUpdateEvent checkUpdateEvent;
    private HomeActivity homeActivity;
    private int adapterItemPosition;
    private AlertDialog alertDialog;
    private DomainListAdapter domainListAdapter;
    private BottomSheetDialog domainListBottomSheetDialog;

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_setting, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        homeActivity = (HomeActivity) getActivity();
        initData();
        initAdapter();
        return view;
    }

    /**
     * 用户保存的设置信息
     */
    private void initData() {
        for (SettingAboutBean bean : list) {
            String title = bean.getTitle();
            if (title.equals(getString(R.string.setDomainTitle))) {
                bean.setSubTitle(parserInterface.getDefaultDomain());
            } else if (title.equals(getString(R.string.setPlayerTitle))) {
                int setDefaultPlayer = SharedPreferencesUtils.getUserSetOpenVidePlayer();
                bean.setSubTitle(bean.getOption()[setDefaultPlayer].toString());
            } else if (title.equals(getString(R.string.setPlayerKernelTitle))) {
                int setPlayerKernel = SharedPreferencesUtils.getUserSetPlayerKernel();
                bean.setSubTitle(bean.getOption()[setPlayerKernel].toString());
            } else if (title.equals(getString(R.string.setDanmuTitle))) {
                bean.setSubTitle(SharedPreferencesUtils.getUserSetOpenDanmu() ? bean.getOption()[0].toString() : bean.getOption()[1].toString());
            } else if (title.equals(getString(R.string.setThemeTitle))) {
                bean.setSubTitle(themeItems[DarkModeUtils.chooseIndex(getActivity())]);
            } else if (title.equals(getString(R.string.currentVersionTitle))) {
                bean.setSubTitle(Utils.getASVersionName());
            }
        }
        /*saveParserLogsSwitch.setChecked(SharedPreferencesUtils.getSaveParserLogs());
        saveParserLogsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setSaveParserLogs(isChecked);
            application.showToastMsg(isChecked ? "开启保存记录解析日志" : "关闭保存记录解析日志", DialogXTipEnum.SUCCESS);
            if (isChecked) LogUtil.deleteLogFile();
        });*/
    }

    @OnClick(R.id.parser_log)
    public void openParserLogView() {
        startActivity(new Intent(getActivity(), ParserLogActivity.class));
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new SettingAboutAdapter(getActivity(), list);
        homeActivity.setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            adapterItemPosition = position;
            String title = list.get(position).getTitle();
            if (title.equals(getString(R.string.setDomainTitle)))
                setDomain(position);
            else if (title.equals(getString(R.string.enableByPassCF)))
                enableByPassCF(position);
            else if (title.equals(getString(R.string.enableSniffTitle)))
                enableSniff(position);
            else if (title.equals(getString(R.string.setPlayerTitle)))
                setDefaultPlayer(position);
            else if (title.equals(getString(R.string.setPlayerKernelTitle)))
                setPlayerKernel(position);
            else if (title.equals(getString(R.string.setDanmuTitle)))
                setDanmu(position);
            else if (title.equals(getString(R.string.setThemeTitle)))
                setTheme(position);
            else if (title.equals(getString(R.string.setM3u8Title)))
                setM3U8DownloadConfig(position);
            else if (title.equals(getString(R.string.setBackupsTitle)))
                backups(position);
            else if (title.equals(getString(R.string.setRemoveDownloadsTitle)))
                removeDownloads();
            else if (title.equals(getString(R.string.aboutTitle)))
                startActivity(new Intent(getActivity(), AboutActivity.class));
            else if (title.equals(getString(R.string.currentVersionTitle)))
                checkUpdate(position);
        });
        if (Utils.checkHasNavigationBar(getActivity())) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()) + 15);
        recyclerView.setAdapter(adapter);

        domainListAdapter = new DomainListAdapter(new ArrayList<>());
        domainListAdapter.setAnimationEnable(true);
        domainListAdapter.setAdapterAnimation(new AlphaInAnimation());
        domainListAdapter.setEmptyView(rvView);
        domainListAdapter.setOnItemClickListener((adapter, view, position) -> {
            DomainDataBean.Domain domain = (DomainDataBean.Domain) adapter.getData().get(position);
            String url = domain.getUrl();
            if (!Utils.isNullOrEmpty(url))
                setDomain(domain.getUrl());
            else
                application.showToastMsg("错误：链接为空", DialogXTipEnum.ERROR);
        });
    }

    private void setDataSubTitle(int position, String data) {
        list.get(position).setSubTitle(data);
        adapter.notifyItemChanged(position);
    }

    /**
     * @方法名称: setDomain
     * @方法描述: 设置当前源域名地址
     * @日期: 2024/1/24 13:37
     * @作者: Li Z
     * @返回:
     */
    public void setDomain(int dataPosition) {
        defaultDomain = parserInterface.getDefaultDomain();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.DialogStyle);
        builder.setTitle(Html.fromHtml(getString(R.string.setDomainDialogTitle)));
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_domain, null);
        TextView websiteView = view.findViewById(R.id.website);
        String websiteReleaseHref = SourceEnum.getWebsiteReleaseBySource(source);
        Button getDomainView = view.findViewById(R.id.getDomain);
        if (!Utils.isNullOrEmpty(websiteReleaseHref)) {
            String websiteTitle = getString(R.string.setDomainReleaseSubContent);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(websiteTitle);
            stringBuilder.append(websiteReleaseHref);
            SpannableString spannableString = new SpannableString(stringBuilder.toString());
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Utils.viewInChrome(getActivity(), websiteReleaseHref);
                }
            };
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(getActivity().getColor(R.color.pink200));
            spannableString.setSpan(clickableSpan, websiteTitle.length(), stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(colorSpan, websiteTitle.length(), stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            websiteView.setText(spannableString);
            websiteView.setMovementMethod(LinkMovementMethod.getInstance());
            websiteView.setVisibility(View.VISIBLE);
            getDomainView.setVisibility(View.VISIBLE);
        }
        getDomainView.setOnClickListener(v -> {
            domainListAdapter.setNewInstance(new ArrayList<>());
            View domainListView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_source_list, null);
            RecyclerView sourceListRecyclerView = domainListView.findViewById(R.id.rv_list);

            sourceListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            sourceListRecyclerView.setAdapter(domainListAdapter);
            domainListBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
            domainListBottomSheetDialog.setContentView(domainListView);

            // 获取 BottomSheetBehavior
            FrameLayout bottomSheet = domainListBottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

            // 设置peek height，比如设置为400像素
            behavior.setPeekHeight(400);

            domainListBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            domainListBottomSheetDialog.show();
            if (Utils.isNullOrEmpty(mPresenter))
                mPresenter = new DomainListPresenter(this);
            mPresenter.loadData();
        });
        /*AutoCompleteTextView prefixView = view.findViewById(R.id.prefix);
        String[] prefixArr = Utils.getArray(R.array.prefix);
        defaultPrefix = prefixArr[defaultDomain.startsWith("https") ? 1 : 0];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.text_list_item, prefixArr);
        prefixView.setText(adapter.getItem(defaultDomain.startsWith("https") ? 1 : 0));
        prefixView.setAdapter(adapter);
        prefixView.setOnItemClickListener((parent, textView, position, id) -> defaultPrefix = parent.getItemAtPosition(position).toString());*/
        TextInputLayout textInputLayout = view.findViewById(R.id.domain);
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
//        textInputLayout.getEditText().setText(defaultDomain.replaceAll("https?://", ""));
        textInputLayout.getEditText().setText(defaultDomain);
        textInputLayout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                Objects.requireNonNull(alertDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        });
        builder.setPositiveButton(getString(R.string.setDomainPositiveBtnText), (dialog, which) -> {
            String text = textInputLayout.getEditText().getText().toString();
            if (!Utils.isNullOrEmpty(text)) {
                if (Patterns.WEB_URL.matcher(text).matches()) {
                    setDomain(text);
                } else
                    textInputLayout.getEditText().setError(getString(R.string.setDomainErrorText2));
            }
            else textInputLayout.getEditText().setError(getString(R.string.setDomainErrorText1));
        });
        builder.setNegativeButton(getString(R.string.defaultNegativeBtnText), null);
        builder.setNeutralButton(getString(R.string.setDomainNeutralBtnText), (dialog, which) -> {
            SharedPreferencesUtils.setUserSetDomain(source, SourceEnum.getDomainUrlBySource(source));
            setDataSubTitle(dataPosition, SourceEnum.getDomainUrlBySource(source));
            EventBus.getDefault().post(REFRESH_INDEX);
            updateUrlByChangeDomain(defaultDomain, SourceEnum.getDomainUrlBySource(source));
            dialog.dismiss();
        });
        builder.setCancelable(true);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
    }

    /**
     * 变更域名时更新相关域名
     * @param oldDomain
     * @param newDomain
     */
    private void updateUrlByChangeDomain(String oldDomain, String newDomain) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            TFavoriteManager.updateUrlByChangeDomain(oldDomain, newDomain);
            EventBus.getDefault().post(REFRESH_FAVORITE);
            EventBus.getDefault().post(REFRESH_HISTORY);
        });
    }

    private void setDomain(String url) {
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
        SharedPreferencesUtils.setUserSetDomain(source, url);
        setDataSubTitle(0, url);
        if (!Utils.isNullOrEmpty(alertDialog))
            alertDialog.dismiss();
        if (!Utils.isNullOrEmpty(domainListBottomSheetDialog))
            domainListBottomSheetDialog.dismiss();
        EventBus.getDefault().post(REFRESH_INDEX);
        updateUrlByChangeDomain(defaultDomain, url);
        application.showToastMsg(String.format(getString(R.string.setDomainSuccessText), url), DialogXTipEnum.SUCCESS);
    }

    /**
     * @方法名称: enableByPassCF
     * @方法描述: 是否开启绕过CF
     * @日期: 2024/5/30 20:17
     * @作者: Li Z
     *
     * @返回:
     */
    public void enableByPassCF(int position) {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.DialogStyle);
        builder.setTitle(list.get(position).getTitle());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pass_cf, null);
        MaterialSwitch materialSwitch = view.findViewById(R.id.enableByPassCF);
        Slider setByPassCFTimeOutSlider = view.findViewById(R.id.setByPassCFTimeOut);
        boolean enable = SharedPreferencesUtils.getByPassCF();
        materialSwitch.setChecked(enable);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setByPassCF(isChecked);
            setByPassCFTimeOutSlider.setEnabled(isChecked);
        });
        setByPassCFTimeOutSlider.setEnabled(enable);
        setByPassCFTimeOutSlider.setValue(SharedPreferencesUtils.getByPassCFTimeout());
        setByPassCFTimeOutSlider.addOnChangeListener((slider, value, fromUser) -> Utils.setVibration(slider));
        setByPassCFTimeOutSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int num = Math.round(slider.getValue());
                SharedPreferencesUtils.setByPassCFTimeout(num);
            }
        });
        builder.setPositiveButton(getString(R.string.defaultPositiveBtnText), (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        alertDialog = builder.setView(view).create();
        alertDialog.setOnDismissListener(dialog -> application.showToastMsg(getString(R.string.setSuccess), DialogXTipEnum.SUCCESS));
        alertDialog.show();
    }

    /**
     * @方法名称: enableSniff
     * @方法描述: 是否开启资源嗅探
     * @日期: 2024/5/30 20:17
     * @作者: Li Z
     *
     * @返回:
     */
    public void enableSniff(int position) {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.DialogStyle);
        builder.setTitle(list.get(position).getTitle());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sniff, null);
        MaterialSwitch materialSwitch = view.findViewById(R.id.enableSniff);
        Slider setSniffTimeoutSlider = view.findViewById(R.id.setSniffTimeout);
        boolean enable = SharedPreferencesUtils.getEnableSniff();
        materialSwitch.setChecked(enable);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setEnableSniff(isChecked);
            setSniffTimeoutSlider.setEnabled(isChecked);
        });
        setSniffTimeoutSlider.setEnabled(enable);
        setSniffTimeoutSlider.setValue(SharedPreferencesUtils.getSniffTimeout());
        setSniffTimeoutSlider.addOnChangeListener((slider, value, fromUser) -> Utils.setVibration(slider));
        setSniffTimeoutSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int num = Math.round(slider.getValue());
                SharedPreferencesUtils.setSniffTimeout(num);
            }
        });
        builder.setPositiveButton(getString(R.string.defaultPositiveBtnText), (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        alertDialog = builder.setView(view).create();
        alertDialog.setOnDismissListener(dialog -> application.showToastMsg(getString(R.string.setSuccess), DialogXTipEnum.SUCCESS));
        alertDialog.show();
    }

    /**
     * @方法名称: setDefaultPlayer
     * @方法描述: 选择默认播放器
     * @日期: 2024/1/24 13:37
     * @作者: Li Z
     * @返回:
     */
    public void setDefaultPlayer(int position) {
        String[] items = (String[]) list.get(position).getOption();
        Utils.showSingleChoiceAlert(getActivity(),
                list.get(position).getTitle(),
                items,
                true,
                SharedPreferencesUtils.getUserSetOpenVidePlayer(),
                (dialogInterface, i) -> {
                    setDataSubTitle(position, items[i]);
                    SharedPreferencesUtils.setUserSetOpenVidePlayer(i);
                    dialogInterface.dismiss();
                });
    }

    /**
     * @方法名称: setTheme
     * @方法描述: 设置主题
     * @日期: 2024/1/24 13:36
     * @作者: Li Z
     * @返回:
     */
    public void setTheme(int position) {
        int chooseIndex = DarkModeUtils.chooseIndex(getActivity());
        Utils.showSingleChoiceAlert(getActivity(),
                list.get(position).getTitle(),
                themeItems,
                true,
                chooseIndex,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (i != chooseIndex) {
                        setDataSubTitle(position, themeItems[i]);
                        switch (i) {
                            case 0:
                                DarkModeUtils.applyDayMode(getActivity());
                                break;
                            case 1:
                                DarkModeUtils.applyNightMode(getActivity());
                                break;
                            case 2:
                                DarkModeUtils.applySystemMode(getActivity());
                                break;
                        }
                    }
                });
    }

    /**
     * @方法名称: setPlayerKernel
     * @方法描述: 选择播放器内核
     * @日期: 2024/1/24 13:36
     * @作者: Li Z
     * @返回:
     */
    public void setPlayerKernel(int position) {
        String[] items = (String[]) list.get(position).getOption();
        Utils.showSingleChoiceAlert(getActivity(),
                list.get(position).getTitle(),
                items,
                true,
                SharedPreferencesUtils.getUserSetPlayerKernel(),
                (dialogInterface, i) -> {
                    setDataSubTitle(position, items[i]);
                    SharedPreferencesUtils.setUserSetPlayerKernel(i);
                    dialogInterface.dismiss();
                });
    }

    /**
     * @方法名称: setDanmu
     * @方法描述: 播放器默认是否开启弹幕
     * @日期: 2024/1/24 13:36
     * @作者: Li Z
     * @返回:
     */
    public void setDanmu(int position) {
        String[] items = (String[]) list.get(position).getOption();
        Utils.showSingleChoiceAlert(getActivity(),
                list.get(position).getTitle(),
                items,
                true,
                SharedPreferencesUtils.getUserSetOpenDanmu() ? 0 : 1,
                (dialogInterface, i) -> {
                    setDataSubTitle(position, items[i]);
                    SharedPreferencesUtils.setUserSetOpenDanmu(i==0);
                    dialogInterface.dismiss();
                });
    }

    /**
     * m3u8协议下载配置
     * @param position
     */
    private void setM3U8DownloadConfig(int position) {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.DialogStyle);
        builder.setTitle(list.get(position).getTitle());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_m3u8_download_config, null);
        MaterialSwitch materialSwitch = view.findViewById(R.id.ignoreTs);
        materialSwitch.setChecked(SharedPreferencesUtils.getIgnoreTs());
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> SharedPreferencesUtils.setIgnoreTs(isChecked));
        Slider setM3u8QueueNumSlider = view.findViewById(R.id.setM3u8QueueNumSlider);
        setM3u8QueueNumSlider.setValue(SharedPreferencesUtils.getMaxTsQueueNum());
        setM3u8QueueNumSlider.addOnChangeListener((slider, value, fromUser) -> Utils.setVibration(slider));
        setM3u8QueueNumSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int num = Math.round(slider.getValue());
                SharedPreferencesUtils.setMaxTsQueueNum(num);
            }
        });
        boolean removeAdTsConfig = SharedPreferencesUtils.getRemoveAdTs();
//        String removeAdTsRegs = SharedPreferencesUtils.getRemoveAdTsRegs();
//        LinearLayout removeAdBoxView = view.findViewById(R.id.removeAdBox);
//        removeAdBoxView.setVisibility(removeAdTsConfig ? View.VISIBLE : View.GONE);
        MaterialSwitch removeAdTsSwitch = view.findViewById(R.id.removeAdTs);
        removeAdTsSwitch.setChecked(removeAdTsConfig);
        removeAdTsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            removeAdBoxView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SharedPreferencesUtils.setRemoveAdTs(isChecked);
        });
//        TextInputLayout regLayout = view.findViewById(R.id.regLayout);
//        regLayout.getEditText().setText(removeAdTsRegs);
//        Button resetBtn = view.findViewById(R.id.reset);
//        resetBtn.setOnClickListener(v -> {
//            regLayout.getEditText().setText(getString(R.string.setM3u8RegRemoveAdDefaultConfig));
//        });
        builder.setPositiveButton(getString(R.string.defaultPositiveBtnText), (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        alertDialog = builder.setView(view).create();
//        alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        alertDialog.setOnDismissListener(dialog -> {
//            if (removeAdTsSwitch.isChecked())
//                SharedPreferencesUtils.setRemoveAdTsRegs(regLayout.getEditText().getText().toString());
//            application.showToastMsg(getString(R.string.setM3u8QueueNumSuccess));
            application.showToastMsg(getString(R.string.setM3u8QueueNumSuccess), DialogXTipEnum.SUCCESS);
        });
        alertDialog.show();
    }

    private static final int READ_REQUEST_CODE = 42;
    private static int BACKUPS_POSITION;
    private static boolean inProgress = false;
    /**
     * 备份/恢复数据
     * @param position
     */
    private void backups(int position) {
        if (inProgress) {
            application.showToastMsg(getString(R.string.setBackupsInProgress), DialogXTipEnum.SUCCESS);
            return;
        }
        Utils.showSingleListAlert(getActivity(), getString(R.string.setBackupsTitle), Utils.getArray(R.array.backupsItems), true, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    // 创建备份文件
                    createBackupsFile(position);
                    break;
                case 1:
                    // 恢复数据
                    performFileSearch(position);
                    break;
            }
            dialogInterface.dismiss();
        });
    }

    /**
     * 创建备份文件
     */
    private void createBackupsFile(int position) {
        inProgress = true;
        String backupsFileName = getString(R.string.app_name) + System.currentTimeMillis()+".backups";
        String filePath = SAFUtils.checkHasSetDataSaveUri() ? getActivity().getFilesDir().getAbsolutePath()+ File.separator + backupsFileName : Utils.APP_DATA_PATH + File.separator + backupsFileName;
        adapter.getViewByPosition(position, R.id.progress).setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Map<String, Object> resultMap = BackupsManager.createBackupsFile(getActivity(), filePath, backupsFileName);
            getActivity().runOnUiThread(() -> {
                inProgress = false;
                adapter.notifyItemChanged(position);
                boolean success = (boolean) resultMap.get("success");
                if (success)
                    application.showToastMsg(String.format(getString(R.string.setBackupsFileComplete), resultMap.get("path")), DialogXTipEnum.SUCCESS);
                else
                    SAFUtils.showUnauthorizedAlert(
                            getActivity(),
                            (dialog, which) -> {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                startActivityForResult(intent, REQUEST_DOCUMENT_TREE);
                            });
            });
        });
    }

    private void performFileSearch(int position) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        BACKUPS_POSITION = position;
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DOCUMENT_TREE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                getActivity().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // 保存授权的目录
                SharedPreferencesUtils.setDataSaveUri(uri.toString());
                application.showToastMsg("已授权 "+ uri, DialogXTipEnum.SUCCESS);
                createBackupsFile(adapterItemPosition);
            }
        } else if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                inProgress = true;
                if (uri.toString().endsWith(".backups")) {
                    readTextFromUri(uri);
                } else {
                    inProgress = false;
                    application.showToastMsg(getString(R.string.setBackupsFileErrorMsg), DialogXTipEnum.ERROR);
                }
            }
        }
    }

    private void readTextFromUri(Uri uri) {
        adapter.getViewByPosition(adapterItemPosition, R.id.progress).setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                ParcelFileDescriptor parcelFileDescriptor =
                        getActivity().getContentResolver().openFileDescriptor(uri, "r");
                if (parcelFileDescriptor != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(parcelFileDescriptor.getFileDescriptor()), StandardCharsets.UTF_8))) {
                        JSONReader jsonReader = new JSONReader(reader);
                        jsonReader.startObject(); // 开始读取 JSON 对象

                        List<TVideo> tVideoList = null;
                        List<TFavorite> tFavorites = null;
                        List<THistory> tHistories = null;
                        List<THistoryData> tHistoryData = null;
                        List<TDirectory> tDirectories = null;

                        while (jsonReader.hasNext()) {
                            String key = jsonReader.readString(); // 读取 JSON 的 key
                            switch (key) {
                                case "videoList":
                                    tVideoList = jsonReader.readObject(new TypeReference<List<TVideo>>() {});
                                    break;
                                case "favoriteList":
                                    tFavorites = jsonReader.readObject(new TypeReference<List<TFavorite>>() {});
                                    break;
                                case "historyList":
                                    tHistories = jsonReader.readObject(new TypeReference<List<THistory>>() {});
                                    break;
                                case "historyDataList":
                                    tHistoryData = jsonReader.readObject(new TypeReference<List<THistoryData>>() {});
                                    break;
                                case "tDirectories":
                                    tDirectories = jsonReader.readObject(new TypeReference<List<TDirectory>>() {});
                                    break;
                                case "white_night_mode_sp":
                                    int darkMode = jsonReader.readInteger();
                                    DarkModeUtils.setNightModel(getActivity(), darkMode);
                                    break;
                                default:
                                    Object value = jsonReader.readObject();
                                    SharedPreferencesUtils.setParam(key, value);
                                    break;
                            }
                        }
                        jsonReader.endObject(); // 结束 JSON 读取

                        // 处理解析后的数据
                        BackupsManager.restoreBackup(tVideoList, tFavorites, tHistories, tHistoryData, tDirectories);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(() -> {
                        application.showToastMsg(getString(R.string.setBackupsRestoredMsg), DialogXTipEnum.SUCCESS);
                        initData();
                        adapter.notifyDataSetChanged();
                        switch (DarkModeUtils.chooseIndex(getActivity())) {
                            case 0:
                                DarkModeUtils.applyDayMode(getActivity());
                                break;
                            case 1:
                                DarkModeUtils.applyNightMode(getActivity());
                                break;
                            case 2:
                                DarkModeUtils.applySystemMode(getActivity());
                                break;
                        }
                        EventBus.getDefault().post(REFRESH_FAVORITE);
                        EventBus.getDefault().post(REFRESH_HISTORY);
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        adapter.notifyItemChanged(BACKUPS_POSITION);
                        application.showToastMsg(getString(R.string.setBackupsFileErrorMsg), DialogXTipEnum.ERROR);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    adapter.notifyItemChanged(BACKUPS_POSITION);
                    application.showToastMsg(e.getMessage(), DialogXTipEnum.ERROR);
                });
            }
            inProgress = false;
        });
    }

    /**
     * @方法名称: removeDownloads
     * @方法描述: 清除Aria所有下载记录
     * @日期: 2024/1/24 13:35
     * @作者: Li Z
     * @返回:
     */
    private void removeDownloads() {
        Utils.showAlert(getActivity(),
                getString(R.string.setRemoveDownloadsTitle),
                getString(R.string.setRemoveDownloadsDialogSubContent),
                true,
                getString(R.string.defaultPositiveBtnText),
                getString(R.string.defaultNegativeBtnText),
                getString(R.string.setRemoveDownloadsNeutralTitle),
                (dialogInterface, i) -> {
                    Aria.download(this).removeAllTask(false);
                    TDownloadManager.deleteAllDownloads();
                    EventBus.getDefault().post(REFRESH_DOWNLOAD);
                    application.showToastMsg(getString(R.string.setRemoveDownloadsSuccess), DialogXTipEnum.SUCCESS);
                    dialogInterface.dismiss();
                },
                (dialogInterface, i) -> dialogInterface.dismiss(),
                (dialogInterface, i) -> {
                    Aria.download(this).removeAllTask(false);
                    application.showToastMsg(getString(R.string.setRemoveDownloadsNeutralSuccess), DialogXTipEnum.SUCCESS);
                    dialogInterface.dismiss();
                });
    }

    /**
     * 检查是否存在更新
     */
    private void checkUpdate(int position) {
        if (checkUpdateEvent != null && checkUpdateEvent.isHasUpdate()) {
            Utils.showAlert(getActivity(),
                    checkUpdateEvent.getVersionTitle(),
                    checkUpdateEvent.getVersionContent(),
                    true,
                    getString(R.string.go2TheReleasePageBtnText),
                    getString(R.string.defaultNegativeBtnText),
                    null,
                    (dialog, which) -> Utils.viewInChrome(getActivity(), checkUpdateEvent.getReleaseUrl()),
                    (dialog, which) -> dialog.dismiss(),
                    null);
        } else if (!Utils.isServiceRunning(getContext(), CheckUpdateService.class)) {
            adapter.getViewByPosition(position, R.id.progress).setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                getActivity().startService(new Intent(getActivity(), CheckUpdateService.class));
            }, 500);
        }
    }

    @Override
    protected DomainListPresenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
        if (!Utils.isNullOrEmpty(mPresenter))
            mPresenter.loadData();
    }

    @Override
    protected void retryListener() {
        if (Utils.isNullOrEmpty(mPresenter))
            mPresenter = new DomainListPresenter(this);
        mPresenter.loadData();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEnum refresh) {
        switch (refresh) {
            case REFRESH_PLAYER_KERNEL:
                for (int index = 0; index < list.size(); index++) {
                    SettingAboutBean bean = list.get(index);
                    if (bean.getTitle().equals(getString(R.string.setPlayerKernelTitle))) {
                        String[] items = (String[]) bean.getOption();
                        bean.setSubTitle(items[SharedPreferencesUtils.getUserSetPlayerKernel()]);
                        adapter.notifyItemChanged(index);
                        break;
                    }
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckUpdateEvent(CheckUpdateEvent checkUpdateEvent) {
        this.checkUpdateEvent = checkUpdateEvent;
        application.showToastMsg(checkUpdateEvent.getMsg(), DialogXTipEnum.DEFAULT);
        for (int i=0,size=list.size(); i<size; i++) {
            if (list.get(i).getTitle().equals(getString(R.string.currentVersionTitle))) {
                if (checkUpdateEvent.isHasUpdate())
                    list.get(i).setEndIcon(R.drawable.round_fiber_new_24);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * @return
     * @方法名称: loadingView
     * @方法描述: 用于显示加载中视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void loadingView() {
        rvLoading();
    }

    /**
     * @param msg 错误文本信息
     * @return
     * @方法名称: errorView
     * @方法描述: 用于显示加载失败视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void errorView(String msg) {
        getActivity().runOnUiThread(() -> rvError(msg));
    }

    /**
     * @return
     * @方法名称: emptyView
     * @方法描述: 用于显示空数据视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void emptyView() {

    }

    @Override
    public void success(List<DomainDataBean.Domain> domainList) {
        getActivity().runOnUiThread(() -> domainListAdapter.setNewInstance(domainList));
    }
}
