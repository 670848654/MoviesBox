package my.project.moviesbox.view;

import static my.project.moviesbox.utils.Utils.DOWNLOAD_SAVE_PATH;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arialyy.aria.core.Aria;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DetailsListItemAdapter;
import my.project.moviesbox.adapter.DownloadDramaAdapter;
import my.project.moviesbox.adapter.DramaAdapter;
import my.project.moviesbox.bean.DownloadDramaBean;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.config.M3U8DownloadConfig;
import my.project.moviesbox.contract.DetailsContract;
import my.project.moviesbox.contract.DownloadVideoContract;
import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.event.DramaEvent;
import my.project.moviesbox.event.RefreshEvent;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.config.ParserInterfaceFactory;
import my.project.moviesbox.presenter.DetailsPresenter;
import my.project.moviesbox.presenter.DownloadVideoPresenter;
import my.project.moviesbox.presenter.VideoPresenter;
import my.project.moviesbox.service.DownloadService;
import my.project.moviesbox.utils.SAFUtils;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.StatusBarUtil;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoUtils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: DetailsActivity
  * @描述: 影视详情视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:09
  * @版本: 1.0
 */
public class DetailsActivity extends BaseActivity<DetailsContract.View, DetailsPresenter> implements DetailsContract.View, VideoContract.View, DownloadVideoContract.View {
    /**
     * appBarLayout
     */
    @BindView(R.id.app_bar_margin)
    LinearLayout appBarMargin;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    /**
     * 收藏按钮视图
     */
    @BindView(R.id.favorite)
    MaterialButton favoriteBtn;
    /**
     * 影视图片视图
     */
    @BindView(R.id.vod_img)
    ImageView vodImgView;
    /**
     * 影视详情视图
     */
    @BindView(R.id.desc)
    ExpandableTextView expandableDescView;
    /**
     * 影视标题视图
     */
    @BindView(R.id.title)
    TextView titleView;
    /**
     * 下拉刷新视图
     */
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    /**
     * detailsTitle 详情标题
     * detailsUrl 详情源地址
     * dramaUrl 播放页源地址
     * dramaTitle 播放集数标题
     */
    private String detailsTitle, detailsUrl, dramaUrl, dramaTitle, downloadDramaUrl, downloadDramaNumber;
    private AlertDialog alertDialog;
    /**
     * 是否收藏
     */
    private boolean isFavorite;
    private DetailsDataBean detailsDataBean = new DetailsDataBean();
    /**
     * 记住点击过的影视 用于手势返回时回到上一影视信息
     */
    private List<String> vodUrlList = new ArrayList();
    /**
     * 剧集列表
     */
    private List<DetailsDataBean.DramasItem> dramaList;
    /**
     * 播放列表相关
     */
    @BindView(R.id.drama_list)
    RecyclerView dramaListRv;
    private LinearLayoutManager dramaListLayoutManager;
    private DramaAdapter dramaListAdapter;
    @BindView(R.id.play_layout)
    LinearLayout playLinearLayout;
    /**
     * 展开播放列表相关
     */
    private RecyclerView expandListRv;
    private DramaAdapter expandListAdapter;
    private BottomSheetDialog expandListBSD;
    @BindView(R.id.open_drama)
    RelativeLayout openDramaView;
    /**
     * 多季相关
     */
    @BindView(R.id.multi_list)
    RecyclerView multiListRv;
    private LinearLayoutManager multiLayoutManager;
    private DetailsListItemAdapter multiAdapter;
    @BindView(R.id.multi_layout)
    LinearLayout multiLinearLayout;
    /**
     * 相关推荐相关
     */
    @BindView(R.id.recommend_list)
    RecyclerView recommendRv;
    private LinearLayoutManager recommendLayoutManager;
    private DetailsListItemAdapter recommendAdapter;
    @BindView(R.id.recommend_layout)
    LinearLayout recommendLinearLayout;
    /**
     * 加载错误视图相关
     */
    @BindView(R.id.error_bg)
    RelativeLayout errorBgView;
    @BindView(R.id.error_msg)
    TextView errorMsgView;
    /**
     * 详情视图
     */
    @BindView(R.id.desc_view)
    RelativeLayout descView;
    /**
     * 背景模糊图片
     */
    @BindView(R.id.bg)
    ImageView bgView;
    /**
     * TAG列表
     */
    @BindView(R.id.chip_group)
    ChipGroup chipGroupView;
    @BindView(R.id.info)
    TextView infoView;
    /**
     * 更新时间视图
     */
    @BindView(R.id.update_time)
    TextView updateTimeView;
    /**
     * 评分视图
     */
    @BindView(R.id.score_view)
    AppCompatTextView scoreView;
    /**
     * 整体滚动视图
     */
    @BindView(R.id.scrollview)
    NestedScrollView scrollView;
    /**
     * 当前点击剧集
     */
    private int clickIndex;
    /**
     * 影视ID
     */
    private String vodId;
    /**
     * 下载相关参数
     */
    private String savePath; // 下载保存路劲
    private DownloadVideoPresenter downloadVideoPresenter; // 下载适配器
    private RecyclerView downloadListRv;
    private BottomSheetDialog downloadBottomSheetDialog;
    private List<DownloadDramaBean> downloadBean = new ArrayList<>();
    private DownloadDramaAdapter downloadAdapter;
    /**
     * 多播放列表选择相关
     */
    @BindView(R.id.selected_text)
    AutoCompleteTextView selectedDrama;
    private List<String> dramaTitles;
    private ArrayAdapter dramaTitlesApter;
    private int sourceIndex = 0; // 所选播放列表下标
    private VideoPresenter videoPresenter;

