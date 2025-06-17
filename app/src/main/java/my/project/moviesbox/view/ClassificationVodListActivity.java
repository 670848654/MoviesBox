package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.ClassificationAdapter;
import my.project.moviesbox.adapter.VodListAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.ClassificationVodListContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.custom.FabExtendingOnScrollListener;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.model.ClassificationVodListModel;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.presenter.ClassificationVodListPresenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: ClassificationVodListActivity
  * @描述: 分类视频列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:07
  * @版本: 1.0
 */
public class ClassificationVodListActivity extends BaseActivity<ClassificationVodListModel, ClassificationVodListContract.View, ClassificationVodListPresenter>
        implements ClassificationVodListContract.View, ClassificationAdapter.OnItemClick {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    private ClassificationAdapter classificationAdapter;
    private RecyclerView classificationListRv;
    private BottomSheetDialog classificationBottomSheetDialog;
    private final List<MultiItemEntity> vodMultiItemEntities = new ArrayList<>();
    private VodListAdapter adapter;
    private String title;
    private String[] paramsTitle; // 参数标题
    private String[] paramsUrl; // 参数URL
    private  boolean groupMultipleChoices = parserInterface.setClassificationGroupMultipleChoices(); // 分类组是否为多选联动
    private LinearLayout buttonToggleGroup;
    @BindView(R.id.classFab)
    ExtendedFloatingActionButton classFab;
    private int fabHeight = 0;

    @Override
    protected ClassificationVodListPresenter createPresenter() {
        return new ClassificationVodListPresenter(this);
    }

    @Override
    protected void loadData() {
        vodMultiItemEntities.clear();
        mPresenter.loadMainData(true, paramsUrl);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_vod_list;
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        title = Utils.isNullOrEmpty(bundle.getString("title")) ? "" : bundle.getString("title");
        paramsUrl = bundle.getStringArray("params");
        paramsTitle = new String[paramsUrl.length];
        setToolbar(toolbar, title, "");
        initSwipe();
        initFab();
        initDefaultAdapter();
    }

    @Override
    protected void initBeforeView() {

    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> retryListener());
    }

    private void initFab() {
        if (Utils.checkHasNavigationBar(this))
        {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) classFab.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 1);
            classFab.setLayoutParams(params);
        }
    }

    @OnClick(R.id.classFab)
    public void openBSD(View view) {
        Utils.setVibration(view);
        classificationBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        classificationBottomSheetDialog.show();
    }

    public void initDefaultAdapter() {
        adapter = new VodListAdapter(vodMultiItemEntities);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            VodDataBean bean = (VodDataBean) adapter.getItem(position);
            String url = bean.getUrl();
            String title = bean.getTitle();
            openVodDetail(title, url);
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            mRecyclerView.postDelayed(() -> {
                if (page >= pageCount) {
                    //数据全部加载完毕
                    adapter.getLoadMoreModule().loadMoreEnd();
                    application.showToastMsg(getString(R.string.noMoreContent), DialogXTipEnum.SUCCESS);
                } else {
                    if (isErr) {
                        //成功获取更多数据
                        page++;
                        paramsUrl[paramsUrl.length-1] = String.valueOf(page);
                        mPresenter.loadPageData(paramsUrl);
                        application.showToastMsg(String.format(LOAD_PAGE_AND_ALL_PAGE, (parserInterface.startPageNum() == 0 ? page+1 : page), pageCount), DialogXTipEnum.DEFAULT);
                    } else {
                        //获取更多数据失败
                        page--;
                        isErr = true;
                        adapter.getLoadMoreModule().loadMoreFail();
                    }
                }
            }, 500);
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        mRecyclerView.setAdapter(adapter);
        // 监听 RecyclerView 的滑动事件
        mRecyclerView.addOnScrollListener(new FabExtendingOnScrollListener(classFab));
        adapter.setEmptyView(rvView);

        View classificationView = LayoutInflater.from(this).inflate(R.layout.dialog_classification, null);
        buttonToggleGroup = classificationView.findViewById(R.id.toggleButton);
        Button restButton = classificationView.findViewById(R.id.rest);
        if (!groupMultipleChoices)
            restButton.setVisibility(View.GONE);
        restButton.setOnClickListener(view -> {
            // 重置过滤器
            Utils.setVibration(view);
            for (MultiItemEntity it : classificationAdapter.getData()) {
                ClassificationDataBean classificationDataBean = (ClassificationDataBean) it;
                paramsUrl[classificationDataBean.getIndex()] = "";
                paramsTitle[classificationDataBean.getIndex()] = "";
                for (int i = 0, size = classificationDataBean.getItemList().size(); i<size; i++) {
                    classificationDataBean.getItemList().get(i).setSelected(i==0);
                }
            }
            classificationAdapter.notifyDataSetChanged();
        });
        Button doneButton = classificationView.findViewById(R.id.done);
        doneButton.setOnClickListener(view -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0, size = paramsTitle.length - 1; i < size; i++) {
                if (!Utils.isNullOrEmpty(paramsTitle[i])) {
                    stringBuilder.append(paramsTitle[i]);
                    stringBuilder.append(",");
                }
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.insert(0, "[");
                stringBuilder.append("]");
            }
            toolbar.setTitle(title + stringBuilder);
            // 条件检索
            Utils.setVibration(view);
            page = parserInterface.startPageNum();
            paramsUrl[paramsUrl.length-1] = String.valueOf(page);
           loadData();
            classificationBottomSheetDialog.dismiss();
        });
        Button closeButton = classificationView.findViewById(R.id.close);
        closeButton.setOnClickListener(view -> {
            Utils.setVibration(view);
            classificationBottomSheetDialog.dismiss();
        });
        classificationListRv = classificationView.findViewById(R.id.rv_list);
        classificationAdapter = new ClassificationAdapter(multiItemEntities, this);
        classificationAdapter.setAnimationEnable(true);
        classificationAdapter.setAdapterAnimation(new AlphaInAnimation());
        classificationListRv.setLayoutManager(new LinearLayoutManager(this));
        classificationListRv.setAdapter(classificationAdapter);
        classificationBottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        classificationBottomSheetDialog.setContentView(classificationView);
    }

    private void setToolbarInfo() {
        toolbar.setSubtitle(String.format(PAGE_AND_ALL_PAGE, (parserInterface.startPageNum() == 0 ? page+1 : page), pageCount));
    }

    public void openVodDetail(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        startActivity(new Intent(ClassificationVodListActivity.this, DetailsActivity.class).putExtras(bundle));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vodMultiItemEntities.size() == 0)
            setRecyclerViewEmpty();
        else
            setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    @Override
    protected void retryListener() {
        page = parserInterface.startPageNum();
        pageCount = parserInterface.startPageNum();
        paramsUrl[paramsUrl.length-1] = String.valueOf(page);
        loadData();
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int spanCount;
        if (vodMultiItemEntities.size() > 0 && vodMultiItemEntities.get(0).getItemType() == VodItemStyleEnum.STYLE_16_9.getType()) {
            spanCount = parserInterface.setVodList16_9ItemSize(Utils.isPad(), isPortrait, false);
        } else {
            spanCount = parserInterface.setVodListItemSize(Utils.isPad(), isPortrait, false);
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void loadingView() {
        if (isFinishing()) return;
        mSwipe.setRefreshing(false);
        toolbar.setSubtitle("");
        rvLoading();
    }

    @Override
    public void errorView(String msg) {
    }

    @Override
    public void emptyView() {
        toolbar.setSubtitle("");
        adapter.setNewInstance(null);
        setRecyclerViewEmpty();
    }

    @Override
    public void successClassList(List<ClassificationDataBean> classificationDataBeans) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (multiItemEntities.size() == 0) {
                // 只有分类数据为空才赋值
                for (ClassificationDataBean classificationDataBean : classificationDataBeans) {
                    multiItemEntities.add(classificationDataBean);
                }
                classificationAdapter.notifyDataSetChanged();
                classFab.setVisibility(View.VISIBLE);
                buttonToggleGroup.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void errorClassList(String msg) {

    }

    @Override
    public void emptyClassList() {

    }

    @Override
    public void successVodList(boolean firstTimeData, List<VodDataBean> vodDataBeans, int pageCount) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (firstTimeData) {
                vodMultiItemEntities.addAll(vodDataBeans);
                hideProgress();
                mSwipe.setRefreshing(false);
                this.pageCount = pageCount;
                new Handler().postDelayed(() -> {
                    adapter.setNewInstance(vodMultiItemEntities);
                    setRecyclerViewView();
                }, 500);
            } else {
                adapter.addData(vodDataBeans);
                setLoadState(adapter, true);
            }
            if (fabHeight == 0) {
                // 添加布局完成监听器
                ViewTreeObserver viewTreeObserver = classFab.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // 获取 FloatingActionButton 的高度
                        fabHeight = classFab.getHeight();
                        mRecyclerView.setPadding(0,0,0, fabHeight+Utils.dpToPx(ClassificationVodListActivity.this, 32));
                        // 由于已经获取了高度，因此可以移除监听器，避免重复调用
                        classFab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
            setToolbarInfo();
        });
    }

    @Override
    public void errorVodList(boolean firstTimeData, String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (firstTimeData) {
                mSwipe.setRefreshing(false);
                rvError(msg);
                Utils.showAlert(this,
                        getString(R.string.errorDialogTitle),
                        msg,
                        false,
                        getString(R.string.defaultPositiveBtnText),
                        "",
                        "",
                        (dialog, which) -> dialog.dismiss(),
                        null,
                        null);
            } else {
                setLoadState(adapter, false);
                application.showToastMsg(msg, DialogXTipEnum.ERROR);
            }
        });
    }

    @Override
    public void emptyVodList(boolean firstTimeData, String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (firstTimeData) {
                mSwipe.setRefreshing(false);
                rvEmpty(msg);
            } else {
                setLoadState(adapter, false);
                App.getInstance().showToastMsg(msg, DialogXTipEnum.ERROR);
            }
        });
    }

    @Override
    public void onChipClick(int index, String title, String url) {
        if (isFinishing()) return;
        paramsTitle[index] = title;
        paramsUrl[index] = url;
        LogUtil.logInfo("params["+index+"]", url);
        if (!groupMultipleChoices) {
            for (MultiItemEntity it : classificationAdapter.getData()) {
                ClassificationDataBean classificationDataBean = (ClassificationDataBean) it;
                for (int i = 0, size = classificationDataBean.getItemList().size(); i<size; i++) {
                    ClassificationDataBean.Item item = classificationDataBean.getItemList().get(i);
                    classificationDataBean.getItemList().get(i).setSelected(item.getUrl().equals(url));
                    classificationAdapter.notifyItemChanged(i);
                }
            }
//            classificationAdapter.notifyDataSetChanged();
        }
    }
}