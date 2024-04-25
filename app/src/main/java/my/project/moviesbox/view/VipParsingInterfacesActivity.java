package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VipVideoAdapter;
import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.VipVideoDataBean;
import my.project.moviesbox.presenter.ParsingInterfacesPresenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: ParsingInterfacesActivity
  * @描述: 各大视频网站VIP影视解析界面<p>自用</p>
  * @作者: Li Z
  * @日期: 2024/2/23 15:37
  * @版本: 1.0
 */
public class VipParsingInterfacesActivity extends BaseActivity<ParsingInterfacesContract.View, ParsingInterfacesPresenter> implements
        ParsingInterfacesContract.View {
    public final static Pattern pattern = Pattern.compile("http(.*)\\.html");
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.urlLayout)
    TextInputLayout textInputLayout;
    @BindView(R.id.parser)
    Button parser;
    @BindView(R.id.infoView)
    MaterialCardView infoView;
    @BindView(R.id.errorMsg)
    TextView errorMsgView;
    @BindView(R.id.videoInfo)
    LinearLayout videoInfoView;
    @BindView(R.id.title)
    TextView titleView;
    @BindView(R.id.introduction)
    ExpandableTextView introductionView;
    @BindView(R.id.dramaIntroduction)
    TextView dramaIntroductionView;
    @BindView(R.id.rvList)
    RecyclerView recyclerView;
    private VipVideoDataBean vipVideoDataBean;
    private VipVideoAdapter adapter;
    private List<VipVideoDataBean.DramasItem> dramasItemList = new ArrayList<>();

    @Override
    protected ParsingInterfacesPresenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
        if (mPresenter != null) {
            mPresenter.parser();
        }
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_parsing_interfaces;
    }

    @Override
    protected void init() {
        getBundle();
        initToolbar();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {

    }


    @Override
    public void onResume() {
        super.onResume();
        setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, Utils.isPad() ? 10 : 5));
    }

    /**
     * 点击重试抽象方法
     *
     * @return
     */
    @Override
    protected void retryListener() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getBundle();
    }

    private void getBundle() {
        Intent intent = getIntent();
        if (intent != null) {
            String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                Matcher matcher = pattern.matcher(sharedText);
                if (matcher.find()) {
                    textInputLayout.getEditText().setText(matcher.group());
                    mPresenter = new ParsingInterfacesPresenter(matcher.group(), this);
                    loadData();
                }
            }
        }
    }

    private void initToolbar() {
        toolbar.setTitle(getString(R.string.vipVideoParserTitle));
        toolbar.setSubtitle(getString(R.string.vipVideoParserSubTitle));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            finish();
        });
    }

    private void initAdapter() {
        adapter = new VipVideoAdapter(this, dramasItemList);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            playVideo(position);
        });
        recyclerView.setAdapter(adapter);
    }

    private void playVideo(int position) {
        dramasItemList.get(position).setSelected(true);
        Bundle bundle = new Bundle();
        bundle.putString("videoTitle", vipVideoDataBean.getTitle());
        bundle.putString("dramaTitle", dramasItemList.get(position).getTitle());
        bundle.putString("url", dramasItemList.get(position).getUrl());
        bundle.putSerializable("list", (Serializable) dramasItemList);
        startActivity(new Intent(this, VipParsingInterfacesPlayerActivity.class).putExtras(bundle));
    }

    @OnClick({R.id.parser, R.id.videoUrl})
    public void parserVideoUrl(View view) {
        switch (view.getId()) {
            case R.id.parser: // 视频解析
                textInputLayout.setError(null);
                String url = textInputLayout.getEditText().getText().toString().trim();
                textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        textInputLayout.setError(null);
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                if (Utils.isNullOrEmpty(url)) {
                    textInputLayout.setError("URL不能为空");
                    return;
                }
                if (!Patterns.WEB_URL.matcher(url).matches()) {
                    textInputLayout.setError("请输入正确的URL");
                    return;
                }
                mPresenter = new ParsingInterfacesPresenter(url, this);
                loadData();
                break;
            case R.id.videoUrl:
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.setOnMenuItemClickListener(item1 -> {
                    String vipUrl = "";
                    switch (item1.getItemId()) {
                        case R.id.qq:
                            vipUrl = "https://v.qq.com/";
                            break;
                        case R.id.iqiyi:
                            vipUrl = "https://www.iqiyi.com/";
                            break;
                        case R.id.youku:
                            vipUrl = "https://www.youku.com/";
                            break;
                        case R.id.mangguo:
                            vipUrl = "https://www.mgtv.com/";
                            break;
                    }
                    Utils.viewInChrome(this, vipUrl);
                    return true;
                });
                popupMenu.inflate(R.menu.vip_popup_menu);
                popupMenu.show();
                break;
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
        if (isFinishing()) return;
        parser.setEnabled(false);
        application.showSnackbarMsg(toolbar, "尝试获取影视信息");
        infoView.setVisibility(View.GONE);
        videoInfoView.setVisibility(View.GONE);
        errorMsgView.setVisibility(View.GONE);
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
        if (isFinishing()) return;
        runOnUiThread(() -> {
            infoView.setStrokeColor(getColor(R.color.red400));
            parser.setEnabled(true);
            infoView.setVisibility(View.VISIBLE);
            errorMsgView.setText(msg);
            errorMsgView.setVisibility(View.VISIBLE);
        });
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
    public void success(Object object) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            infoView.setStrokeColor(getColor(R.color.red400));
            parser.setEnabled(true);
            JSONObject jsonObject = (JSONObject) object;
            try {
                vipVideoDataBean = new VipVideoDataBean();
                dramasItemList = new ArrayList<>();

                if (jsonObject.getInteger("code") == 200) {
                    String name = jsonObject.getString("name");
                    String url = jsonObject.getString("url");
                    String aes_key = jsonObject.getString("aes_key");
                    String aes_iv = jsonObject.getString("aes_iv");
                    String videoInfoHtml = getData(aes_iv, aes_key, jsonObject.getString("html"));
                    LogUtil.logInfo("videoInfoHtml", videoInfoHtml);
                    if (Utils.isNullOrEmpty(videoInfoHtml)) {
                        handleEmptyVideoInfo(name, aes_iv, aes_key, url);
                    } else {
                        handleNonEmptyVideoInfo(name, videoInfoHtml, aes_iv, aes_key, url);
                    }
                    vipVideoDataBean.setDramasItemList(dramasItemList);
                    adapter.setNewInstance(dramasItemList);
                    videoInfoView.setVisibility(View.VISIBLE);
                    infoView.setStrokeColor(getColor(R.color.green400));
                    application.showSnackbarMsgAction(toolbar, "成功：" + jsonObject.getString("iptime"), "好", v -> {

                    });
                } else {
                    errorMsgView.setText(jsonObject.toJSONString());
                    errorMsgView.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMsgView.setText(e.getMessage());
                errorMsgView.setVisibility(View.VISIBLE);
            }
            infoView.setVisibility(View.VISIBLE);
        });
    }

    private void handleEmptyVideoInfo(String name, String aes_iv, String aes_key, String url) {
        VipVideoDataBean.DramasItem dramasItem = new VipVideoDataBean.DramasItem();
        dramasItem.setTitle(name);
        dramasItem.setIndex(0);
        dramasItem.setUrl(getData(aes_iv, aes_key, url));
        dramasItemList.add(dramasItem);

        titleView.setText(name);
        introductionView.setVisibility(View.GONE);
        dramaIntroductionView.setVisibility(View.GONE);
    }

    private void handleNonEmptyVideoInfo(String title, String videoInfoHtml, String aes_iv, String aes_key, String url) {
        Document document = Jsoup.parse(videoInfoHtml);
        String imgUrl = document.select(".bj").attr("src");
        String htmlTitle = document.select(".anthology-title-wrap .title").text();
        title = Utils.isNullOrEmpty(htmlTitle) ? title : htmlTitle;
        String introduction = document.select(".title-info").text();
        String dramaIntroduction = document.select(".component-title").text();

        vipVideoDataBean.setTitle(title);
        vipVideoDataBean.setImgUrl(imgUrl);
        vipVideoDataBean.setIntroduction(introduction);
        vipVideoDataBean.setDramaIntroduction(dramaIntroduction);

        Element listElem = document.getElementById("listShow");
        if (Utils.isNullOrEmpty(listElem)) {
            handleEmptyList(aes_iv, aes_key, url);
        } else {
            handleNonEmptyList(listElem);
        }

        titleView.setText(title);
        if (!Utils.isNullOrEmpty(introduction)) {
            introductionView.setContent(introduction);
            introductionView.setVisibility(View.VISIBLE);
        } else
            introductionView.setVisibility(View.GONE);
        if (!Utils.isNullOrEmpty(dramaIntroduction)) {
            dramaIntroductionView.setText(dramaIntroduction);
            dramaIntroductionView.setVisibility(View.VISIBLE);
        } else
            dramaIntroductionView.setVisibility(View.GONE);
    }

    private void handleEmptyList(String aes_iv, String aes_key, String url) {
        VipVideoDataBean.DramasItem dramasItem = new VipVideoDataBean.DramasItem();
        dramasItem.setTitle("全集");
        dramasItem.setIndex(0);
        dramasItem.setUrl(getData(aes_iv, aes_key, url));
        dramasItemList.add(dramasItem);
    }

    private void handleNonEmptyList(Element listElem) {
        Elements list = listElem.select("a");
        int i = 0;
        for (Element a : list) {
            String href = a.attr("onclick");
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                VipVideoDataBean.DramasItem dramasItem = new VipVideoDataBean.DramasItem();
                dramasItem.setTitle(a.text());
                dramasItem.setIndex(i);
                dramasItem.setUrl(matcher.group());
                dramasItemList.add(dramasItem);
                i++;
            }
        }
    }


    /**
     * 解密数据
     * @param aes_iv
     * @param aes_key
     * @param data
     * @return
     */
    public static String getData(String aes_iv, String aes_key, String data) {
        try {
            IvParameterSpec iv = new IvParameterSpec(aes_iv.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec sKeySpec = new SecretKeySpec(aes_key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(data));
            String result = new String(original, StandardCharsets.UTF_8);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
