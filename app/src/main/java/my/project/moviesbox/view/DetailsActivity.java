package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOWNLOAD;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_FAVORITE;
import static my.project.moviesbox.utils.Utils.isPad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
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

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DetailTagAdapter;
import my.project.moviesbox.adapter.DetailsExpandListItemAdapter;
import my.project.moviesbox.adapter.DetailsListItemAdapter;
import my.project.moviesbox.adapter.DownloadDramaAdapter;
import my.project.moviesbox.adapter.DramaAdapter;
import my.project.moviesbox.bean.DownloadDramaBean;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.contract.DetailsContract;
import my.project.moviesbox.contract.DownloadVideoContract;
import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.custom.VideoPreviewDialog;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.entity.TDownload;
import my.project.moviesbox.database.entity.THistoryData;
import my.project.moviesbox.database.enums.DirectoryTypeEnum;
import my.project.moviesbox.database.manager.TDirectoryManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.databinding.ActivityDetailsBinding;
import my.project.moviesbox.databinding.DialogDownloadDramaBinding;
import my.project.moviesbox.databinding.DialogDramaBinding;
import my.project.moviesbox.databinding.DialogMultiRecommendBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.event.DramaEvent;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.model.DetailsModel;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.config.ItemStyleEnum;
import my.project.moviesbox.presenter.DetailsPresenter;
import my.project.moviesbox.presenter.DownloadVideoPresenter;
import my.project.moviesbox.presenter.VideoPresenter;
import my.project.moviesbox.utils.DownloadUtils;
import my.project.moviesbox.utils.ImageCache;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoAlertUtils;
import my.project.moviesbox.view.base.BaseMvpActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: DetailsActivity
  * @描述: 影视详情视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:09
  * @版本: 1.0
 */