    @Override
    protected DetailsPresenter createPresenter() {
        return new DetailsPresenter(detailsUrl, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_details;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0);
        expandableDescView.setNeedExpend(true);
        getBundle();
        initToolbar();
        initSwipe();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            detailsUrl = bundle.getString("url");
            detailsTitle = bundle.getString("title");
            vodUrlList.add(detailsUrl);
        }
    }

    private void initToolbar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            finish();
        });
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() > 0) {
                toolbar.setTitle(detailsTitle);
            } else
            {
                mSwipe.setEnabled(true);
                toolbar.setTitle("");
            }

        });
    }

    @OnClick({R.id.order, R.id.favorite, R.id.download, R.id.browser,R.id.drama})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.order:
                Collections.reverse(dramaList);
                dramaListAdapter.notifyDataSetChanged();
                expandListAdapter.notifyDataSetChanged();
                break;
            case R.id.favorite:
                favoriteVod();
                break;
            case R.id.download:
                select2Download();
                break;
            case R.id.browser:
                Utils.viewInChrome(DetailsActivity.this, detailsUrl);
//                startActivity(new Intent(this, WebViewActivity.class).putExtra("url", detailsUrl));
                break;
            case R.id.drama:
                if (!expandListBSD.isShowing()) {
                    expandListBSD.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                    expandListBSD.show();
                }
                break;
        }
    }

    private void select2Download() {
        if (downloadAdapter.getData().size() == 0)
            return;
        if (!Utils.isWifiConnected(this)) {
            Utils.showAlert(this,
                    getString(R.string.noWifiDialogTitle),
                    getString(R.string.noWifiDialogContent),
                    false,
                    getString(R.string.defaultPositiveBtnText),
                    getString(R.string.defaultNegativeBtnText),
                    "",
                    (dialog, which) -> showDownloadBsd(),
                    (dialog, which) -> dialog.dismiss(),
                    null);
        } else
            showDownloadBsd();
    }

    private void showDownloadBsd() {
        checkHasDownload();
        downloadBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        downloadBottomSheetDialog.show();
    }

    private void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() ->  {
            retryListener();
        });
    }

    private void initAdapter() {
        dramaListAdapter = new DramaAdapter(this, dramaList);
        dramaListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            playVideo(dramaListAdapter, position);
        });
        dramaListLayoutManager = getLinearLayoutManager();
        dramaListRv.setLayoutManager(dramaListLayoutManager);
        dramaListRv.setAdapter(dramaListAdapter);
        dramaListRv.setNestedScrollingEnabled(false);

        multiAdapter = new DetailsListItemAdapter(detailsDataBean.getMultiList());
        multiAdapter.setOnItemClickListener((adapter, view, position) -> {
            DetailsDataBean.Recommend bean = (DetailsDataBean.Recommend) adapter.getItem(position);
            detailsTitle = bean.getTitle();
            detailsUrl = bean.getUrl();
            vodUrlList.add(detailsUrl);
            openVodDesc();
        });
        multiLayoutManager = getLinearLayoutManager();
        multiListRv.setLayoutManager(multiLayoutManager);
        multiListRv.setAdapter(multiAdapter);
        multiListRv.setNestedScrollingEnabled(false);

        recommendAdapter = new DetailsListItemAdapter(detailsDataBean.getRecommendList());
        recommendAdapter.setOnItemClickListener((adapter, view, position) -> {
            DetailsDataBean.Recommend bean = (DetailsDataBean.Recommend) adapter.getItem(position);
            detailsTitle = bean.getTitle();
            detailsUrl = bean.getUrl();
            vodUrlList.add(detailsUrl);
            openVodDesc();
        });
        recommendLayoutManager = getLinearLayoutManager();
        recommendRv.setLayoutManager(recommendLayoutManager);
        if (Utils.checkHasNavigationBar(this)) recommendRv.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        recommendRv.setAdapter(recommendAdapter);
        recommendRv.setNestedScrollingEnabled(false);

        View dramaView = LayoutInflater.from(this).inflate(R.layout.dialog_drama, null);
        expandListRv = dramaView.findViewById(R.id.drama_list);

        expandListAdapter = new DramaAdapter(this, new ArrayList<>());
        expandListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            expandListBSD.dismiss();
            playVideo(expandListAdapter, position);
        });
        expandListRv.setAdapter(expandListAdapter);
        expandListBSD = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        expandListBSD.setContentView(dramaView);

        View downloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_drama, null);
        ExpandableTextView expandableTextView = downloadView.findViewById(R.id.info);
        expandableTextView.setContent(String.format(getString(R.string.downloadInfoContent), SharedPreferencesUtils.getDataName()));
        expandableTextView.setNeedExpend(true);
        downloadListRv = downloadView.findViewById(R.id.download_list);

        downloadAdapter = new DownloadDramaAdapter(this, new ArrayList<>());
        downloadAdapter.setOnItemClickListener((adapter, view, position) -> {
            DownloadDramaBean bean = downloadBean.get(position);
            if (bean.isHasDownload()) {
                application.showToastMsg(getString(R.string.hasDownloadTask));
                return;
            }
            // 下载开始
            alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
            createDownloadConfig();
            downloadDramaUrl = downloadBean.get(position).getUrl();
            downloadDramaNumber = downloadBean.get(position).getTitle();
            downloadVideoPresenter  = new DownloadVideoPresenter(downloadDramaUrl, downloadDramaNumber, this);
            downloadVideoPresenter.loadData(false);
        });
        downloadListRv.setAdapter(downloadAdapter);
        downloadBottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        downloadBottomSheetDialog.setContentView(downloadView);
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        return linearLayoutManager;
    }

    private void chipClick(View view, int position) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        Bundle bundle = new Bundle();
        bundle.putString("title", detailsDataBean.getTagTitles().get(position));
        bundle.putString("url", detailsDataBean.getTagUrls().get(position));
        startActivity(new Intent(this, VodListActivity.class).putExtras(bundle));
    }

    @SuppressLint("RestrictedApi")
    public void openVodDesc() {
        hideView(chipGroupView);
        chipGroupView.removeAllViews();
        hideView(scoreView);
        setTextviewEmpty(expandableDescView);
        detailsDataBean = new DetailsDataBean();
        mPresenter = new DetailsPresenter(detailsUrl, this);
        mPresenter.loadData(true);
    }

    private void setTextviewEmpty(AppCompatTextView appCompatTextView) {
        appCompatTextView.setText("");
    }

    private void playVideo(DramaAdapter adapter, int position) {
        alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
        adapter.getData().get(position).setSelected(true);
        clickIndex = position;
        dramaList.get(position).setSelected(true);
        dramaUrl = dramaList.get(position).getUrl();
        dramaTitle = dramaList.get(position).getTitle();
        dramaListAdapter.notifyDataSetChanged();
        expandListAdapter.notifyDataSetChanged();
        videoPresenter = new VideoPresenter(false, detailsTitle, dramaUrl, sourceIndex, dramaTitle, this);
        videoPresenter.loadData(true);
    }

    @Override
    public void onBackPressed() {
        if (vodUrlList.size() == 1)
            super.onBackPressed();
        else {
            if (!mSwipe.isRefreshing()) {
                vodUrlList.remove(vodUrlList.size() - 1);
                detailsUrl = vodUrlList.get(vodUrlList.size() - 1);
                openVodDesc();
            }
            else
                application.showToastMsg(getString(R.string.loadVodDescInfo));
        }
    }

    private void favoriteVod() {
        isFavorite = TFavoriteManager.favorite(detailsDataBean.getUrl(), detailsDataBean.getImg(), detailsDataBean.getIntroduction(), vodId);
        favoriteBtn.setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.round_bookmark_added_24 : R.drawable.round_bookmark_add_24));
        favoriteBtn.setText(isFavorite ? getString(R.string.removeFavoriteBtnText) : getString(R.string.addFavoriteBtnText));
        favoriteBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        EventBus.getDefault().post(new RefreshEvent(1));
        application.showImgSnackbarMsg(favoriteBtn, isFavorite ? R.drawable.round_favorite_24 : R.drawable.round_favorite_border_24, getColor(R.color.night_text_color), isFavorite ? getString(R.string.addFavorite) : getString(R.string.removeFavorite));
    }

    private void setCollapsingToolbar() {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .error(R.drawable.default_bg);
        if (Utils.isNullOrEmpty(detailsDataBean.getImg()))
            bgView.setImageDrawable(getDrawable(R.drawable.default_bg));
        else
            GlideApp.with(this)
                    .load(Utils.getGlideUrl(detailsDataBean.getImg()))
                    .override(500)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .apply(options)
                    .apply(RequestOptions.bitmapTransform( new BlurTransformation(15, 5)))
                    .into(bgView);
        vodImgView.setTag(R.id.imageid, detailsDataBean.getImg());
        Utils.setDefaultImage(detailsDataBean.getImg(), detailsDataBean.getUrl(), vodImgView, false, null, null);
        titleView.setText(detailsDataBean.getTitle());
        if (detailsDataBean.getTagTitles() != null) {
            for (int i=0,size=detailsDataBean.getTagTitles().size(); i<size; i++) {
                Chip chip = new Chip(this);
                chip.setText(detailsDataBean.getTagTitles().get(i));
                int position = i;
                chip.setOnClickListener(view -> chipClick(view, position));
                chipGroupView.addView(chip);
            }
            showView(chipGroupView);
        }else
            hideView(chipGroupView);
        if (Utils.isNullOrEmpty(detailsDataBean.getIntroduction()))
            hideView(expandableDescView);
        else {
            expandableDescView.setContent(detailsDataBean.getIntroduction());
            showView(expandableDescView);
        }
        setTextView(infoView, detailsDataBean.getInfo());
        setTextView(updateTimeView, detailsDataBean.getUpdateTime());
        setTextView(scoreView, detailsDataBean.getScore());
    }

    private void setTextView(TextView textView, String content) {
        if (Utils.isNullOrEmpty(content))
            textView.setVisibility(View.GONE);
        else {
            textView.setText("•"+content);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void hideView(View view) {
        Utils.fadeOut(view);
        view.setVisibility(View.GONE);
    }

    private void showView(View view) {
        Utils.fadeIn(view);
        view.setVisibility(View.VISIBLE);
    }

    private void initTitleAdapter() {
        selectedDrama.setText(dramaTitles.get(0));
        dramaTitlesApter = new ArrayAdapter(this, R.layout.text_list_item, dramaTitles);
        selectedDrama.setAdapter(dramaTitlesApter);
        selectedDrama.setOnItemClickListener((parent, view, position, id) -> setAdapterData(position));
    }

    private void setAdapterData(int position) {
        sourceIndex = position;
        dramaList = detailsDataBean.getDramasList().get(position).getDramasItemList();
        /*for (DetailsDataBean.DramasItem item : dramaList) {
            System.out.println(item.isSelected() + "");
        }*/
        dramaListAdapter.setNewInstance(dramaList);
        downloadBean = new ArrayList<>();
        for (DetailsDataBean.DramasItem b : dramaList) {
            DownloadDramaBean downloadDramaBean = new DownloadDramaBean();
            downloadDramaBean.setTitle(b.getTitle());
            downloadDramaBean.setUrl(b.getUrl());
            downloadBean.add(downloadDramaBean);
        }
        setVodDramaAdapter();
    }

    private void setVodDramaAdapter() {
        showView(openDramaView);
        expandListAdapter.setNewInstance(dramaList);
        downloadAdapter.setNewInstance(downloadBean);
    }

    public void cancelDialog() {
        if (isFinishing()) return;
        Utils.cancelDialog(alertDialog);
    }

    @Override
    public void successPlayUrl(List<String> urls) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
           cancelDialog();
            if (urls.size() == 1)
                playVod(urls.get(0));
            else
                VideoUtils.showMultipleVideoSources(this,
                        urls,
                        (dialog, index) -> playVod(urls.get(index)),
                        false);
        });
    }

    /**
     * 播放视频
     * @param playUrl
     */
    private void playVod(String playUrl) {
        cancelDialog();
        switch (SharedPreferencesUtils.getUserSetOpenVidePlayer()) {
            case 0:
                //调用播放器
                TFavoriteManager.updateFavorite(dramaUrl, dramaTitle, vodId);
                TVideoManager.addVideoHistory(vodId, dramaUrl, sourceIndex, dramaTitle);
                VideoUtils.openPlayer(true, this, dramaTitle, playUrl, detailsTitle, dramaUrl, dramaList, clickIndex, vodId, sourceIndex);
                break;
            case 1:
                Utils.selectVideoPlayer(this, playUrl);
                break;
        }
    }

    @Override
    public void errorPlayUrl() {
        if (isFinishing()) return;
        //解析出错
        runOnUiThread(() -> {
            if (SharedPreferencesUtils.getEnableSniff()) {
                alertDialog = Utils.getProDialog(this, R.string.sniffVodPlayUrl);
                VideoUtils.startSniffing(dramaUrl, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.PLAY);
            } else
                Utils.showAlert(this,
                        getString(R.string.errorDialogTitle),
                        getString(R.string.parseVodPlayUrlError),
                        false,
                        getString(R.string.defaultPositiveBtnText),
                        "",
                        "",
                        (dialog, which) -> dialog.dismiss(),
                        null,
                        null);
        });
    }

    @Override
    public void errorNet(String msg) {
        if (isFinishing()) return;
        //网络出错
        runOnUiThread(() -> Utils.showAlert(this,
                getString(R.string.errorDialogTitle),
                msg,
                false,
                getString(R.string.defaultPositiveBtnText),
                "",
                "",
                (dialog, which) -> dialog.dismiss(),
                null,
                null));
    }

    @Override
    public void successDramasList(List<DetailsDataBean.DramasItem> dramasItems) {

    }

    @Override
    public void errorDramasList() {

    }

    @Override
    public void successOnlyPlayUrl(List<String> urls) {

    }

    @Override
    public void errorOnlyPlayUrl() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != downloadVideoPresenter)
            downloadVideoPresenter.detachView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DramaEvent event) {
        if (isFinishing()) return;
        clickIndex = event.getClickIndex();
        dramaList.get(clickIndex).setSelected(true);
        dramaListAdapter.notifyDataSetChanged();
        expandListAdapter.notifyDataSetChanged();
    }

    /****************************************** 下载相关 ******************************************/
    /**
     * 创建下载保存目录
     */
    private void createDownloadConfig() {
        savePath = String.format(DOWNLOAD_SAVE_PATH, ParserInterfaceFactory.getParserInterface().getSourceName() , detailsTitle);
        if (SAFUtils.canReadDownloadDirectory())
            Utils.createDataFolder(savePath);
        else {
            savePath = getFilesDir().getAbsolutePath()+String.format("/%s/%s/", ParserInterfaceFactory.getParserInterface().getSourceName() , detailsTitle);
            Utils.createDataFolder(savePath);
        }
    }

    /**
     * 开始执行下载操作
     * @param url
     * @param playNumber
     */
    private void startDownload(String url, String playNumber) {
        if (!url.contains("http")) {
            VideoUtils.showInfoDialog(this, String.format(getString(R.string.notSupportDownloadMsg), url));
            return;
        }
        long taskId;
        String fileSavePath = savePath + playNumber;
        boolean isM3U8 = url.endsWith("m3u8");
        taskId = createDownloadTask(isM3U8, url, fileSavePath);
        if (isM3U8) VideoUtils.showInfoDialog(this, getString(R.string.downloadM3u8Tips));
        TDownloadManager.insertDownload(detailsTitle,  detailsDataBean.getImg(), detailsUrl);
        TDownloadDataManager.insertDownloadData(detailsTitle, playNumber, 0, taskId);
        application.showToastMsg(String.format(getString(R.string.downloadStart), playNumber));
        // 开启下载服务
        startService(new Intent(this, DownloadService.class));
        EventBus.getDefault().post(new RefreshEvent(3));
        checkHasDownload();
    }

    /**
     * 创建下载任务
     * @param isM3u8
     * @param url
     * @param savePath
     * @return
     */
    private long createDownloadTask(boolean isM3u8, String url, String savePath) {
        url = url.replaceAll("\\\\", "");
        if (isM3u8)
            return Aria.download(this)
                    .load(url)
                    .setFilePath(savePath + ".m3u8")
                    .ignoreFilePathOccupy()
                    .m3u8VodOption(new M3U8DownloadConfig().setM3U8Option())
                    .ignoreCheckPermissions()
                    .create();
        else
            return Aria.download(this)
                    .load(url)
                    .setFilePath(savePath + ".mp4")
                    .ignoreFilePathOccupy()
                    .ignoreCheckPermissions()
                    .create();
    }

    /**
     * 检查是否在下载任务中
     */
    private void checkHasDownload() {
        List<DownloadDramaBean> data = downloadAdapter.getData();
        for (int i=0,size=data.size(); i<size; i++) {
            int complete = TDownloadManager.queryDownloadDataIsDownloadError(vodId, data.get(i).getTitle(), 0);
            switch (complete) {
                case -1:
                case 2:
                    data.get(i).setHasDownload(false);
                    break;
                default:
                    data.get(i).setHasDownload(true);
                    break;
            }
        }
        downloadAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecyclerViewView();
    }

    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    @Override
    protected void retryListener() {
        sourceIndex = 0;
        mPresenter.loadData(true);
    }

    private void setRecyclerViewView() {
        expandListRv.setLayoutManager(new GridLayoutManager(this, parserInterface.setDetailExpandListItemSize(Utils.isPad())));
        downloadListRv.setLayoutManager(new GridLayoutManager(this, parserInterface.setDetailExpandListItemSize(Utils.isPad())));
    }

    @Override
    public void loadingView() {
        if (isFinishing()) return;
        GlideApp.with(this).load(R.drawable.default_bg).into(bgView);
        emptyView();
        dramaListRv.scrollToPosition(0);
        multiListRv.scrollToPosition(0);
        recommendRv.scrollToPosition(0);
    }

    @Override
    public void errorView(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            mSwipe.setRefreshing(false);
            hideView(descView);
            hideView(playLinearLayout);
            hideView(multiLinearLayout);
            hideView(recommendLinearLayout);
            errorMsgView.setText(msg);
            showView(errorBgView);
        });
    }

    @Override
    public void emptyView() {
        if (isFinishing()) return;
        mSwipe.setRefreshing(true);
        hideView(descView);
        hideView(playLinearLayout);
        hideView(multiLinearLayout);
        hideView(recommendLinearLayout);
        hideView(errorBgView);
    }

    @Override
    public void success(DetailsDataBean detailsDataBean) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            this.detailsDataBean = detailsDataBean;
            detailsTitle = detailsDataBean.getTitle();
            setCollapsingToolbar();
            mSwipe.setRefreshing(false);
            if (isFavorite)
                TFavoriteManager.updateFavorite(detailsDataBean.getUrl(), detailsDataBean.getImg(), detailsDataBean.getIntroduction(), vodId);
            showView(descView);
            if (detailsDataBean.getDramasList().size() > 0) {
                downloadBean = new ArrayList<>();
                // 默认展示第一播放源
                dramaList = detailsDataBean.getDramasList().get(0).getDramasItemList();
                dramaTitles = new ArrayList<>();
                for (DetailsDataBean.Dramas dramas : detailsDataBean.getDramasList()) {
                    dramaTitles.add(String.format(getString(R.string.episodeSize), dramas.getListTitle(), dramas.getDramasItemList().size()));
                }
                initTitleAdapter();
                setAdapterData(0);
                showView(playLinearLayout);
            } else {
                errorMsgView.setText(getString(R.string.noEpisode));
                showView(errorBgView);
            }
            if (detailsDataBean.getMultiList().size() > 0) {
                multiAdapter.setNewInstance(detailsDataBean.getMultiList());
                showView(multiLinearLayout);
            }
            else
                hideView(multiLinearLayout);
            if (detailsDataBean.getRecommendList().size() > 0) {
                recommendAdapter.setNewInstance(detailsDataBean.getRecommendList());
                showView(recommendLinearLayout);
            }
            else
                hideView(recommendLinearLayout);
        });
    }

    @Override
    public void favorite(boolean favorite) {
        if (isFinishing()) return;
        isFavorite = favorite;
        runOnUiThread(() -> {
            favoriteBtn.setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.round_bookmark_added_24 : R.drawable.round_bookmark_add_24));
            favoriteBtn.setText(isFavorite ? getString(R.string.removeFavoriteBtnText) : getString(R.string.addFavoriteBtnText));
        });
    }

    @Override
    public void getVodId(String id) {
        vodId = id;
    }

    @Override
    public void downloadVodUrlSuccess(List<String> urls, String playNumber) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            cancelDialog();
            if (urls.size() > 1) {
                VideoUtils.showMultipleVideoSources4Download(this,
                        urls,
                        (dialog, index) -> {
                            startDownload(urls.get(index), playNumber);
                            dialog.dismiss();
                        }
                );
            } else
                startDownload(urls.get(0), playNumber);
        });
    }

    @Override
    public void downloadVodUrlError(String playNumber) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            String msg = detailsTitle + playNumber;
            cancelDialog();
            if (SharedPreferencesUtils.getEnableSniff()) {
                alertDialog = Utils.getProDialog(this, R.string.sniffVodPlayUrl);
                VideoUtils.startSniffing(downloadDramaUrl, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.DOWNLOAD);
            } else {
                Utils.showAlert(this,
                        getString(R.string.errorDialogTitle),
                        String.format(getString(R.string.playUrlParserError), msg),
                        false,
                        getString(R.string.defaultPositiveBtnText),
                        "",
                        "",
                        (dialog, which) -> dialog.dismiss(),
                        null,
                        null);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSniff(VideoSniffEvent event) {
        if (isFinishing()) return;
        if (event.getActivityEnum() == VideoSniffEvent.ActivityEnum.DETAIL) {
            cancelDialog();
            List<String> urls = event.getUrls();
            if (event.isSuccess()) {
                switch (event.getSniffEnum()) {
                    case PLAY:
                        successPlayUrl(urls);
                        break;
                    case DOWNLOAD:
                        downloadVodUrlSuccess(urls, downloadDramaNumber);
                        break;
                }
            } else
                VideoUtils.sniffErrorDialog(this);
        }
    }
}
