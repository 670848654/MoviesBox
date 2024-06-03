package my.project.moviesbox.view;

import static android.app.Activity.RESULT_OK;

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
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.arialyy.aria.core.Aria;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.SettingAboutAdapter;
import my.project.moviesbox.bean.SettingAboutBean;
import my.project.moviesbox.database.entity.TFavorite;
import my.project.moviesbox.database.entity.THistory;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.database.entity.TVideo;
import my.project.moviesbox.database.manager.BackupsManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.enums.SettingEnum;
import my.project.moviesbox.event.CheckUpdateEvent;
import my.project.moviesbox.event.RefreshEvent;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.presenter.Presenter;
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
public class SettingFragment extends BaseFragment {
    private static final int REQUEST_DOCUMENT_TREE = 10000;
    @BindView(R.id.title)
    TextView titleView;
    private View view;
    private final int source = parserInterface.getSource();
    private String defaultPrefix;
    private final String[] themeItems = Utils.getArray(R.array.theme);
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private SettingAboutAdapter adapter;
    private final List<SettingAboutBean> list = SettingEnum.getSettingAboutBeanList();
    private CheckUpdateEvent checkUpdateEvent;
    private HomeActivity homeActivity;
    private int adapterItemPosition;

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
        String defaultDomain = parserInterface.getDefaultDomain();
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.DialogStyle);
        builder.setTitle(Html.fromHtml(getString(R.string.setDomainDialogTitle)));
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_domain, null);
        TextView websiteView = view.findViewById(R.id.website);
        String websiteReleaseHref = SourceEnum.getWebsiteReleaseBySource(source);
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
        }
        AutoCompleteTextView prefixView = view.findViewById(R.id.prefix);
        String[] prefixArr = Utils.getArray(R.array.prefix);
        defaultPrefix = prefixArr[defaultDomain.startsWith("https") ? 1 : 0];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.text_list_item, prefixArr);
        prefixView.setText(adapter.getItem(defaultDomain.startsWith("https") ? 1 : 0));
        prefixView.setAdapter(adapter);
        prefixView.setOnItemClickListener((parent, textView, position, id) -> defaultPrefix = parent.getItemAtPosition(position).toString());
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
        textInputLayout.getEditText().setText(defaultDomain.replaceAll("https?://", ""));
        builder.setPositiveButton(getString(R.string.setDomainPositiveBtnText), (dialog, which) -> {
            String text = textInputLayout.getEditText().getText().toString();
            if (!Utils.isNullOrEmpty(text)) {
                if (Patterns.WEB_URL.matcher(text).matches()) {
                    if (text.endsWith("/"))
                        text = text.substring(0, text.length() - 1);
                    String newDomain = defaultPrefix + text;
                    SharedPreferencesUtils.setUserSetDomain(source, newDomain);
                    setDataSubTitle(dataPosition, newDomain);
                    dialog.dismiss();
                    EventBus.getDefault().post(new RefreshEvent(0));
//                    application.showToastMsg(String.format(getString(R.string.setDomainSuccessText), newDomain));
                    homeActivity.showBottomNavigationViewSnackbar(titleView, String.format(getString(R.string.setDomainSuccessText), newDomain), false);
                } else
                    textInputLayout.getEditText().setError(getString(R.string.setDomainErrorText2));
            }
            else textInputLayout.getEditText().setError(getString(R.string.setDomainErrorText1));
        });
        builder.setNegativeButton(getString(R.string.defaultNegativeBtnText), null);
        builder.setNeutralButton(getString(R.string.setDomainNeutralBtnText), (dialog, which) -> {
            SharedPreferencesUtils.setUserSetDomain(source, SourceEnum.getDomainUrlBySource(source));
            setDataSubTitle(dataPosition, SourceEnum.getDomainUrlBySource(source));
            EventBus.getDefault().post(new RefreshEvent(0));
            dialog.dismiss();
        });
        builder.setCancelable(true);
        alertDialog = builder.setView(view).create();
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
        setSniffTimeoutSlider.addOnChangeListener((slider, value, fromUser) -> slider.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING));
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
//        alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        alertDialog.setOnDismissListener(dialog -> homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setSuccess), false));
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
        setM3u8QueueNumSlider.addOnChangeListener((slider, value, fromUser) -> slider.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING));
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
            homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setM3u8QueueNumSuccess), false);
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
//            application.showToastMsg(getString(R.string.setBackupsInProgress));
            homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setBackupsInProgress), true);
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
                    homeActivity.showBottomNavigationViewSnackbar(titleView, String.format(getString(R.string.setBackupsFileComplete), resultMap.get("path")), false);
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
                homeActivity.showBottomNavigationViewSnackbar(titleView, "已授权 "+ uri, false);
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
//                    application.showToastMsg(getString(R.string.setBackupsFileErrorMsg));
                    homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setBackupsFileErrorMsg), true);
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
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(parcelFileDescriptor.getFileDescriptor())))) {
                        String line;
                        StringBuilder stringBuilder = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        JSONObject jsonObject = JSON.parseObject(stringBuilder.toString());
                        List<TVideo> tVideoList = new ArrayList<>();
                        List<TFavorite> tFavorites = new ArrayList<>();
                        List<THistory> tHistories = new ArrayList<>();
                        List<THistoryData> tHistoryData = new ArrayList<>();
                        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if (key.equals("white_night_mode_sp")) {
                                DarkModeUtils.setNightModel(getActivity(), (Integer) value);
                            } else if (key.equals("videoList")) {
                                JSONArray array = (JSONArray) value;
                                tVideoList = JSONObject.parseArray(array.toJSONString(), TVideo.class);
                            } else if (key.equals("favoriteList")) {
                                JSONArray array = (JSONArray) value;
                                tFavorites = JSONObject.parseArray(array.toJSONString(), TFavorite.class);
                            } else if (key.equals("historyList")) {
                                JSONArray array = (JSONArray) value;
                                tHistories = JSONObject.parseArray(array.toJSONString(), THistory.class);
                            } else if (key.equals("historyDataList")) {
                                JSONArray array = (JSONArray) value;
                                tHistoryData = JSONObject.parseArray(array.toJSONString(), THistoryData.class);
                            } else
                            {
                                SharedPreferencesUtils.setParam(key, value);
                            }
                        }
                        BackupsManager.restoreBackup(tVideoList, tFavorites, tHistories, tHistoryData);
                    }
                    getActivity().runOnUiThread(() -> {
//                        application.showToastMsg(getString(R.string.setBackupsRestoredMsg));
                        homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setBackupsRestoredMsg), false);
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
                        EventBus.getDefault().post(new RefreshEvent(1));
                        EventBus.getDefault().post(new RefreshEvent(2));
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        adapter.notifyItemChanged(BACKUPS_POSITION);
//                        application.showToastMsg(getString(R.string.setBackupsFileErrorMsg));
                        homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setBackupsFileErrorMsg), true);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    adapter.notifyItemChanged(BACKUPS_POSITION);
