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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VipVideoAdapter;
import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.custom.SmartGridSpacingDecoration;
import my.project.moviesbox.databinding.ActivityParsingInterfacesBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.ParsingInterfacesModel;
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
//        recyclerView.setLayoutManager(new GridLayoutManager(this, Utils.isPad() ? 10 : 4));
        recyclerView.setLayoutManager(new FlexboxLayoutManager(this));
        if (recyclerView.getTag() == null) {
            recyclerView.addItemDecoration(new SmartGridSpacingDecoration(16, true));
            recyclerView.setTag("decoration_added");
        }
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundle();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getBundle();
    }

    private void getBundle() {
        Intent intent = getIntent();
        if (intent == null) return;

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null) return;

        Matcher matcher = URL_PATTERN.matcher(sharedText);
        if (matcher.find()) {
            String newUrl = matcher.group();
            textInputLayout.getEditText().setText(newUrl);
            url = newUrl;
        }
    }

    private void initAdapter() {
        adapter = new VipVideoAdapter(this, dramasItemList);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            playVideo(position);
        });
        recyclerView.setAdapter(adapter);
        if (Utils.checkHasNavigationBar(this))
            recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
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
                mPresenter.parser(url, true);
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
    public void success(JSONObject object, boolean isEpisodes) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            infoView.setStrokeColor(getColor(R.color.red400));
            parser.setEnabled(true);
            if (isEpisodes) {
                // 限制消息
                String message = object.getString("ip-message");
                // 剧集列表
                vipVideoDataBean = new VipVideoDataBean();
                vipVideoDataBean.setTitle(object.getString("vod_title"));
                vipVideoDataBean.setImgUrl(object.getString("vod_pic"));
                vipVideoDataBean.setIntroduction(object.getString("vod_desc"));
                vipVideoDataBean.setDramaIntroduction(object.getString("vod_updateTo"));
                JSONArray vodEpisodes = object.getJSONArray("vod_episodes");
                dramasItemList = new ArrayList<>();
                for (int i=0,size=vodEpisodes.size(); i<size; i++) {
                    JSONObject episodes = vodEpisodes.getJSONObject(i);
                    String name = episodes.getString("name");
                    String url = episodes.getString("url");
                    VipVideoDataBean.DramasItem dramasItem = new VipVideoDataBean.DramasItem();
                    dramasItem.setTitle(name);
                    dramasItem.setIndex(i);
                    dramasItem.setUrl(url);
                    dramasItemList.add(dramasItem);
                }
                vipVideoDataBean.setDramasItemList(dramasItemList);
                titleView.setText(message + "\n" + vipVideoDataBean.getTitle());
                introductionView.setContent(vipVideoDataBean.getIntroduction());
                introductionView.setVisibility(View.VISIBLE);
                dramaIntroductionView.setText(vipVideoDataBean.getDramaIntroduction());
                dramaIntroductionView.setVisibility(View.VISIBLE);
                adapter.setNewInstance(dramasItemList);
                videoInfoView.setVisibility(View.VISIBLE);
                infoView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void error(String msg, boolean isEpisodes) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (isEpisodes) {
                infoView.setStrokeColor(getColor(R.color.red400));
                parser.setEnabled(true);
                infoView.setVisibility(View.VISIBLE);
                errorMsgView.setText(msg);
                errorMsgView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        emptyRecyclerView(recyclerView);
    }
}
