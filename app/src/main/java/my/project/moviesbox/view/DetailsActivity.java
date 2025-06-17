package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOWNLOAD;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_FAVORITE;
import static my.project.moviesbox.utils.Utils.DOWNLOAD_SAVE_PATH;
import static my.project.moviesbox.utils.Utils.isPad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.HttpOption;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DetailTagAdapter;
import my.project.moviesbox.adapter.DetailsExpandListItemAdapter;
import my.project.moviesbox.adapter.DetailsListItemAdapter;
import my.project.moviesbox.adapter.DownloadDramaAdapter;
import my.project.moviesbox.adapter.DramaAdapter;
import my.project.moviesbox.bean.DownloadDramaBean;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.config.M3U8DownloadConfig;
import my.project.moviesbox.contract.DetailsContract;
import my.project.moviesbox.contract.DownloadVideoContract;
import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.custom.AutoLineFeedLayoutManager;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.entity.TDownload;
import my.project.moviesbox.database.enums.DirectoryTypeEnum;
import my.project.moviesbox.database.manager.TDirectoryManager;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.event.DramaEvent;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.model.DetailsModel;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.config.ItemStyleEnum;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.presenter.DetailsPresenter;
import my.project.moviesbox.presenter.DownloadVideoPresenter;
import my.project.moviesbox.presenter.VideoPresenter;
import my.project.moviesbox.service.DownloadService;
import my.project.moviesbox.utils.ImageUtils;
import my.project.moviesbox.utils.KeyDownloader;
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
public class DetailsActivity extends BaseActivity<DetailsModel, DetailsContract.View, DetailsPresenter> implements DetailsContract.View,
        VideoContract.View, DownloadVideoContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    /**
     * 收藏按钮视图
     */
    @BindView(R.id.favorite)
    MaterialButton favoriteBtn;
    /**
     * 平板图片视图
     */
    @BindView(R.id.pad_img_box)
    public ConstraintLayout padImgBoxView;
    /**
     * 影视图片视图 (1:1.4)
     */
    @BindView(R.id.img_view_type_0)
    public MaterialCardView imgType0View;
    /**
     * 影视图片视图 (1:1.4)
     */
    @BindView(R.id.vod_img_type0)
    public ImageView vodImgType0View;
    /**
     * 影视图片视图 (16:9)
     */
    @BindView(R.id.img_view_type_1)
    public MaterialCardView imgType1View;
    /**
     * 影视图片视图 (16:9)
     */
    @BindView(R.id.vod_img_type1)
    public ImageView vodImgType1View;
    /**
     * 影视详情视图
     */
    @BindView(R.id.desc)
    ExpandableTextView expandableDescView;
    /**
     * 影视标题视图
     */
    @BindView(R.id.title)
    public TextView titleView;
    /**
     * 下拉刷新视图
     */
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    /**
     * <p>detailsTitle 详情标题</p>
     * <p>detailsUrl 详情源地址</p>
     * <p>dramaUrl 播放页源地址</p>
     * <p>dramaTitle 播放集数标题</p>
     * <p>downloadDramaUrl 下载剧集集数地址</p>
     * <p>dramaTitle 下载剧集集数标题</p>
     */
    protected String detailsTitle, detailsUrl, dramaUrl, dramaTitle, downloadDramaUrl, downloadDramaNumber;
    /**
     * 是否收藏
     */
    private boolean isFavorite;
    /**
     * 影视详情信息
     */
    protected DetailsDataBean detailsDataBean = new DetailsDataBean();
    /**
     * 记住点击过的影视 用于手势返回时回到上一影视信息
     */
    private List<String> vodUrlList = new ArrayList<>();
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
    protected DetailsListItemAdapter multiAdapter;
    @BindView(R.id.multi_layout)
    LinearLayout multiLinearLayout;
    /**
     * 相关推荐相关
     */
    @BindView(R.id.recommend_list)
    RecyclerView recommendRv;
    private LinearLayoutManager recommendLayoutManager;
    protected DetailsListItemAdapter recommendAdapter;
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
    public ImageView bgView;
    /**
     * TAG列表
     */
    @BindView(R.id.chip_group)
    RecyclerView detailTagRecyclerView;
    private RecyclerView expandTagListRv;
    private BottomSheetDialog tagBottomSheetDialog;
    List<ClassificationDataBean.Item> detailTags = new ArrayList<>();
    DetailTagAdapter detailTagAdapter;

    @BindView(R.id.info)
    public TextView infoView;
    /**
     * 更新时间视图
     */
    @BindView(R.id.update_time)
    public TextView updateTimeView;
    /**
     * 评分视图
     */
    @BindView(R.id.score_view)
    public AppCompatTextView scoreView;
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
    private final DownloadVideoPresenter downloadVideoPresenter = new DownloadVideoPresenter(this); // 下载适配器
    private RecyclerView downloadListRv;
    private BottomSheetDialog downloadBottomSheetDialog;
    private List<DownloadDramaBean> downloadBean = new ArrayList<>();
    private DownloadDramaAdapter downloadAdapter;
    /**
     * 多播放列表选择相关
     */
    @BindView(R.id.multi_play_layout)
    RelativeLayout multiPlayLayout;
    @BindView(R.id.multi_play_input)
    TextInputLayout multiPlayInput;
    @BindView(R.id.selected_text)
    AutoCompleteTextView selectedDrama;
    private List<String> dramaTitles;
    private ArrayAdapter dramaTitlesApter;
    private int sourceIndex = 0; // 所选播放列表下标
    private final VideoPresenter videoPresenter = new VideoPresenter(this);
    private RecyclerView multiRecommendRv;
    private BottomSheetDialog multiRecommendDialog;
    private View multiRecommendView;
    private List<MultiItemEntity> detailsExpandData = new ArrayList<>();
    private DetailsExpandListItemAdapter detailsExpandListItemAdapter;
    private Button selectDirectoryView; // 保存清单按钮
    private String downloaDdirectoryId; // 下载保存清单ID
    @BindView(R.id.detailFab)
    ExtendedFloatingActionButton detailFab;
    private int fabHeight = 0;

    @Override
    protected DetailsPresenter createPresenter() {
        return new DetailsPresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true, detailsUrl);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_details;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0);
        getBundle();
        initToolbar();
        initSwipe();
        initFab();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!Utils.isNullOrEmpty(bundle)) {
            detailsUrl = bundle.getString("url");
            detailsTitle = bundle.getString("title");
            vodUrlList.add(detailsUrl);
        }
    }

    private void initToolbar() {
       setToolbar(toolbar, "", "");
       scrollView.post(() -> scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView != null) {
                boolean scrollGtZero = scrollView.getScrollY() > 0;
                toolbar.setTitle(scrollGtZero ? detailsTitle : "");
                mSwipe.setEnabled(!scrollGtZero);
            }
        }));
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                detailFab.shrink();
            } else {
                detailFab.extend();
            }
        });
    }

    @OnClick({R.id.order, R.id.favorite, R.id.download, R.id.browser,R.id.drama})
    public void onClick(View view) {
        Utils.setVibration(view);
        switch (view.getId()) {
            case R.id.favorite:
                if (!isFavorite)
                    // 去选择清单进行收藏
                    startActivityForResult(new Intent(this, DirectoryActivity.class).putExtra("type", DirectoryTypeEnum.FAVORITE.getName()), DIRECTORY_REQUEST_CODE);
                else
                    favoriteVod("");
                break;
            case R.id.download:
                select2Download();
                break;
            case R.id.browser:
                Utils.viewInChrome(DetailsActivity.this, detailsUrl);
                break;
            case R.id.order:
                Collections.reverse(dramaList);
                dramaListAdapter.notifyDataSetChanged();
                expandListAdapter.notifyDataSetChanged();
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
        if (downloadAdapter.getData().size() == 0) {
            application.showToastMsg("无可下载列表", DialogXTipEnum.ERROR);
            return;
        }
        if (!Utils.isWifiConnected()) {
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
        mSwipe.setOnRefreshListener(this::retryListener);
    }

    private void initFab() {
        if (Utils.checkHasNavigationBar(this))
        {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) detailFab.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 1);
            detailFab.setLayoutParams(params);
        }
    }

    private void initAdapter() {
        detailTagAdapter = new DetailTagAdapter(detailTags);
        detailTagAdapter.setOnItemClickListener((adapter, view, position) -> {
            chipClick(view, position);
        });
        View tagView = LayoutInflater.from(this).inflate(R.layout.dialog_drama, null);
        expandTagListRv = tagView.findViewById(R.id.drama_list);
        expandTagListRv.setAdapter(detailTagAdapter);
        tagBottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        tagBottomSheetDialog.setContentView(tagView);
        expandTagListRv.setLayoutManager(new AutoLineFeedLayoutManager());
        detailTagAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            tagBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            tagBottomSheetDialog.show();
            return true;
        });