//                    application.showToastMsg(e.getMessage());
                    homeActivity.showBottomNavigationViewSnackbar(titleView, e.getMessage(), true);
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
                    EventBus.getDefault().post(new RefreshEvent(99));
                    Aria.download(this).removeAllTask(false);
                    TDownloadManager.deleteAllDownloads();
                    EventBus.getDefault().post(new RefreshEvent(3));
//                    application.showToastMsg(getString(R.string.setRemoveDownloadsSuccess));
                    homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setRemoveDownloadsSuccess), false);
                    dialogInterface.dismiss();
                },
                (dialogInterface, i) -> dialogInterface.dismiss(),
                (dialogInterface, i) -> {
                    Aria.download(this).removeAllTask(false);
                    homeActivity.showBottomNavigationViewSnackbar(titleView, getString(R.string.setRemoveDownloadsNeutralSuccess), false);
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
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected void retryListener() {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent refresh) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckUpdateEvent(CheckUpdateEvent checkUpdateEvent) {
        this.checkUpdateEvent = checkUpdateEvent;
//        application.showToastMsg(checkUpdateEvent.getMsg());
        homeActivity.showBottomNavigationViewSnackbar(titleView, checkUpdateEvent.getMsg(), false);
        for (int i=0,size=list.size(); i<size; i++) {
            if (list.get(i).getTitle().equals(getString(R.string.currentVersionTitle))) {
                if (checkUpdateEvent.isHasUpdate())
                    list.get(i).setEndIcon(R.drawable.round_fiber_new_24);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }
}
