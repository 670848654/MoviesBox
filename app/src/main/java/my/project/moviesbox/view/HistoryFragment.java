package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.HistoryListAdapter;
import my.project.moviesbox.contract.HistoryContract;
import my.project.moviesbox.contract.UpdateImgContract;
import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.database.entity.THistoryWithFields;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.event.RefreshEvent;
import my.project.moviesbox.event.ShowProgressEvent;
import my.project.moviesbox.event.UpdateImgEvent;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.presenter.HistoryPresenter;
import my.project.moviesbox.presenter.UpdateImgPresenter;
import my.project.moviesbox.presenter.VideoPresenter;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoUtils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: HistoryFragment
  * @描述: 历史记录视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:10
  * @版本: 1.0
 */
public class HistoryFragment extends BaseFragment<HistoryContract.View, HistoryPresenter> implements HistoryContract.View,
        VideoContract.View, UpdateImgContract.View {
    private View view;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private HistoryListAdapter adapter;
    private List<THistoryWithFields> historyBeans = new ArrayList<>();
    private int limit = 10;
    private int historyCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private AlertDialog alertDialog;
    private VideoPresenter videoPresenter;
//    private UpdateImgPresenter updateImgPresenter;
    private String vodId;
    private String vodTitle;
    private String vodDramaUrl;
    private String vodDramaTitle;
    private int vodPlaySource;
    private int vodSource;
    private List<DetailsDataBean.DramasItem> dramasItems;
    private int clickIndex = 0;
    @BindView(R.id.remove_all)
    FloatingActionButton removeAllFAB;
    private UpdateImgPresenter updateImgPresenter;

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_my_list, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        historyCount = THistoryManager.queryHistoryCount();
        initAdapter();
        initFab();
        return view;
    }

    private void initAdapter() {
        adapter = new HistoryListAdapter(getActivity(), historyBeans);
        adapter.setAnimationEnable(true);
        adapter.setAdapterAnimation(new AlphaInAnimation());
        adapter.setEmptyView(rvView);
        adapter.addChildClickViewIds(R.id.option);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            vodId = historyBeans.get(position).getVideoId();
            vodTitle = historyBeans.get(position).getVideoTitle();
            vodDramaUrl = historyBeans.get(position).getVideoUrl();
            vodDramaTitle = historyBeans.get(position).getVideoNumber();
            vodPlaySource = historyBeans.get(position).getVideoPlaySource();
            vodSource = historyBeans.get(position).getVideoSource();
            videoPresenter = new VideoPresenter(
                    false,
                    vodTitle,
                    vodDramaUrl,
                    vodPlaySource,
                    vodDramaTitle,
                    this
            );
            alertDialog = Utils.getProDialog(getActivity(), R.string.parseVodPlayUrl);
            videoPresenter.loadData(true);
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            switch (view.getId()) {
                /*case R.id.desc_view:
                    Bundle bundle = new Bundle();
                    bundle.putString("title", historyBeans.get(position).getVideoTitle());
                    bundle.putString("url", historyBeans.get(position).getTHistory().getVideoDescUrl());
                    startActivityForResult(new Intent(getActivity(), DetailsActivity.class).putExtras(bundle), 3000);
                    break;*/
                case R.id.option:
//                    showDeleteHistoryDialog(position, historyBeans.get(position).getTHistory().getHistoryId(), false);
                    setMenu(view, R.menu.history_menu, R.id.delete, item -> {
                        switch (item.getItemId()) {
                            case R.id.desc:
                                Bundle bundle = new Bundle();
                                bundle.putString("title", historyBeans.get(position).getVideoTitle());
                                bundle.putString("url", historyBeans.get(position).getTHistory().getVideoDescUrl());
                                startActivityForResult(new Intent(getActivity(), DetailsActivity.class).putExtras(bundle), 3000);
                                break;
                            case R.id.delete:
                                showDeleteHistoryDialog(position, historyBeans.get(position).getTHistory().getHistoryId(), false);
                                break;
                        }
                        return true;
                    });
                    break;
            }
        });
        /*adapter.setOnItemLongClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return false;
            View v = adapter.getViewByPosition(position, R.id.title);
            setMenu(v, R.menu.history_menu, R.id.delete, item -> {
                switch (item.getItemId()) {
                    case R.id.desc:
                        Bundle bundle = new Bundle();
                        bundle.putString("title", historyBeans.get(position).getVideoTitle());
                        bundle.putString("url", historyBeans.get(position).getTHistory().getVideoDescUrl());
                        startActivityForResult(new Intent(getActivity(), DetailsActivity.class).putExtras(bundle), 3000);
                        break;
                    case R.id.delete:
                        showDeleteHistoryDialog(position, historyBeans.get(position).getTHistory().getHistoryId(), false);
                        break;
                }
                return true;
            });
            return true;
        });*/
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (historyBeans.size() >= historyCount) {
                adapter.getLoadMoreModule().loadMoreEnd();
            } else {
                if (isErr) {
                    isMain = false;
                    mPresenter = new HistoryPresenter(historyBeans.size(), limit, this);
                    loadData();
                } else {
                    isErr = true;
                    adapter.getLoadMoreModule().loadMoreFail();
                }
            }
        }, 500));
        if (Utils.checkHasNavigationBar(getActivity())) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
        mRecyclerView.setAdapter(adapter);
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.getLoadMoreModule().loadMoreComplete();
    }

    private void initFab() {
        /*if (Utils.checkHasNavigationBar(getActivity())) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) removeAllFAB.getLayoutParams();
            params.setMargins(Utils.dpToPx(getActivity(), 16),
                    Utils.dpToPx(getActivity(), 16),
                    Utils.dpToPx(getActivity(), 16),
                    Utils.getNavigationBarHeight(getActivity()) + 15);
            removeAllFAB.setLayoutParams(params);
        }*/
        removeAllFAB.setOnClickListener(view -> showDeleteHistoryDialog(0, null, true));
    }

    private void loadHistoryData() {
        isMain = true;
        historyBeans.clear();
        setRecyclerViewView();
        mPresenter = createPresenter();
        loadData();
    }

    /**
     * 删除历史记录弹窗
     * @param position
     * @param historyId
     * @param isAll
     */
    private void showDeleteHistoryDialog(int position, String historyId, boolean isAll) {
        Utils.showAlert(getActivity(),
                getString(R.string.otherOperation),
                isAll ? getString(R.string.deleteAllHistory) : getString(R.string.deleteSingleHistory),
                true,
                getString(R.string.defaultPositiveBtnText),
                getString(R.string.defaultNegativeBtnText),
                null,
                (dialogInterface, i) -> deleteHistory(position, historyId, isAll),
                (dialogInterface, i) -> dialogInterface.dismiss(),
                null);
    }

    /**
     * 删除历史记录
     * @param position
     * @param historyId
     * @param isAll
     */
    private void deleteHistory(int position, String historyId, boolean isAll) {
        THistoryManager.deleteHistory(historyId, parserInterface.getSource(), isAll);
        historyCount = THistoryManager.queryHistoryCount();
        if (!isAll)
            adapter.removeAt(position);
        else
            historyBeans.clear();
        if (historyCount == 0) {
            removeAllFAB.setVisibility(View.GONE);
            setRecyclerViewEmpty();
            rvEmpty(getString(R.string.emptyMyList));
        }
        EventBus.getDefault().post(new RefreshEvent(4));
    }

    /**
     * 播放视频
     *
     * @param url
     */
    private void playVod(String url) {
        cancelDialog();
        switch (SharedPreferencesUtils.getUserSetOpenVidePlayer()) {
            case 0:
                //调用播放器
                TFavoriteManager.updateFavorite(vodDramaUrl, vodDramaTitle, vodId);
                TVideoManager.addVideoHistory(vodId, vodDramaUrl, vodPlaySource, vodDramaTitle);
                VideoUtils.openPlayer(true, getActivity(), vodDramaTitle, url, vodTitle, vodDramaUrl, dramasItems, clickIndex, vodId, vodSource);
                break;
            case 1:
                Utils.selectVideoPlayer(getActivity(), url);
                break;
        }
    }

    @Override
    protected HistoryPresenter createPresenter() {
        return new HistoryPresenter(historyBeans.size(), limit, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(isMain);
    }

    @Override
    protected void retryListener() {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent refresh) {
        if (getActivity().isFinishing()) return;
        switch (refresh.getIndex()) {
            case 2:
                loadHistoryData();
                break;
        }
    }

    @Override
    public void loadingView() {
        setRecyclerViewEmpty();
        rvLoading();
    }

    @Override
    public void errorView(String msg) {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            setRecyclerViewEmpty();
            rvEmpty(msg);
        });
    }

    @Override
    public void emptyView() {

    }

    @Override
    public void success(List<THistoryWithFields> list) {
        if (getActivity().isFinishing()) return;
        setLoadState(true);
        getActivity().runOnUiThread(() -> {
            if (isMain) {
                new Handler().postDelayed(() -> {
                    hideProgress();
                    historyBeans = list;
                    removeAllFAB.setVisibility(View.VISIBLE);
                    adapter.setNewInstance(historyBeans);
                    setRecyclerViewView();
                }, 500);
            } else
                adapter.addData(list);
        });
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        removeAllFAB.setVisibility(View.GONE);
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), parserInterface.setHistoryListItemSize(Utils.isPad(), isPortrait)));
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void cancelDialog() {
        Utils.cancelDialog(alertDialog);
    }

    @Override
    public void successPlayUrl(List<String> urls) {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            cancelDialog();
            if (urls.size() == 1)
                playVod(urls.get(0));
            else
                VideoUtils.showMultipleVideoSources(getActivity(),
                        urls,
                        (dialog, index) -> playVod(urls.get(index)),
                        null,
                        true);
        });
    }

    @Override
    public void errorPlayUrl() {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> Utils.showAlert(getActivity(),
                getString(R.string.errorDialogTitle),
                getString(R.string.parseVodPlayUrlError),
                false,
                getString(R.string.defaultPositiveBtnText),
                "",
                "",
                (dialog, which) -> dialog.dismiss(),
                null,
                null));
    }

    @Override
    public void errorNet(String msg) {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> Utils.showAlert(getActivity(),
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
    public void successDramasList(List<DetailsDataBean.DramasItem> items) {
        if (getActivity().isFinishing()) return;
        this.dramasItems = items;
        for (int i=0,size=dramasItems.size(); i<size; i++) {
            if (dramasItems.get(i).getUrl().equals(vodDramaUrl)) {
                clickIndex = i;
                break;
            }
        }
    }

    @Override
    public void errorDramasList() {
        // 获取播放列表失败
    }

    @Override
    public void successOnlyPlayUrl(List<String> urls) {

    }

    @Override
    public void errorOnlyPlayUrl() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != videoPresenter) videoPresenter.detachView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgEvent updateImgEvent) {
        if (getActivity().isFinishing()) return;
        updateImgPresenter = new UpdateImgPresenter(updateImgEvent.getOldImgUrl(), updateImgEvent.getDescUrl(), this);
        updateImgPresenter.loadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ShowProgress(ShowProgressEvent showProgressEvent) {
        // LinearProgressIndicator在RecyclerView使用有BUG进度会消失，暂时使用该方法进行重新显示
        if (getActivity().isFinishing()) return;
        for (int i=0,size=historyBeans.size(); i<size; i++) {
            THistoryWithFields tHistoryWithFields = historyBeans.get(i);
            long watchProgress = tHistoryWithFields.getWatchProgress();
            long videoDuration = tHistoryWithFields.getVideoDuration();
            LinearProgressIndicator linearProgressIndicator = (LinearProgressIndicator) adapter.getViewByPosition(i, R.id.bottom_progress);
            linearProgressIndicator.setVisibility(watchProgress == 0 ? View.GONE : View.VISIBLE);
            linearProgressIndicator.setMax((int) videoDuration);
            linearProgressIndicator.setProgress((int) watchProgress);
        }
    }

    @Override
    public void successImg(String oldImgUrl, String imgUrl) {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            for (int i=0,size=historyBeans.size(); i<size; i++) {
                if (historyBeans.get(i).getTHistory().getVideoImgUrl().contains(oldImgUrl)) {
                    historyBeans.get(i).getTHistory().setVideoImgUrl(imgUrl);
                    adapter.notifyItemChanged(i);
                    TVideoManager.updateImg(historyBeans.get(i).getVideoId(), imgUrl, 1);
                    break;
                }
            }
        });
    }

    @Override
    public void errorImg() {

    }
}
