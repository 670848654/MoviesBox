package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOWNLOAD;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
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
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DownloadAdapter;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.config.M3U8DownloadConfig;
import my.project.moviesbox.contract.DownloadContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.database.enums.DirectoryTypeEnum;
import my.project.moviesbox.database.manager.TDirectoryManager;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.event.DownloadEvent;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.model.DownloadModel;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.presenter.DownloadPresenter;
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
public class DownloadFragment extends BaseFragment<DownloadModel, DownloadContract.View, DownloadPresenter> implements
        DownloadContract.View, BaseFragment.DirectoryPopupWindowAdapterClickListener {
    private View headerView;
    private Button headerSelectView;
    private Button headerConfigView;
    private View view;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private String directoryId = "";
    private int downloadCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private DownloadAdapter adapter;
    private List<TDownloadWithFields> downloadList = new ArrayList<>();

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
        setDirectoryPopupWindowAdapterClickListener(this);
        initAdapter();
        Aria.download(this).register();
        checkNotCompleteDownloadTask();
        return view;
    }

    @Override
    protected DownloadPresenter createPresenter() {
        return new DownloadPresenter(this);
    }

    @Override
    protected void loadData() {
        downloadCount = TDownloadManager.queryDownloadCountByDirectoryId(directoryId);
        mPresenter.loadDownloadList(isMain, directoryId, downloadList.size(), ConfigManager.getInstance().getDownloadQueryLimit());
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
                                    TDownloadDataManager.updateDownloadState(0, taskId);
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
        headerView = getLayoutInflater().inflate(R.layout.base_header_view, null);
        headerSelectView = headerView.findViewById(R.id.select);
        headerConfigView = headerView.findViewById(R.id.config);
        adapter = new DownloadAdapter(downloadList);
        headerSelectView.setOnClickListener(v -> {
            Utils.setVibration(v);
            List<TDirectory> tDirectories = TDirectoryManager.queryDownloadDirectoryList(false);
            showDirectoryPopupWindow(tDirectories, headerSelectView);
        });
        headerConfigView.setOnClickListener(v -> {
            Utils.setVibration(v);
            Bundle bundle = new Bundle();
            bundle.putString("type", DirectoryTypeEnum.DOWNLOAD.getName());
            bundle.putBoolean("showConfigBtn", true);
            startActivityForResult(new Intent(getActivity(), DirectoryConfigActivity.class).putExtras(bundle), DIRECTORY_REQUEST_CODE);
        });
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
            homeActivity.setAdapterAnimation(adapter);
        adapter.setHeaderView(headerView);
        adapter.setEmptyView(rvView);
        adapter.setHeaderWithEmptyEnable(true);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Bundle bundle = new Bundle();
            bundle.putString("vodTitle", downloadList.get(position).getVideoTitle());
            bundle.putString("downloadId", downloadList.get(position).getTDownload().getDownloadId());
            startActivity(new Intent(getActivity(), DownloadDataActivity.class).putExtras(bundle));
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return false;
            setMenu(view, R.menu.download_menu, -1, item -> {
                switch (item.getItemId()) {
                    case R.id.moveDirectory:
                        Bundle bundle = new Bundle();
                        bundle.putString("type", DirectoryTypeEnum.DOWNLOAD.getName());
                        bundle.putInt("position", position);
                        startActivityForResult(new Intent(getActivity(), DirectoryChangeActivity.class).putExtras(bundle), DIRECTORY_REQUEST_CODE);
                        break;
                }
                return true;
            });
            return true;
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> recyclerView.postDelayed(() -> {
            if (downloadList.size() >= downloadCount) {
                adapter.getLoadMoreModule().loadMoreEnd();
            } else {
                if (isErr) {
                    isMain = false;
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
        loadData();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEnum refresh) {
        if (getActivity().isFinishing()) return;
        if (refresh == REFRESH_DOWNLOAD)
            loadDownloadData();
    }

    @Download.onTaskStop
    protected void stop(DownloadTask downloadTask) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Aria.download(this).unRegister();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent downloadEvent) {
        new Handler().postDelayed(() -> {
            for (int i = 0, size = downloadList.size(); i < size; i++) {
                if (downloadList.get(i).getAriaTaskIds().contains(String.valueOf(downloadEvent.getTaskId()))) {
                    long fileSize = Long.parseLong(TDownloadDataManager.queryDownloadFilesSize(downloadList.get(i).getTDownload().getDownloadId()));
                    downloadList.get(i).setFilesSize((Utils.getNetFileSizeDescription(fileSize)));
                    downloadList.get(i).setNoCompleteSize(TDownloadDataManager.queryDownloadNotCompleteCount(downloadList.get(i).getTDownload().getDownloadId()));
                    adapter.notifyItemChanged(i + adapter.getHeaderLayoutCount() + (adapter.hasEmptyView() ? 1 : 0));
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
        int spanCount = parserInterface.setDownloadListItemSize(Utils.isPad(), isPortrait);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? spanCount : 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIRECTORY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String type = data.getStringExtra("type");
                String selectDirectoryId = data.getStringExtra("directoryId");
                int position = data.getIntExtra("position", 0);
                if (Objects.equals(type, DirectoryTypeEnum.DOWNLOAD.getName())) {
                    if (!Objects.equals(selectDirectoryId, directoryId)) {
                        TDownloadManager.updateDownloadDirectoryId(downloadList.get(position).getTDownload().getDownloadId(), selectDirectoryId);
                        adapter.removeAt(position);
                        downloadCount = TDownloadManager.queryDownloadCountByDirectoryId(directoryId);
                        if (downloadCount == 0) {
                            setRecyclerViewEmpty();
                            rvEmpty(getString(R.string.emptyMyList));
                        }
                    }
                }
            } else if (resultCode == DIRECTORY_CONFIG_RESULT_CODE) {
                TDirectory tDirectory = TDirectoryManager.queryById(directoryId, false);
                if (tDirectory == null) {
                    // 被删除了回到默认清单
                    directoryId = null;
                    List<TDirectory> tDirectories = TDirectoryManager.queryDownloadDirectoryList(false);
                    headerSelectView.setText(tDirectories.get(0).getName());
                } else
                    headerSelectView.setText(tDirectory.getName());
                loadDownloadData();
            }
        }
    }

    @Override
    public void onItemClickListener(TDirectory tDirectory) {
        directoryId = tDirectory.getId();
        headerSelectView.setText(tDirectory.getName());
        loadDownloadData();
        popupWindow.dismiss();
    }
}
