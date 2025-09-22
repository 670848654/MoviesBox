package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.MaterialShapeDrawable;
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

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VipVideoAdapter;
import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.databinding.ActivityParsingInterfacesBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.ParsingInterfacesModel;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.VipVideoDataBean;
import my.project.moviesbox.presenter.ParsingInterfacesPresenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseMvpActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: ParsingInterfacesActivity
  * @描述: 各大视频网站VIP影视解析界面<p>自用</p>
  * @作者: Li Z
  * @日期: 2024/2/23 15:37
  * @版本: 1.0
 */
public class VipParsingInterfacesActivity extends BaseMvpActivity<ParsingInterfacesModel, ParsingInterfacesContract.View, ParsingInterfacesPresenter, ActivityParsingInterfacesBinding> implements
        ParsingInterfacesContract.View {
    public final static String TEST_URL = "https://www.iqiyi.com/v_2399g3yo9f0.html?ht=2&ischarge=true&lt=2&tvname=%E4%B8%8E%E6%99%8B%E9%95%BF%E5%AE%89%E7%AC%AC10%E9%9B%86&vid=08da6c7063675b3678f646261f227e1b&fid=8144089039617901&vtype=0&f_block=selector_bk-undefined&s2=wna_tvg_1st&s3=wna_tvg_select&s4=10&vfrm=pcw_home&vfrmblk=pca_recommend_focus&vfrmrst=small_image1&pb2=bkt%3D%26c1%3D%26childinfo%3D%26e%3D%26fatherid%3D%26position%3D4%26r_area%3D%26r_source%3D%26recext%3D%26sc1%3D%26sqpid%3D%26stype%3D%26tagemode%3D0&ab=8883_A%2C10385_B%2C8185_A%2C10274_B%2C8739_B%2C9419_A%2C9922_C%2C9379_B%2C10590_D%2C10276_B%2C11389_B%2C8004_B%2C5257_B%2C10566_C%2C9776_B%2C8873_E%2C10123_A%2C7423_C%2C9082_B%2C8401_A%2C6249_C%2C10793_B%2C7996_B%2C11391_A%2C9576_B%2C10358_B%2C10897_B%2C9365_B%2C5465_B%2C6843_B%2C11816_A%2C6578_B%2C6312_B%2C6091_B%2C8690_A%2C10992_B%2C8737_D%2C11400_C%2C8742_A%2C10193_B%2C10803_C%2C10596_B%2C9484_B%2C6752_C%2C11716_B%2C10311_B%2C11171_B%2C10698_B%2C10237_B%2C10188_A%2C8971_C%2C7332_B%2C9683_B%2C10383_B%2C11402_A%2C8665_D%2C12103_B%2C11237_B%2C10575_B%2C11642_B%2C6237_B%2C9569_B%2C11004_B%2C11238_A%2C8983_B%2C7024_C%2C5592_B%2C9117_A%2C6031_B%2C10509_B%2C7581_A%2C9506_B%2C11393_A%2C9517_C%2C10216_B%2C9394_B%2C11350_B%2C8542_B%2C6050_B%2C9167_C%2C10637_B%2C11556_C%2C11413_B%2C11819_B%2C10551_B%2C9469_B%2C10633_B%2C10598_B%2C8812_B%2C11245_B%2C6832_C%2C7074_C%2C7682_C%2C8867_B%2C5924_D%2C6151_C%2C5468_B%2C10447_B%2C11580_C%2C11299_C%2C6704_C%2C10530_B%2C11672_B%2C11987_D%2C8808_B%2C10765_B%2C12098_D%2C8497_B%2C8342_B%2C8871_C%2C11095_B%2C9790_B%2C11754_A%2C9355_B%2C10389_B%2C8760_B%2C12028_D%2C11441_A%2C10624_C%2C10627_B%2C9292_B%2C6629_B%2C5670_B%2C9158_A%2C10541_B%2C9805_B%2C9959_B%2C10999_A%2C11578_A%2C6082_B%2C5335_B%2C11625_C%2C11224_B%2C11471_A%2C11032_B%2C10271_C";
    public final static String OLD_API = "https://cache.hls.one/xmflv.js";
    public final static String NEW_API = "https://202.189.8.170/Api.js";
    private boolean useNewApi = true;
    public final static Pattern URL_PATTERN = Pattern.compile("https://[^\\']*");
    private String url = "";
    private String danmuUrl = "";
    private String dmid = "";
    private VipVideoDataBean vipVideoDataBean;
    private VipVideoAdapter adapter;
    private List<VipVideoDataBean.DramasItem> dramasItemList = new ArrayList<>();

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityParsingInterfacesBinding inflateBinding(LayoutInflater inflater) {
        return ActivityParsingInterfacesBinding.inflate(inflater);
    }

    private AppBarLayout appBar;
    private Toolbar toolbar;
    private TextInputLayout textInputLayout;
    private Button parser;
    private TextView jsonView;
    private MaterialCardView infoView;
    private TextView errorMsgView;
    private LinearLayout videoInfoView;
    private TextView titleView;
    private ExpandableTextView introductionView;
    private TextView dramaIntroductionView;
    private RecyclerView recyclerView;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        textInputLayout = binding.urlLayout;
        parser = binding.parser;
        jsonView = binding.json;
        infoView = binding.infoView;
        errorMsgView = binding.errorMsg;
        videoInfoView = binding.videoInfo;
        titleView = binding.title;
        introductionView = binding.introduction;
        dramaIntroductionView = binding.dramaIntroduction;
        recyclerView = binding.rvList;
    }

    @Override
    public void initClickListeners() {
        binding.parser.setOnClickListener(v -> parserVideoUrl(v));
        binding.videoUrl.setOnClickListener(v -> parserVideoUrl(v));
    }

    @Override
    protected ParsingInterfacesPresenter createPresenter() {
        return new ParsingInterfacesPresenter(this);
    }

    @Override
    protected void loadData() {
    }

    private void loadData(String api) {
        mPresenter.parser(api, url);
    }

    @Override
    protected void init() {
        getBundle();
        setToolbar(toolbar, getString(R.string.vipVideoParserTitle), getString(R.string.vipVideoParserSubTitle));
        initAdapter();
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
                Matcher matcher = URL_PATTERN.matcher(sharedText);
                if (matcher.find()) {
                    textInputLayout.getEditText().setText(matcher.group());
                    url = matcher.group();
                }
            }
        }
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
        String url = dramasItemList.get(position).getUrl();
        if (Utils.isNullOrEmpty(url)) {
            application.showToastMsg("接口返回播放地址url为null", DialogXTipEnum.ERROR);
            return;
        }
        dramasItemList.get(position).setSelected(true);
        Bundle bundle = new Bundle();
        bundle.putString("videoTitle", vipVideoDataBean.getTitle());
        bundle.putString("dramaTitle", dramasItemList.get(position).getTitle());
        bundle.putString("url", url);
        bundle.putString("danmuUrl", danmuUrl);
        bundle.putString("dmid", dmid);
        bundle.putSerializable("list", (Serializable) dramasItemList);
        startActivity(new Intent(this, VipParsingInterfacesPlayerActivity.class).putExtras(bundle));
    }

    public void parserVideoUrl(View view) {
        switch (view.getId()) {
            case R.id.parser: // 视频解析
                textInputLayout.setError(null);
                jsonView.setVisibility(View.GONE);
                url = textInputLayout.getEditText().getText().toString().trim();
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
                useNewApi = true;
                loadData(NEW_API);
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
        application.showToastMsg("尝试获取影视信息", DialogXTipEnum.DEFAULT);
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
                    String info = useNewApi ? "当前解析为【新】Api,新接口移动端页面URL解析后貌似不显示剧集标题和集数！直接点击播放！" : "当前解析为【旧】Api";
                    jsonView.setText(info+"\n接口返回信息如下:\n"+JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat));
                    jsonView.setVisibility(View.VISIBLE);
                    String name = jsonObject.getString("name");
                    name = Utils.isNullOrEmpty(name) ? "未知" : name;
                    String url = jsonObject.getString("url");
                    String aes_key = jsonObject.getString("aes_key");
                    String aes_iv = jsonObject.getString("aes_iv");
                    danmuUrl = jsonObject.getString("ggdmapi");
                    dmid = jsonObject.getString("dmid");
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
                    application.showToastMsg( "成功：" + jsonObject.getString("iptime"), DialogXTipEnum.SUCCESS);
                } else if (useNewApi) {
                    // 当新API返回不是200时 调用老接口
                    useNewApi = false;
                    loadData(NEW_API);
                    return;
                }  else {
                    errorMsgView.setText("接口请求失败！\n"+jsonObject.toJSONString());
                    errorMsgView.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMsgView.setText("接口请求失败,错误信息如下\n"+e.getMessage());
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
            /*Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                VipVideoDataBean.DramasItem dramasItem = new VipVideoDataBean.DramasItem();
                dramasItem.setTitle(a.text());
                dramasItem.setIndex(i);
                dramasItem.setUrl(matcher.group());
                dramasItemList.add(dramasItem);
                i++;
            }*/
            VipVideoDataBean.DramasItem dramasItem = new VipVideoDataBean.DramasItem();
            dramasItem.setTitle(a.text());
            dramasItem.setIndex(i);
            dramasItem.setUrl(href);
            dramasItemList.add(dramasItem);
            i++;
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
            String result = new String(original, StandardCharsets.UTF_8).replaceAll("\u000F", "");
            LogUtil.logInfo("播放地址", result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        emptyRecyclerView(recyclerView);
    }
}