//        if (Utils.isPad()) {
//            detailTagRecyclerView.setLayoutManager(new AutoLineFeedLayoutManager());
//        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            detailTagRecyclerView.setLayoutManager(layoutManager);
//        }
        detailTagRecyclerView.setAdapter(detailTagAdapter);
        dramaListAdapter = new DramaAdapter(this, dramaList);
        dramaListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            playVideo(dramaListAdapter, position);
        });
        dramaListAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            expandListBSD.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            expandListBSD.show();
            return true;
        });
        dramaListLayoutManager = getLinearLayoutManager();
        dramaListRv.setLayoutManager(dramaListLayoutManager);
        dramaListRv.setAdapter(dramaListAdapter);
        dramaListRv.setNestedScrollingEnabled(false);

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
        selectDirectoryView = downloadView.findViewById(R.id.selectDirectory);
        selectDirectoryView.setOnClickListener(v -> {
            // 去选择清单进行收藏
            startActivityForResult(new Intent(this, DirectoryActivity.class).putExtra("type", DirectoryTypeEnum.DOWNLOAD.getName()), DIRECTORY_REQUEST_CODE);
        });
        setSelectDirectoryData();
        downloadListRv = downloadView.findViewById(R.id.download_list);

        downloadAdapter = new DownloadDramaAdapter(this, new ArrayList<>());
        downloadAdapter.setOnItemClickListener((adapter, view, position) -> {
            DownloadDramaBean bean = downloadBean.get(position);
            if (bean.isHasDownload()) {
                application.showToastMsg(getString(R.string.hasDownloadTask), DialogXTipEnum.WARNING);
                return;
            }
            downloadDramaUrl = downloadBean.get(position).getUrl();
            downloadDramaNumber = downloadBean.get(position).getTitle();
            createDownloadConfig();
            // 下载开始
            if (!parserInterface.playUrlNeedParser()) {
                startDownload(downloadDramaUrl, downloadDramaNumber);
                return;
            }
            alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
            downloadVideoPresenter.loadData(downloadDramaUrl, downloadDramaNumber);
        });
        downloadListRv.setAdapter(downloadAdapter);
        downloadBottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        downloadBottomSheetDialog.setContentView(downloadView);

        multiRecommendDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        multiRecommendView = LayoutInflater.from(this).inflate(R.layout.dialog_multi_recommend, null);
        multiRecommendRv = multiRecommendView.findViewById(R.id.drama_list);
        detailsExpandListItemAdapter = new DetailsExpandListItemAdapter(new ArrayList<>());
        detailsExpandListItemAdapter.setOnItemClickListener((adapter, view, position) -> getDescData((DetailsDataBean.Recommend) adapter.getItem(position)));
        multiRecommendRv.setLayoutManager(new LinearLayoutManager(this));
        multiRecommendRv.setAdapter(detailsExpandListItemAdapter);
        multiRecommendDialog.setContentView(multiRecommendView);
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        return linearLayoutManager;
    }

    private void chipClick(View view, int position) {
        if (tagBottomSheetDialog.isShowing())
            tagBottomSheetDialog.dismiss();
        Utils.setVibration(view);
        Bundle bundle = new Bundle();
        bundle.putString("title", detailsDataBean.getTagTitles().get(position));
        bundle.putString("url", detailsDataBean.getTagUrls().get(position));
        startActivity(new Intent(this, parserInterface.detailTagOpenClass()).putExtras(bundle));
    }

    @OnClick(R.id.detailFab)
    public void onDetailFabClick() {
        Utils.setVibration(detailFab);
        DetailsDataBean.DetailsFabBren fabBren = detailsDataBean.getDetailsFabBren();
        if (fabBren != null) {
            if (fabBren.getOpenClass().equals(DetailsActivity.class)) {
                detailsTitle = fabBren.getTitle();
                detailsUrl = fabBren.getUrl();
                vodUrlList.add(detailsUrl);
                openVodDesc();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public void openVodDesc() {
        multiAdapter = null;
        recommendAdapter = null;
        detailTags.clear();
        detailTagAdapter.setNewInstance(detailTags);
        dramaList.clear();
        dramaListAdapter.setNewInstance(dramaList);
        expandListAdapter.setNewInstance(dramaList);
        /*hideView(chipGroupView);
        chipGroupView.removeAllViews();*/
        setTextviewEmpty(expandableDescView);
        detailFab.setVisibility(View.GONE);
        detailsDataBean = new DetailsDataBean();
        retryListener();
    }

    private void setTextviewEmpty(AppCompatTextView appCompatTextView) {
        appCompatTextView.setText("");
    }

    private void playVideo(DramaAdapter adapter, int position) {
        adapter.getData().get(position).setSelected(true);
        clickIndex = position;
        dramaList.get(position).setSelected(true);
        dramaUrl = dramaList.get(position).getUrl();
        dramaTitle = dramaList.get(position).getTitle();
        dramaListAdapter.notifyItemChanged(position);
        expandListAdapter.notifyItemChanged(position);
        if (!parserInterface.playUrlNeedParser()) {
            playVod(dramaUrl);
            return;
        }
        alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
        videoPresenter.loadData(false, detailsTitle, dramaUrl, sourceIndex, dramaTitle);
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
                application.showToastMsg(getString(R.string.loadVodDescInfo), DialogXTipEnum.WARNING);
        }
    }

    private void favoriteVod(String directoryId) {
        isFavorite = TFavoriteManager.favorite(detailsDataBean.getUrl(), detailsDataBean.getImg(), detailsDataBean.getIntroduction(), vodId, directoryId);
        favoriteBtn.setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.round_bookmark_added_24 : R.drawable.round_bookmark_add_24));
        favoriteBtn.setText(isFavorite ? getString(R.string.removeFavoriteBtnText) : getString(R.string.addFavoriteBtnText));
        EventBus.getDefault().post(REFRESH_FAVORITE);
//        application.showImgSnackbarMsg(favoriteBtn, isFavorite ? R.drawable.round_favorite_24 : R.drawable.round_favorite_border_24, getColor(R.color.night_text_color), isFavorite ? getString(R.string.addFavorite) : getString(R.string.removeFavorite));
        application.showToastMsg(isFavorite ? getString(R.string.addFavorite) : getString(R.string.removeFavorite), DialogXTipEnum.SUCCESS);
    }

    protected void setCollapsingToolbar() {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565);
        if (Utils.isNullOrEmpty(detailsDataBean.getImg())) {
            bgView.setVisibility(View.GONE);
        } else {
            // 获取屏幕高度
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;

            // 计算高度的二分之一
            int height = screenHeight / 2;

            // 设置ImageView的高度
            ViewGroup.LayoutParams params = bgView.getLayoutParams();
            params.height = height;
            bgView.setLayoutParams(params);
            // 设置图片信息
            if (isPad()) {
                // 如果是平板 设置背景模糊
                GlideApp.with(this)
                        .load(Utils.getGlideUrl(detailsDataBean.getImg()))
                        .override(500)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .apply(options)
                        .error(getDrawable(R.drawable.default_bg))
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 5)))
                        .into(bgView);
                bgView.setVisibility(View.VISIBLE);
                GlideApp.with(this)
                        .asBitmap()
                        .load(Utils.getGlideUrl(detailsDataBean.getImg()))
                        .override(500)
                        .apply(new RequestOptions()
                                .encodeQuality(70)
                        )
                        .transition(BitmapTransitionOptions.withCrossFade(500))
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // 获取图片宽高
                                int width = resource.getWidth();
                                int height = resource.getHeight();
                                // 回调结果
                                if (width > height) {
                                    vodImgType1View.setTag(R.id.imageid, detailsDataBean.getImg());
                                    vodImgType1View.setImageBitmap(resource);
                                    imgType1View.setVisibility(View.VISIBLE);
                                } else {
                                    vodImgType0View.setTag(R.id.imageid, detailsDataBean.getImg());
                                    vodImgType0View.setImageBitmap(resource);
                                    imgType0View.setVisibility(View.VISIBLE);
                                }
                                // 显示图片
                                padImgBoxView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                padImgBoxView.setVisibility(View.GONE);
                            }
                        });
                /*if (detailsDataBean.getImgStyle().equals(ItemStyleEnum.STYLE_16_9)) {
                    // 如果是16:9
                    vodImgType1View.setTag(R.id.imageid, videoImgUrl);
                    Utils.setDefaultImage(videoImgUrl, detailsDataBean.getUrl(), vodImgType1View, false, null, null);
                    imgType1View.setVisibility(View.VISIBLE);
                } else {
                    // 1:1.4
                    vodImgType0View.setTag(R.id.imageid, videoImgUrl);
                    Utils.setDefaultImage(videoImgUrl, detailsDataBean.getUrl(), vodImgType0View, false, null, null);
                    imgType0View.setVisibility(View.VISIBLE);
                }
                // 显示图片
                imgBoxView.setVisibility(View.VISIBLE);*/
            } else {
                // 手机设备显示原图
                GlideApp.with(this)
                        .load(Utils.getGlideUrl(detailsDataBean.getImg()))
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .apply(options)
                        .error(getDrawable(R.drawable.default_bg))
                        .into(bgView);
                bgView.setVisibility(View.VISIBLE);
                // 不显示图片
                padImgBoxView.setVisibility(View.GONE);
                // 将标题完全显示 调整字体大小
                titleView.setTextSize(24);
                titleView.setMaxLines(99);
                infoView.setTextSize(18);
                scoreView.setTextSize(18);
                updateTimeView.setTextSize(18);
            }
        }
    }

    private void setContent() {
        titleView.setText(detailsDataBean.getTitle());
        detailTags = new ArrayList<>();
        if (detailsDataBean.getTagTitles() != null) {
            for (int i=0,size=detailsDataBean.getTagTitles().size(); i<size; i++) {
                detailTags.add(new ClassificationDataBean.Item(detailsDataBean.getTagTitles().get(i), detailsDataBean.getTagUrls().get(i)));
            }
            detailTagAdapter.setNewInstance(detailTags);
        }
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
            textView.setText("\uD83D\uDD38 "+content);
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
        if (dramaTitles.size() > 1)
            multiPlayInput.setHint("播放列表 [共"+dramaTitles.size()+"个播放列表]");
        else
            multiPlayInput.setHint("播放列表");
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
        alertDialog = null;
    }

    @Override
    public void successPlayUrl(List<DialogItemBean> urls) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
           cancelDialog();
            if (urls.size() == 1)
                playVod(urls.get(0).getUrl());
            else
               alertDialog = VideoUtils.showMultipleVideoSources(this,
                        urls,
                        (adapter, view, position) -> {
                            playVod(urls.get(position).getUrl());
                            Utils.cancelDialog(alertDialog);
                        },
                        false);
        });
    }

    /**
     * 播放视频
     * @param playUrl
     */
    private void playVod(String playUrl) {
        LogUtil.logInfo("播放视频", playUrl);
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
                VideoUtils.startSniffing(this, dramaUrl, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.PLAY);
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
    public void successOnlyPlayUrl(List<DialogItemBean> urls) {

    }

    @Override
    public void errorOnlyPlayUrl() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != downloadVideoPresenter)
            downloadVideoPresenter.registerEventBus();
        if (null != videoPresenter)
            videoPresenter.registerEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != downloadVideoPresenter)
            downloadVideoPresenter.unregisterEventBus();
        if (null != videoPresenter)
            videoPresenter.unregisterEventBus();
    }

    @Override
    protected void onDestroy() {
        if (null != downloadVideoPresenter)
            downloadVideoPresenter.detachView();
        if (null != videoPresenter)
            videoPresenter.detachView();
        emptyRecyclerView(dramaListRv, expandListRv, multiListRv, recommendRv, detailTagRecyclerView, expandTagListRv, downloadListRv);
        EventBus.getDefault().unregister(this);
        Utils.clearCache();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DramaEvent event) {
        if (isFinishing()) return;
        clickIndex = event.getClickIndex();
        dramaList.get(clickIndex).setSelected(true);
        dramaListAdapter.notifyItemChanged(clickIndex);
        expandListAdapter.notifyItemChanged(clickIndex);
    }

    /****************************************** 下载相关 ******************************************/
    /**
     * 创建下载保存目录
     */
    private void createDownloadConfig() {
        String fileName = Utils.getHashedFileName(detailsTitle);
        savePath = String.format(DOWNLOAD_SAVE_PATH, ParserInterfaceFactory.getParserInterface().getSourceName() , fileName);
        if (SAFUtils.canReadDownloadDirectory())
            Utils.createDataFolder(savePath);
        else {
            savePath = getFilesDir().getAbsolutePath()+String.format(DOWNLOAD_SAVE_PATH, ParserInterfaceFactory.getParserInterface().getSourceName() , fileName);
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
        String fileSavePath = savePath + playNumber;
        String netImg = detailsDataBean.getImg();
        String localImgPath = savePath + "cover_" + new Date().getTime() + ".jpg";
        ImageUtils.saveImageToLocalAsync(netImg, localImgPath, saveSuccess -> {
            String img = saveSuccess ? localImgPath : netImg;
            boolean isM3U8 = url.contains("m3u8");
            long taskId = createDownloadTask(isM3U8, url, fileSavePath);
            if (isM3U8) VideoUtils.showInfoDialog(this, getString(R.string.downloadM3u8Tips));
            TDownloadManager.insertDownload(detailsTitle,  img, detailsUrl, downloaDdirectoryId);
            TDownloadDataManager.insertDownloadData(detailsTitle, playNumber, 0, taskId);
            application.showToastMsg(String.format(getString(R.string.downloadStart), playNumber), DialogXTipEnum.SUCCESS);
            // 开启下载服务
            startService(new Intent(this, DownloadService.class));
            EventBus.getDefault().post(REFRESH_DOWNLOAD);
            checkHasDownload();
        });
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
        HttpOption httpOption = new HttpOption();
        HashMap<String, String> headerMap = parserInterface.setPlayerHeaders();
        if (!Utils.isNullOrEmpty(headerMap))
            httpOption.addHeaders(headerMap);
        long downloadId = isM3u8 ?
                Aria.download(this)
                .load(url)
                .setFilePath(savePath + ".m3u8")
                .ignoreFilePathOccupy()
                .option(httpOption)
                .m3u8VodOption(new M3U8DownloadConfig().setM3U8Option())
                .ignoreCheckPermissions()
                .create() :
                Aria.download(this)
                .load(url)
                .setFilePath(savePath + ".mp4")
                .ignoreFilePathOccupy()
                .option(httpOption)
                .ignoreCheckPermissions()
                .create();
        KeyDownloader.downloadKey(url, savePath);
        return downloadId;
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
            downloadAdapter.notifyItemChanged(i);
        }
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
        mPresenter.loadData(true, detailsUrl);
    }

    private void setRecyclerViewView() {
        expandListRv.setLayoutManager(new GridLayoutManager(this, parserInterface.setDetailExpandListItemSize(Utils.isPad())));
        downloadListRv.setLayoutManager(new GridLayoutManager(this, parserInterface.setDetailExpandListItemSize(Utils.isPad())));
    }

    @Override
    public void loadingView() {
        if (isFinishing()) return;
//        GlideApp.with(this).load(R.drawable.default_bg).into(bgView);
        bgView.setVisibility(View.GONE);
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
            hideView(openDramaView);
            hideView(multiPlayLayout);
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
        detailFab.setVisibility(View.GONE);
        hideView(descView);
        hideView(playLinearLayout);
        hideView(openDramaView);
        hideView(multiPlayLayout);
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
            setContent();
            mSwipe.setRefreshing(false);
            showView(playLinearLayout);
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
                showView(multiPlayLayout);
                showView(openDramaView);
            } else {
                errorMsgView.setText(getString(R.string.noEpisode));
                hideView(openDramaView);
                showView(errorBgView);
            }

            if (detailsDataBean.getMultiList().size() > 0) {
                int layout = 0;
                switch (detailsDataBean.getMultiStyle()) {
                    case STYLE_1_1_DOT_4:
                        layout = R.layout.item_home_data_type_0;
                        break;
                    case STYLE_16_9:
                        layout = R.layout.item_home_data_type_1;
                        break;
                }
                multiAdapter = new DetailsListItemAdapter(layout, detailsDataBean.getMultiList());
                multiAdapter.setOnItemClickListener((adapter, view, position) -> getDescData((DetailsDataBean.Recommend) adapter.getItem(position)));
                multiAdapter.setOnItemLongClickListener((adapter, view, position) -> {
                    setMultiRecommendDialog(detailsDataBean.getMultiStyle(), detailsDataBean.getMultiList());
                    multiRecommendDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                    multiRecommendDialog.show();
                    return true;
                });
                multiLayoutManager = getLinearLayoutManager();
                multiListRv.setLayoutManager(multiLayoutManager);
                multiListRv.setAdapter(multiAdapter);
                multiListRv.setNestedScrollingEnabled(false);
                showView(multiLinearLayout);
            }
            else
                hideView(multiLinearLayout);
            if (detailsDataBean.getRecommendList().size() > 0) {
                int layout = 0;
                switch (detailsDataBean.getRecommendStyle()) {
                    case STYLE_1_1_DOT_4:
                        layout = R.layout.item_home_data_type_0;
                        break;
                    case STYLE_16_9:
                        layout = R.layout.item_home_data_type_1;
                        break;
                }
                recommendAdapter = new DetailsListItemAdapter(layout, detailsDataBean.getRecommendList());
                recommendAdapter.setOnItemClickListener((adapter, view, position) -> getDescData((DetailsDataBean.Recommend) adapter.getItem(position)));
                recommendAdapter.setOnItemLongClickListener((adapter, view, position) -> {
                    setMultiRecommendDialog(detailsDataBean.getRecommendStyle(), detailsDataBean.getRecommendList());
                    multiRecommendDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                    multiRecommendDialog.show();
                    return true;
                });
                recommendLayoutManager = getLinearLayoutManager();
                recommendRv.setLayoutManager(recommendLayoutManager);
                if (Utils.checkHasNavigationBar(this)) recommendRv.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
                recommendRv.setAdapter(recommendAdapter);
                recommendRv.setNestedScrollingEnabled(false);
                showView(recommendLinearLayout);
            }
            else
                hideView(recommendLinearLayout);
            setSelectDirectoryData();
            if (detailsDataBean.isHasDetailFab()) {
                if (fabHeight == 0) {
                    ViewTreeObserver viewTreeObserver = detailFab.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            fabHeight = detailFab.getHeight();
                            scrollView.setPadding(0,0,0, fabHeight+Utils.dpToPx(DetailsActivity.this, 32));
                            detailFab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                }
                showFab();
            }
            lazyLoadImg();
        });
    }

    private void showFab() {
        detailFab.setIcon(getDrawable(detailsDataBean.getDetailsFabBren().getIcon()));
        detailFab.setText(detailsDataBean.getDetailsFabBren().getTitle());
        detailFab.show();
    }

    private void getDescData(DetailsDataBean.Recommend bean) {
        if (multiRecommendDialog.isShowing())
            multiRecommendDialog.dismiss();
        detailsTitle = bean.getTitle();
        detailsUrl = bean.getUrl();
        vodUrlList.add(detailsUrl);
        openVodDesc();
    }

    private void setMultiRecommendDialog(ItemStyleEnum itemStyleEnum, List<DetailsDataBean.Recommend> data) {
        detailsExpandData.clear();
        multiRecommendRv = multiRecommendView.findViewById(R.id.drama_list);
        int spanCount = 0;
        switch (itemStyleEnum) {
            case STYLE_1_1_DOT_4:
                spanCount = parserInterface.setVodListItemSize(Utils.isPad(), isPortrait, true);
                break;
            case STYLE_16_9:
                spanCount = parserInterface.setVodList16_9ItemSize(Utils.isPad(), isPortrait, true);
                break;
        }
        detailsExpandData.addAll(data);
        detailsExpandListItemAdapter.setNewInstance(detailsExpandData);
        multiRecommendRv.setLayoutManager(new GridLayoutManager(this, spanCount));
        multiRecommendRv.setAdapter(detailsExpandListItemAdapter);
        multiRecommendDialog.setContentView(multiRecommendView);
    }

    private void setSelectDirectoryData() {
        TDownload tDownload = TDownloadManager.queryByVideoTitle(detailsTitle);
        String selectDirectoryTitle;
        if (Utils.isNullOrEmpty(tDownload) || Utils.isNullOrEmpty(tDownload.getDirectoryId())) {
            selectDirectoryTitle = String.format(getString(R.string.saveToListSplicing), getString(R.string.defaultList));
            downloaDdirectoryId = "";
        } else {
            TDirectory tDirectory = TDirectoryManager.queryById(tDownload.getDirectoryId(), false);
            selectDirectoryTitle = String.format(getString(R.string.saveToListSplicing), tDirectory.getName());
            downloaDdirectoryId = tDirectory.getId();
        }
        selectDirectoryView.setText(selectDirectoryTitle);
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
    public void downloadVodUrlSuccess(List<DialogItemBean> urls, String playNumber) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            cancelDialog();
            if (urls.size() > 1) {
                alertDialog = VideoUtils.showMultipleVideoSources4Download(this,
                        urls,
                        (adapter, view, position) -> {
                            startDownload(urls.get(position).getUrl(), playNumber);
                            Utils.cancelDialog(alertDialog);
                        }
                );
            } else
                startDownload(urls.get(0).getUrl(), playNumber);
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
                VideoUtils.startSniffing(this, downloadDramaUrl, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.DOWNLOAD);
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
            List<DialogItemBean> urls = event.getUrls();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIRECTORY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String type = data.getStringExtra("type");
                String directoryId = data.getStringExtra("directoryId");
                new Handler().postDelayed(() -> {
                    if (Objects.equals(type, DirectoryTypeEnum.FAVORITE.getName())) {
                        favoriteVod(directoryId);
                    } else if (Objects.equals(type, DirectoryTypeEnum.DOWNLOAD.getName())) {
                        downloaDdirectoryId = directoryId;
                        String selectDirectoryTitle;
                        if (directoryId.isEmpty()) {
                            selectDirectoryTitle = String.format(getString(R.string.saveToListSplicing), getString(R.string.defaultList));
                        } else {
                            TDirectory tDirectory = TDirectoryManager.queryById(directoryId, false);
                            selectDirectoryTitle = String.format(getString(R.string.saveToListSplicing), tDirectory.getName());
                        }
                        selectDirectoryView.setText(selectDirectoryTitle);
                        TDownload tDownload = TDownloadManager.queryByVideoTitle(detailsTitle);
                        if (!Utils.isNullOrEmpty(tDownload)) {
                            // 存在下载 更新数据
                            TDownloadManager.updateDownloadDirectoryId(tDownload.getDownloadId(), downloaDdirectoryId);
                            EventBus.getDefault().post(REFRESH_DOWNLOAD);
                        }
                    }
                }, 500);
            }
        }
    }
}