public class DetailsActivity extends BaseMvpActivity<DetailsModel, DetailsContract.View, DetailsPresenter, ActivityDetailsBinding> implements DetailsContract.View,
        VideoContract.View, DownloadVideoContract.View {
    /**
     * toolbar
     */
    Toolbar toolbar;
    /**
     * 收藏按钮视图
     */
    MaterialButton favoriteBtn;
    /**
     * 平板图片视图
     */
    public ConstraintLayout padImgBoxView;
    /**
     * 影视图片视图 (1:1.4)
     */
    public MaterialCardView imgType0View;
    /**
     * 影视图片视图 (1:1.4)
     */
    public ImageView vodImgType0View;
    /**
     * 影视图片视图 (16:9)
     */
    public MaterialCardView imgType1View;
    /**
     * 影视图片视图 (16:9)
     */
    public ImageView vodImgType1View;
    /**
     * 影视详情视图
     */
    ExpandableTextView expandableDescView;
    /**
     * 影视标题视图
     */
    public TextView titleView;
    /**
     * 下拉刷新视图
     */
    SwipeRefreshLayout mSwipe;
    /**
     * 预览图片
     */
    Button previewBtn;
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
    private List<DetailsDataBean.DramasItem> dramaList = new ArrayList<>();
    /**
     * 播放列表相关
     */
    RecyclerView dramaListRv;
    private DramaAdapter dramaListAdapter;
    LinearLayout playLinearLayout;
    /**
     * 展开播放列表相关
     */
    private RecyclerView expandListRv;
    private DramaAdapter expandListAdapter;
    private BottomSheetDialog expandListBSD;
    RelativeLayout openDramaView;
    /**
     * 多季相关
     */
    RecyclerView multiListRv;
    private LinearLayoutManager multiLayoutManager;
    protected DetailsListItemAdapter multiAdapter;
    LinearLayout multiLinearLayout;
    /**
     * 相关推荐相关
     */
    RecyclerView recommendRv;
    private LinearLayoutManager recommendLayoutManager;
    protected DetailsListItemAdapter recommendAdapter;
    LinearLayout recommendLinearLayout;
    /**
     * 加载错误视图相关
     */
    RelativeLayout errorBgView;
    TextView errorMsgView;
    /**
     * 详情视图
     */
    RelativeLayout descView;
    /**
     * 背景模糊图片
     */
    public ImageView bgView;
    /**
     * TAG列表
     */
    RecyclerView detailTagRecyclerView;
    private RecyclerView expandTagListRv;
    private BottomSheetDialog tagBottomSheetDialog;
    List<ClassificationDataBean.Item> detailTags = new ArrayList<>();
    DetailTagAdapter detailTagAdapter;
    /**
     * 其他视图
     */
    public TextView infoView;
    /**
     * 更新时间视图
     */
    public TextView updateTimeView;
    /**
     * 评分视图
     */
    public TextView scoreView;
    /**
     * 整体滚动视图
     */
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
    private final DownloadVideoPresenter downloadVideoPresenter = new DownloadVideoPresenter(this); // 下载适配器
    private RecyclerView downloadListRv;
    private BottomSheetDialog downloadBottomSheetDialog;
    private List<DownloadDramaBean> downloadBean = new ArrayList<>();
    private DownloadDramaAdapter downloadAdapter;
    /**
     * 多播放列表选择相关
     */
    RelativeLayout multiPlayLayout;
    TextInputLayout multiPlayInput;
    AutoCompleteTextView selectedDrama;
    private List<String> dramaTitles;
    /**
     * 所选播放列表下标
     */
    private int sourceIndex = 0;
    private RecyclerView multiRecommendRv;
    private BottomSheetDialog multiRecommendDialog;
    private View multiRecommendView;
    /**
     * 多季/推荐列表展开适配器
     */
    private final List<MultiItemEntity> detailsExpandData = new ArrayList<>();
    private DetailsExpandListItemAdapter detailsExpandListItemAdapter;
    /**
     * 保存清单按钮
     */
    private Button selectDirectoryView;
    /**
     *  下载保存清单ID
     */
    private String downloaDdirectoryId;
    /**
     * 浮动按钮
     */
    ExtendedFloatingActionButton detailFab;
    private int fabHeight = 0;
    private final VideoPresenter videoPresenter = new VideoPresenter(this);
    /**
     * 下载工具类
     */
    private DownloadUtils downloadUtils;
    /**
     * 弹窗工具类
     */
    private VideoAlertUtils videoAlertUtils;
    TextView lastWatchView;

    @Override
    protected DetailsPresenter createPresenter() {
        return new DetailsPresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true, detailsUrl);
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
//        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0);
        Window window = getWindow();
        // 透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            // 内容延伸到状态栏
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        getBundle();
        initToolbar();
        initSwipe();
        initFab();
        initAdapter();
        previewBtn.setVisibility(Utils.isPad() ? View.GONE : View.VISIBLE);
        videoAlertUtils = new VideoAlertUtils(this);
    }

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityDetailsBinding inflateBinding(LayoutInflater inflater) {
        return ActivityDetailsBinding.inflate(inflater);
    }


    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        toolbar = binding.toolbar;
        favoriteBtn = binding.favorite;
        padImgBoxView = binding.padImgBox;
        imgType0View = binding.imgViewType0;
        vodImgType0View = binding.vodImgType0;
        imgType1View = binding.imgViewType1;
        vodImgType1View = binding.vodImgType1;
        expandableDescView = binding.desc;
        titleView = binding.title;
        mSwipe = binding.mSwipe;
        previewBtn = binding.preview;
        dramaListRv = binding.dramaList;
        playLinearLayout = binding.playLayout;
        openDramaView = binding.openDrama;
        multiListRv = binding.multiList;
        multiLinearLayout = binding.multiLayout;
        recommendRv = binding.recommendList;
        recommendLinearLayout = binding.recommendLayout;
        errorBgView = binding.errorBg;
        errorMsgView = binding.errorMsg;
        descView = binding.descView;
        bgView = binding.bg;
        detailTagRecyclerView = binding.chipGroup;
        infoView = binding.info;
        updateTimeView = binding.updateTime;
        scoreView = binding.scoreView;
        scrollView = binding.scrollview;
        multiPlayLayout = binding.multiPlayLayout;
        multiPlayInput = binding.multiPlayInput;
        selectedDrama = binding.selectedText;
        detailFab = binding.detailFab;
        lastWatchView = binding.lastWatch;
    }

    @Override
    public void initClickListeners() {
        // 预览、图像点击
        binding.preview.setOnClickListener(v -> handleClick(v));
        binding.vodImgType0.setOnClickListener(v -> handleClick(v));
        binding.vodImgType1.setOnClickListener(v -> handleClick(v));
        // 收藏
        binding.favorite.setOnClickListener(v -> handleClick(v));
        // 下载
        binding.download.setOnClickListener(v -> handleClick(v));
        // 浏览器打开
        binding.browser.setOnClickListener(v -> handleClick(v));
        // 排序
        binding.order.setOnClickListener(v -> handleClick(v));
        // 剧集
        binding.drama.setOnClickListener(v -> handleClick(v));
        // 浮动按钮点击事件
        binding.detailFab.setOnClickListener(v -> handleClick(v));
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
            if (!detailFab.isShown()) return;
            if (scrollY > oldScrollY) {
                detailFab.shrink();
            } else {
                detailFab.extend();
            }
        });
    }

    public void handleClick(View view) {
        Utils.setVibration(view);
        ActivityOptions options;
        switch (view.getId()) {
            case R.id.preview:
                binding.preview.setTransitionName("image_transition");
                options = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        binding.preview,
                        "image_transition"
                );
                openPreviewActivity(options);
                break;
            case R.id.vod_img_type0:
                binding.vodImgType0.setTransitionName("image_transition");
                options = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        binding.vodImgType0,
                        "image_transition"
                );
                openPreviewActivity(options);
                break;
            case R.id.vod_img_type1:
                binding.vodImgType1.setTransitionName("image_transition");
                options = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        binding.vodImgType1,
                        "image_transition"
                );
                openPreviewActivity(options);
                break;
            case R.id.favorite:
                if (!isFavorite)
                    // 去选择清单进行收藏
                    startActivityForResult(new Intent(this, DirectoryActivity.class).putExtra("type", DirectoryTypeEnum.FAVORITE.getName()), DIRECTORY_REQUEST_CODE);
                else
                    favoriteVod("");
                break;
            case R.id.download:
                downloadUtils.select2Download();
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
            case R.id.detailFab:
                DetailsDataBean.DetailsFabBren fabBren = detailsDataBean.getDetailsFabBren();
                if (fabBren != null) {
                    if (fabBren.getOpenClass().equals(DetailsActivity.class)) {
                        detailsTitle = fabBren.getTitle();
                        detailsUrl = fabBren.getUrl();
                        vodUrlList.add(detailsUrl);
                        openVodDesc();
                    } else if (fabBren.getOpenClass().equals(SniffingVideoActivity.class)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("url", fabBren.getUrl());
                        bundle.putString("vodId", vodId);
                        startActivity(new Intent(this, SniffingVideoActivity.class).putExtras(bundle));
                    }
                }
                break;
        }
    }

    private void openPreviewActivity(ActivityOptions options) {
        ImageCache.ImagePathResult result = ImageCache.prepareImagePath(this, detailsDataBean.getImg(), "preview.jpg");
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra("image_path", result.path);
        intent.putExtra("is_temp_file", result.isTempFile);
        startActivity(intent, options.toBundle());
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
        DialogDramaBinding dialogDramaBinding = DialogDramaBinding.inflate(LayoutInflater.from(this));
        expandTagListRv = dialogDramaBinding.dramaList;
        expandTagListRv.setAdapter(detailTagAdapter);
        tagBottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        tagBottomSheetDialog.setContentView(dialogDramaBinding.getRoot());
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW); // 横向排布
        layoutManager.setFlexWrap(FlexWrap.WRAP);         // 换行
        layoutManager.setJustifyContent(JustifyContent.FLEX_START); // 起始对齐
        expandTagListRv.setLayoutManager(layoutManager);
        detailTagAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            tagBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            tagBottomSheetDialog.show();
            return true;
        });
