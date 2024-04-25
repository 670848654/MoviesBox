package my.project.moviesbox.view;

import static my.project.moviesbox.view.BaseActivity.ADAPTER_SCALE_IN_ANIMATION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DownloadAdapter;
import my.project.moviesbox.config.M3U8DownloadConfig;
import my.project.moviesbox.contract.DownloadContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.event.DownloadEvent;
import my.project.moviesbox.event.RefreshEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.presenter.DownloadPresenter;
import my.project.moviesbox.presenter.UpdateImgPresenter;
import my.project.moviesbox.service.DownloadService;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: DownloadFragment
  * @描述: 下载列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:10
  * @版本: 1.0
 */
public class DownloadFragment extends BaseFragment<DownloadContract.View, DownloadPresenter> implements
        DownloadContract.View {
    private View view;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    CoordinatorLayout msg;
    private int limit = 10;
    private int downloadCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private DownloadAdapter adapter;
    private List<TDownloadWithFields> downloadList = new ArrayList<>();
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
        initAdapter();
        Aria.download(this).register();
        checkNotCompleteDownloadTask();
        return view;
    }

    @Override
    protected DownloadPresenter createPresenter() {
        return new DownloadPresenter(downloadList.size(), limit, this);
    }

    @Override
    protected void loadData() {
        downloadCount = TDownloadManager.queryDownloadCount();
        mPresenter.loadDownloadList(isMain);
    }

    @Override
    protected void retryListener() {

    }

    /**
     * 是否存在未下载完成的任务
     */
    private void checkNotCompleteDownloadTask() {
        new Handler().postDelayed(() -> {
            if (!Utils.isServiceRunning(getContext(), DownloadService.class)) {
                List<DownloadEntity> list = Aria.download(this).getAllNotCompleteTask();
                if (list != null && list.size() > 0) {
                    for (DownloadEntity d : list) {
                        LogUtil.logInfo("savePath", d.getFilePath());
                    }
                    Utils.showAlert(getActivity(),
                            getString(R.string.downloadTaskOperationTitle),
                            String.format(getString(R.string.downloadTaskOperationContent), list.size()),
                            false,
                            getString(R.string.resumeDownloadDialogSubPositiveBtnText),
                            getString(R.string.resumeDownloadDialogSubNegativeBtnText),
                            null,
                            (dialogInterface, i) -> {
                                getActivity().startService(new Intent(getActivity(), DownloadService.class));
                                for (DownloadEntity entity : list) {
                                    long taskId = entity.getId();
                                    boolean isM3u8 = entity.getUrl().contains("m3u8");
                                    if (isM3u8)
                                        Aria.download(this).load(taskId).m3u8VodOption(new M3U8DownloadConfig().setM3U8Option()).ignoreCheckPermissions().resume();
                                    else
                                        Aria.download(this).load(taskId).ignoreCheckPermissions().resume();
                                    LogUtil.logInfo("恢复下载任务，资源类型为", isM3u8 ? "M3U8" : "MP4");
                                    TDownloadDataManager.updateDownloadState(taskId);
                                    getActivity().startService(new Intent(getActivity(), DownloadService.class));
                                }
                            },
                            (dialogInterface, i) -> dialogInterface.dismiss(),
                            null);
                }
            }
        }, 500);
    }

    @SuppressLint("RestrictedApi")
    private void initAdapter() {
        adapter = new DownloadAdapter(getActivity(), downloadList);
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
            homeActivity.setAdapterAnimation(adapter, ADAPTER_SCALE_IN_ANIMATION, true);
        adapter.setEmptyView(rvView);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Bundle bundle = new Bundle();
            bundle.putString("vodTitle", downloadList.get(position).getVideoTitle());
            bundle.putString("downloadId", downloadList.get(position).getTDownload().getDownloadId());
            startActivity(new Intent(getActivity(), DownloadDataActivity.class).putExtras(bundle));
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> recyclerView.postDelayed(() -> {
            if (downloadList.size() >= downloadCount) {
                adapter.getLoadMoreModule().loadMoreEnd();
            } else {
                if (isErr) {
                    isMain = false;
                    mPresenter = new DownloadPresenter(downloadList.size(), limit, this);
                    loadData();
                } else {
                    isErr = true;
                    adapter.getLoadMoreModule().loadMoreFail();
                }
            }
        }, 500));
        if (Utils.checkHasNavigationBar(getActivity())) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.getLoadMoreModule().loadMoreComplete();
    }

    private void loadDownloadData() {
        isMain = true;
        downloadList.clear();
        mPresenter = createPresenter();
        loadData();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent refresh) {
        if (getActivity().isFinishing()) return;
        if (refresh.getIndex() == 3)
            loadDownloadData();
    }

    @Download.onTaskRunning
    protected void running(DownloadTask downloadTask) {
        /*for (int i = 0, size = downloadList.size(); i < size; i++) {
            *//*List<Object> list = TVideoManager.queryDownloadVodInfo(downloadTask.getEntity().getId());
            String title = (String) list.get(0);*//*
//            if (downloadList.get(i).getVideoTitle().equals(title)) {
            if (downloadList.get(i).getAriaTaskIds().contains(String.valueOf(downloadTask.getEntity().getId()))) {
                TextView number = (TextView) adapter.getViewByPosition(i, R.id.number);
                if (number != null) {
                    String speed = downloadTask.getConvertSpeed() == null ? "0kb/s" : downloadTask.getConvertSpeed();
                    number.setText("正在下载" + downloadTask.getTaskName().replaceAll(".mp4", "").replaceAll(".m3u8", "")  + "\n" + speed);
                }
                TextView fileSize = (TextView) adapter.getViewByPosition(i, R.id.file_size);
                if (fileSize != null) {
                    if (fileSize.getVisibility() != View.VISIBLE) fileSize.setVisibility(View.VISIBLE);
                    fileSize.setText("大小:" +Utils.getNetFileSizeDescription(downloadTask.getEntity().getFileSize()));
                }
                LinearProgressIndicator linearProgressIndicator = (LinearProgressIndicator) adapter.getViewByPosition(i, R.id.bottom_progress);
                if (linearProgressIndicator != null) {
                    if (linearProgressIndicator.getVisibility() != View.VISIBLE) linearProgressIndicator.setVisibility(View.VISIBLE);
                    linearProgressIndicator.setProgress(downloadTask.getPercent());
                }
            }
        }*/
    }

    @Download.onTaskStop
    protected void stop(DownloadTask downloadTask) {
        adapter.notifyDataSetChanged();
    }

    /*@Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
//        JSONObject obj = JSONObject.parseObject(Aria.download(this).load(downloadTask.getEntity().getId()).getExtendField());
        new Handler().postDelayed(() -> {
            for (int i = 0, size = downloadList.size(); i < size; i++) {
                String title = (String) DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId()).get(0);
                if (downloadList.get(i).getAnimeTitle().equals(title)) {
                    downloadList.get(i).setFilesSize(DatabaseUtil.queryDownloadFilesSize(downloadList.get(i).getDownloadId()));
                    downloadList.get(i).setNoCompleteSize(DatabaseUtil.queryDownloadNotCompleteCount(downloadList.get(i).getDownloadId()));
                    adapter.notifyItemChanged(i);
                    DatabaseUtil.updateDownloadSuccess((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
                    break;
                }
            }
        }, 1000);
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != updateImgPresenter) updateImgPresenter.detachView();
        Aria.download(this).unRegister();
    }

    /*@Override
    public void showSuccessImg(String oldImgUrl, String imgUrl) {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
            for (int i=0,size=downloadList.size(); i<size; i++) {
                if (downloadList.get(i).getImgUrl().contains(oldImgUrl)) {
                    downloadList.get(i).setImgUrl(imgUrl);
                    adapter.notifyItemChanged(i);
                    DatabaseUtil.updateImg(downloadList.get(i).getDownloadId(), imgUrl, 2);
                    break;
                }
            }
        });
    }*/

    /*@Override
    public void showErrorImg(String msg) {
        if (mActivityFinish) return;
        runOnUiThread(() -> CustomToast.showToast(this, msg, CustomToast.ERROR));
    }*/

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgBean updateImgBean) {
        if (mActivityFinish) return;
        updateImgPresenter = new UpdateImgPresenter(updateImgBean.getOldImgUrl(), updateImgBean.getDescUrl(), this);
        updateImgPresenter.loadData();
    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent downloadEvent) {
        new Handler().postDelayed(() -> {
            for (int i = 0, size = downloadList.size(); i < size; i++) {
                if (downloadList.get(i).getAriaTaskIds().contains(String.valueOf(downloadEvent.getTaskId()))) {
//                if (downloadList.get(i).getVideoTitle().equals(downloadEvent.getTitle())) {
                    long fileSize = Long.parseLong(TDownloadDataManager.queryDownloadFilesSize(downloadList.get(i).getTDownload().getDownloadId()));
                    downloadList.get(i).setFilesSize((Utils.getNetFileSizeDescription(fileSize)));
                    downloadList.get(i).setNoCompleteSize(TDownloadDataManager.queryDownloadNotCompleteCount(downloadList.get(i).getTDownload().getDownloadId()));
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }, 1000);
    }

    private void setRecyclerViewEmpty() {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
    }

    private void setRecyclerViewView() {
        position = recyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), parserInterface.setDownloadListItemSize(Utils.isPad(), isPortrait)));
        recyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void loadingView() {
        if (getActivity().isFinishing()) return;
        downloadList.clear();
        adapter.notifyDataSetChanged();
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
    public void downloadList(List<TDownloadWithFields> list) {
        if (getActivity().isFinishing()) return;
        setLoadState(true);
        getActivity().runOnUiThread(() -> {
            if (isMain) {
                new Handler().postDelayed(() -> {
                    hideProgress();
                    downloadList = list;
                    setRecyclerViewView();
                    adapter.setNewInstance(downloadList);
                    setRecyclerViewView();
                }, 500);
            } else
                adapter.addData(list);
        });
    }

    @Override
    public void downloadDataList(List<TDownloadDataWithFields> list) {

    }

    /*
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgEvent updateImgEvent) {
        if (getActivity().isFinishing()) return;
        updateImgPresenter = new UpdateImgPresenter(updateImgEvent.getOldImgUrl(), updateImgEvent.getDescUrl(), this);
        updateImgPresenter.loadData();
    }

    @Override
    public void successImg(String oldImgUrl, String imgUrl) {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            for (int i=0,size=downloadList.size(); i<size; i++) {
                if (downloadList.get(i).getTDownload().getVideoImgUrl().contains(oldImgUrl)) {
                    downloadList.get(i).getTDownload().setVideoImgUrl(imgUrl);
                    adapter.notifyItemChanged(i);
                    TVideoManager.updateImg(downloadList.get(i).getTDownload().getDownloadId(), imgUrl, 2);
                    break;
                }
            }
        });
    }

    @Override
    public void errorImg() {

    }*/
}