//        if (Utils.isPad()) {
//            detailTagRecyclerView.setLayoutManager(new AutoLineFeedLayoutManager());
//        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            detailTagRecyclerView.setLayoutManager(linearLayoutManager);
//        }
        detailTagRecyclerView.setAdapter(detailTagAdapter);
        dramaListAdapter = new DramaAdapter(this, false, dramaList);
        dramaListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
            playVideo(dramaListAdapter, position);
        });
        dramaListAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            expandListBSD.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            expandListBSD.show();
            return true;
        });
        LinearLayoutManager dramaListLayoutManager = getLinearLayoutManager();
        dramaListRv.setLayoutManager(dramaListLayoutManager);
        dramaListRv.setAdapter(dramaListAdapter);
        dramaListRv.setNestedScrollingEnabled(false);

        DialogDramaBinding dialogDramaBinding1 = DialogDramaBinding.inflate(LayoutInflater.from(this));
        expandListRv = dialogDramaBinding1.dramaList;
        expandListAdapter = new DramaAdapter(this, false, new ArrayList<>());
        expandListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
            expandListBSD.dismiss();
            playVideo(expandListAdapter, position);
        });
        expandListRv.setAdapter(expandListAdapter);
        expandListBSD = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        expandListBSD.setContentView(dialogDramaBinding1.getRoot());

        DialogDownloadDramaBinding dialogDownloadDramaBinding = DialogDownloadDramaBinding.inflate(LayoutInflater.from(this));
        ExpandableTextView expandableTextView = dialogDownloadDramaBinding.info;
        expandableTextView.setContent(String.format(getString(R.string.downloadInfoContent), SharedPreferencesUtils.getDataName()));
        expandableTextView.setNeedExpend(true);
        selectDirectoryView = dialogDownloadDramaBinding.selectDirectory;
        selectDirectoryView.setOnClickListener(v -> {
            // 去选择清单进行收藏
            startActivityForResult(new Intent(this, DirectoryActivity.class).putExtra("type", DirectoryTypeEnum.DOWNLOAD.getName()), DIRECTORY_REQUEST_CODE);
        });
        setSelectDirectoryData();
        downloadListRv = dialogDownloadDramaBinding.downloadList;

        downloadAdapter = new DownloadDramaAdapter(this, new ArrayList<>());
        downloadAdapter.setOnItemClickListener((adapter, view, position) -> {
            Utils.setVibration(view);
            DownloadDramaBean bean = downloadBean.get(position);
            if (bean.isHasDownload()) {
                application.showToastMsg(getString(R.string.hasDownloadTask), DialogXTipEnum.WARNING);
                return;
            }
            downloadDramaUrl = downloadBean.get(position).getUrl();
            downloadDramaNumber = downloadBean.get(position).getTitle();
            downloadUtils.createDownloadConfig(detailsTitle);
            // 下载开始
            if (!parserInterface.playUrlNeedParser()) {
                downloadUtils.startDownload(detailsTitle, detailsUrl, downloadDramaUrl, downloadDramaNumber, detailsDataBean.getImg(), downloaDdirectoryId);
                return;
            }
            alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
            downloadVideoPresenter.loadData(downloadDramaUrl, downloadDramaNumber);
        });
        downloadListRv.setAdapter(downloadAdapter);
        downloadBottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        downloadBottomSheetDialog.setContentView(dialogDownloadDramaBinding.getRoot());

        multiRecommendDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        DialogMultiRecommendBinding dialogMultiRecommendBinding = DialogMultiRecommendBinding.inflate(LayoutInflater.from(this));
        multiRecommendView = dialogMultiRecommendBinding.getRoot();
        multiRecommendRv = dialogMultiRecommendBinding.dramaList;
        detailsExpandListItemAdapter = new DetailsExpandListItemAdapter(new ArrayList<>());
        detailsExpandListItemAdapter.setOnItemClickListener((adapter, view, position) -> {
            Utils.setVibration(view);
            getDescData((DetailsDataBean.Recommend) adapter.getItem(position));
        });
        detailsExpandListItemAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            DetailsDataBean.Recommend bean = (DetailsDataBean.Recommend) adapter.getItem(position);
            String title = bean.getTitle();
            String previewUrl = bean.getPreviewUrl();
            if (Utils.isNullOrEmpty(previewUrl)) return false;
            VideoPreviewDialog dialog = new VideoPreviewDialog(this, title, bean.getUrl(), previewUrl, bean.getImg());
            dialog.show();
            return true;
        });
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
                                // 显示图片
                                padImgBoxView.setVisibility(View.VISIBLE);
                                // 回调结果
                                if (width > height) {
                                    vodImgType1View.setTag(R.id.imageid, detailsDataBean.getImg());
                                    vodImgType1View.setImageBitmap(resource);
                                    imgType1View.setVisibility(View.VISIBLE);
                                    Utils.setImageViewAnim(vodImgType1View);
                                } else {
                                    vodImgType0View.setTag(R.id.imageid, detailsDataBean.getImg());
                                    vodImgType0View.setImageBitmap(resource);
                                    imgType0View.setVisibility(View.VISIBLE);
                                    Utils.setImageViewAnim(vodImgType0View);
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {}

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                padImgBoxView.setVisibility(View.GONE);
                            }
                        });
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
        ArrayAdapter dramaTitlesApter = new ArrayAdapter(this, R.layout.text_list_item, dramaTitles);
        selectedDrama.setAdapter(dramaTitlesApter);
        selectedDrama.setOnItemClickListener((parent, view, position, id) -> {
            Utils.setVibration(view);
            setAdapterData(position);
        });
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
               alertDialog = videoAlertUtils.showMultipleVideoSources(
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
                dramaUrl = Utils.isNullOrEmpty(dramaUrl) ? playUrl : dramaUrl;
                TFavoriteManager.updateFavorite(dramaUrl, dramaTitle, vodId);
                TVideoManager.addVideoHistory(vodId, dramaUrl, sourceIndex, dramaTitle);
                videoAlertUtils.openPlayer(true, dramaTitle, playUrl, detailsTitle, dramaUrl, dramaList, clickIndex, vodId, sourceIndex);
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
                videoAlertUtils.startSniffing(vodId, dramaUrl, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.PLAY);
            } else
                Utils.showAlert(this,
                        R.drawable.round_warning_24,
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
    public void errorNet(boolean onlyGetPlayUrl, String msg) {
        if (isFinishing()) return;
        //网络出错
        runOnUiThread(() -> Utils.showAlert(this,
                R.drawable.round_warning_24,
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
        if (downloadUtils != null)
            downloadUtils.release();
        if (videoAlertUtils != null)
            videoAlertUtils.release();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DramaEvent event) {
        if (isFinishing()) return;
        if (event.getVodId().equals(vodId)) {
            clickIndex = event.getClickIndex();
            dramaList.get(clickIndex).setSelected(true);
            dramaListAdapter.notifyItemChanged(clickIndex);
            expandListAdapter.notifyItemChanged(clickIndex);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshLastWatch(RefreshEnum refreshEnum) {
        if (isFinishing()) return;
        if (Objects.requireNonNull(refreshEnum) == RefreshEnum.REFRESH_LAST_WATCH) {
            showLastWatch();
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
            showLastWatch();
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
                String errorMsg = detailsDataBean.getNoDramaListFoundMsg();
                errorMsgView.setText(Utils.isNullOrEmpty(errorMsg) ? getString(R.string.noEpisode) : errorMsg);
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
                multiAdapter.setOnItemClickListener((adapter, view, position) -> {
                    Utils.setVibration(view);
                    getDescData((DetailsDataBean.Recommend) adapter.getItem(position));
                });
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
                recommendAdapter.setOnItemClickListener((adapter, view, position) -> {
                    Utils.setVibration(view);
                    getDescData((DetailsDataBean.Recommend) adapter.getItem(position));
                });
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

    private void showLastWatch() {
        String msg = "上次观看至「%s」%s";
        THistoryData tHistoryData = TVideoManager.queryLastWatchData(vodId);
        if (!Utils.isNullOrEmpty(tHistoryData)) {
            int duration = (int) tHistoryData.getWatchProgress(); // 已观看，单位：毫秒
            int progress = (int) tHistoryData.getVideoDuration(); // 总时长，单位：毫秒
            String percentStr;
            if (duration == 0 && progress == 0) {
                percentStr = "播放出错";
            } else if (duration == 0) {
                percentStr = "已看完";
            } else {
                float percent = (duration * 100f) / progress;
                percentStr = String.format(Locale.getDefault(), "进度%.1f%%", percent);
            }
            lastWatchView.setText(String.format(msg, tHistoryData.getVideoNumber(), percentStr));
            lastWatchView.setVisibility(View.VISIBLE);
        } else
            lastWatchView.setVisibility(View.GONE);
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
        if (downloadUtils == null)
            downloadUtils = new DownloadUtils(this, vodId, downloadAdapter, downloadBottomSheetDialog);
        else
            downloadUtils.updateVodId(vodId);
    }

    @Override
    public void downloadVodUrlSuccess(List<DialogItemBean> urls, String playNumber) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            cancelDialog();
            if (urls.size() > 1) {
                alertDialog = videoAlertUtils.showMultipleVideoSources4Download(
                        urls,
                        (adapter, view, position) -> {
                            downloadUtils.startDownload(detailsTitle, detailsUrl, urls.get(position).getUrl(), playNumber, detailsDataBean.getImg(), downloaDdirectoryId);
                            Utils.cancelDialog(alertDialog);
                        }
                );
            } else
                downloadUtils.startDownload(detailsTitle, detailsUrl, urls.get(0).getUrl(), playNumber, detailsDataBean.getImg(), downloaDdirectoryId);
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
                videoAlertUtils.startSniffing(vodId, downloadDramaUrl, VideoSniffEvent.ActivityEnum.DETAIL, VideoSniffEvent.SniffEnum.DOWNLOAD);
            } else {
                Utils.showAlert(this,
                        R.drawable.round_warning_24,
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
        if (event.getActivityEnum() == VideoSniffEvent.ActivityEnum.DETAIL && event.getVodId().equals(vodId)) {
            cancelDialog();
            List<DialogItemBean> urls = event.getUrls();
            if (event.isSuccess()) {
                if (!Utils.isNullOrEmpty(event.getDramaTitle())) {
                    dramaTitle = event.getDramaTitle();
                    downloadDramaNumber = event.getDramaTitle();
                    downloadUtils.createDownloadConfig(detailsTitle);
                }
                switch (event.getSniffEnum()) {
                    case PLAY:
                        successPlayUrl(urls);
                        break;
                    case DOWNLOAD:
                        downloadVodUrlSuccess(urls, downloadDramaNumber);
                        break;
                }
            } else
                videoAlertUtils.sniffErrorDialog();
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
