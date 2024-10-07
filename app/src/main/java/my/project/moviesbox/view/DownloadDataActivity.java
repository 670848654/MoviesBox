package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOWNLOAD;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_TAB_COUNT;
import static my.project.moviesbox.utils.Utils.DOWNLOAD_SAVE_PATH;
import static my.project.moviesbox.utils.Utils.getArray;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DownloadDataAdapter;
import my.project.moviesbox.config.M3U8DownloadConfig;
import my.project.moviesbox.contract.DownloadContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.event.DownloadEvent;
import my.project.moviesbox.event.DownloadStateEvent;
import my.project.moviesbox.event.RefreshDownloadEvent;
import my.project.moviesbox.model.DownloadModel;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.presenter.DownloadPresenter;
import my.project.moviesbox.service.DownloadService;
import my.project.moviesbox.utils.SAFUtils;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: DownloadDataActivity
  * @描述: 下载子列表数据集合列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:10
  * @版本: 1.0
 */
public class DownloadDataActivity extends BaseActivity<DownloadModel, DownloadContract.View, DownloadPresenter> implements DownloadContract.View {
    private static final int REQUEST_DOCUMENT_TREE = 10000;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private List<TDownloadDataWithFields> downloadDataBeans = new ArrayList<>();
    private DownloadDataAdapter adapter;
    private String downloadId;
    private String vodTitle;
    private int limit = 10;
    private int downloadDataCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private static final String[] DOWNLOAD_STR = getArray(R.array.downloadItems);
    private static final String[] COMPLETE_STR = getArray(R.array.completeItems);
    private static final String[] COMPLETE_EXPORT_STR = getArray(R.array.completeExportItems);
    private static final String[] DOWNLOAD_ERROR_STR = getArray(R.array.downloadErrorItems);
    private File downloadDirOld;
    private File downloadDirNew;
    private ExecutorService executorService;

    @Override
    protected DownloadPresenter createPresenter() {
        return new DownloadPresenter(this);
    }

    @Override
    protected void loadData() {
        downloadDataCount = TDownloadDataManager.queryDownloadDataCount(downloadId);
        mPresenter.loadDownloadDataList(isMain, downloadId, downloadDataBeans.size(), limit);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_vod_list;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        Aria.download(this).register();
        executorService = Executors.newFixedThreadPool(1);
        Bundle bundle = getIntent().getExtras();
        downloadId = bundle.getString("downloadId");
        vodTitle = bundle.getString("vodTitle");
        setToolbar(toolbar, vodTitle, "");
        initSwipe();
        initFab();
        initAdapter();
    }

    private void initSwipe() {
        mSwipe.setEnabled(false);
    }

    private void initFab() {

    }

    private void initAdapter() {
        HttpOption httpOption = new HttpOption();
        HashMap<String, String> headerMap = parserInterface.setPlayerHeaders();
        if (!headerMap.isEmpty())
            httpOption.addHeaders(headerMap);
        adapter = new DownloadDataAdapter(this, downloadDataBeans);
        setAdapterAnimation(adapter);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            long taskId = downloadDataBeans.get(position).getTDownloadData().getAriaTaskId();
            DownloadEntity downloadEntity = Aria.download(this).getDownloadEntity(taskId);
            switch (downloadDataBeans.get(position).getTDownloadData().getComplete()) {
                case 0:
                    // 等待下载状态
                    Utils.showSingleListAlert(this, "", DOWNLOAD_STR, true, (dialogInterface, i) -> {
                        switch (i) {
                            case 0:
                                // 继续下载
                                if (!Utils.isNullOrEmpty(downloadEntity)) {
                                    boolean isM3u8 = downloadEntity.getUrl().contains("m3u8");
                                    if (isM3u8)
                                        Aria.download(this).load(taskId).option(httpOption).m3u8VodOption(new M3U8DownloadConfig().setM3U8Option()).ignoreCheckPermissions().resume();
                                    else
                                        Aria.download(this).load(taskId).option(httpOption).ignoreCheckPermissions().resume();
                                    downloadDataBeans.get(position).getTDownloadData().setComplete(0);
                                    startService(new Intent(this, DownloadService.class));
                                } else
                                    application.showToastMsg(getString(R.string.taskDoesNotExist), DialogXTipEnum.ERROR);
                                break;
                            case 1:
                                // 暂停任务
                                if (!Utils.isNullOrEmpty(downloadEntity)) {
                                    Aria.download(this).load(taskId).ignoreCheckPermissions().stop();
                                    downloadDataBeans.get(position).getTDownloadData().setComplete(0);
                                    adapter.notifyItemChanged(position);
                                    stopService(new Intent(this, DownloadService.class));
                                } else
                                    application.showToastMsg(getString(R.string.taskDoesNotExist), DialogXTipEnum.ERROR);
                                break;
                            case 2:
                                // 删除任务
                                showDeleteDataDialog(downloadDataBeans.get(position), position);
                                break;
                        }
                        dialogInterface.dismiss();
                    });
                    break;
                case 1:
                    String savePath = downloadDataBeans.get(position).getTDownloadData().getSavePath();
                    String videoNumber = downloadDataBeans.get(position).getTDownloadData().getVideoNumber();
                    if (savePath.contains(getFilesDir().getAbsolutePath()))
                        Utils.showSingleListAlert(this, "", COMPLETE_EXPORT_STR, true, (dialogInterface, i) -> {
                            switch (i) {
                                case 0:
                                    playLocalVideo(position);
                                    break;
                                case 1:
                                    Utils.selectVideoPlayer(this, savePath);
                                    break;
                                case 2:
                                    if (SAFUtils.checkHasSetDataSaveUri()) {
                                        application.showToastMsg(videoNumber + " 开始执行导出，请稍后...", DialogXTipEnum.DEFAULT);
                                        executorService.submit(() -> {
                                            SAFUtils.copyConfigFileToSAF(DownloadDataActivity.this, savePath, "video/mp4", false);
                                            Handler uiThread = new Handler(Looper.getMainLooper());
                                            uiThread.post(() -> application.showToastMsg(videoNumber + " 已导出到 "  + SAFUtils.getUriDirectoryName(), DialogXTipEnum.SUCCESS));
                                        });
                                    } else
                                        SAFUtils.showUnauthorizedAlert(
                                                this,
                                                (dialog, which) -> {
                                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                                    startActivityForResult(intent, REQUEST_DOCUMENT_TREE);
                                                });
                                    break;
                                case 3:
                                    showDeleteDataDialog(downloadDataBeans.get(position), position);
                                    break;
                            }
                            dialogInterface.dismiss();
                        });
                    else
                        Utils.showSingleListAlert(this, "", COMPLETE_STR, true, (dialogInterface, i) -> {
                            switch (i) {
                                case 0:
                                    playLocalVideo(position);
                                    break;
                                case 1:
                                    Utils.selectVideoPlayer(this, downloadDataBeans.get(position).getTDownloadData().getSavePath());
                                    break;
                                case 2:
                                    showDeleteDataDialog(downloadDataBeans.get(position), position);
                                    break;
                            }
                        dialogInterface.dismiss();
                    });
                    break;
                case 2:
                    Utils.showSingleListAlert(this, "", DOWNLOAD_ERROR_STR, true, (dialogInterface, i) -> {
                        switch (i) {
                            case 0:
                                if (!Utils.isNullOrEmpty(downloadEntity)) {
                                    boolean isM3u8 = downloadEntity.getUrl().contains("m3u8");
                                    if (isM3u8)
                                        Aria.download(this).load(taskId).option(httpOption).m3u8VodOption(new M3U8DownloadConfig().setM3U8Option()).ignoreCheckPermissions().resume();
                                    else
                                        Aria.download(this).load(taskId).option(httpOption).ignoreCheckPermissions().resume();
                                    downloadDataBeans.get(position).getTDownloadData().setComplete(0);
                                    TDownloadDataManager.updateDownloadState(0, taskId);
                                    startService(new Intent(this, DownloadService.class));
                                } else
                                    application.showToastMsg(getString(R.string.taskDoesNotExist), DialogXTipEnum.ERROR);
                                break;
                            case 1:
                                showDeleteDataDialog(downloadDataBeans.get(position), position);
                                break;
                        }
                        dialogInterface.dismiss();
                    });
            }
        });
        if (Utils.checkHasNavigationBar(this))
            mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (downloadDataBeans.size() >= downloadDataCount) {
                //数据全部加载完毕
                adapter.getLoadMoreModule().loadMoreEnd();
            } else {
                if (isErr) {
                    //成功获取更多数据
                    isMain = false;
                    loadData();
                } else {
                    //获取更多数据失败
                    isErr = true;
                    adapter.getLoadMoreModule().loadMoreFail();
                }
            }
        }, 500));
        mRecyclerView.setAdapter(adapter);
        adapter.setEmptyView(rvView);
    }

    private void playLocalVideo(int position) {
        Bundle bundle = new Bundle();
//        bundle.putString("localFilePath", encodeURL(downloadDataBeans.get(position).getTDownloadData().getSavePath()));
        bundle.putString("localFilePath", downloadDataBeans.get(position).getTDownloadData().getSavePath());
        bundle.putString("vodTitle", vodTitle);
        bundle.putString("dramaTitle", downloadDataBeans.get(position).getTDownloadData().getVideoNumber());
        bundle.putSerializable("downloadDataBeans", (Serializable) downloadDataBeans);
        startActivity(new Intent(this, LocalPlayerActivity.class).putExtras(bundle));
    }

    private void showDeleteDataDialog(TDownloadDataWithFields tDownloadDataWithFields, int position) {
        Utils.showAlert(
                this,
                getString(R.string.otherOperation),
                getString(R.string.deleteSingleDownloadDialogSubContent),
                true,
                getString(R.string.deleteSingleDownloadDialogPositiveBtnText),
                getString(R.string.defaultNegativeBtnText),
                getString(R.string.deleteSingleDownloadDialogNeutralBtnText),
                (dialog, which) -> {
                    deleteData(false, tDownloadDataWithFields, position);
                },
                (dialog, which) -> {
                    dialog.dismiss();
                },
                (dialog, which) -> {
                    deleteData(true, tDownloadDataWithFields, position);
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DOCUMENT_TREE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // 保存授权的目录
                SharedPreferencesUtils.setDataSaveUri(uri.toString());
                application.showToastMsg("已授权 "+ uri, DialogXTipEnum.SUCCESS);
            }
        }
    }

    private void deleteData(boolean removeFile, TDownloadDataWithFields tDownloadDataWithFields, int position) {
        TDownloadDataManager.deleteDownloadData(tDownloadDataWithFields.getTDownloadData().getDownloadDataId());
        String savePath = tDownloadDataWithFields.getTDownloadData().getSavePath();
        if (Utils.isNullOrEmpty(savePath)) {
            // 下载位置为空
            removeAdapterByPosition(position);
            Aria.download(this).load(tDownloadDataWithFields.getTDownloadData().getAriaTaskId()).ignoreCheckPermissions().cancel(false);
        } else {
            // 旧版本的路劲
            String downloadPathOld = savePath.contains("MoviesBox") ?
                    String.format(DOWNLOAD_SAVE_PATH, SourceEnum.getTitleBySource(tDownloadDataWithFields.getVideoSource()), tDownloadDataWithFields.getVideoTitle())
                    :
                    this.getFilesDir().getAbsolutePath()+String.format("/%s/%s", SourceEnum.getTitleBySource(tDownloadDataWithFields.getVideoSource()), tDownloadDataWithFields.getVideoTitle());
            downloadDirOld = new File(downloadPathOld);
            // 新版路劲
            String downloadPathDirName = Utils.getHashedFileName(tDownloadDataWithFields.getVideoTitle());
            String downloadPathNew = savePath.contains("MoviesBox") ?
                    String.format(DOWNLOAD_SAVE_PATH, SourceEnum.getTitleBySource(tDownloadDataWithFields.getVideoSource()), downloadPathDirName)
                    :
                    this.getFilesDir().getAbsolutePath()+String.format("/%s/%s", SourceEnum.getTitleBySource(tDownloadDataWithFields.getVideoSource()), downloadPathDirName);
            downloadDirNew = new File(downloadPathNew);
            if (tDownloadDataWithFields.getTDownloadData().getAriaTaskId() == -1) {
                // -1直接删除
                removeAdapterByPosition(position);
            } else {
                if (!downloadDirOld.exists() && !downloadDirNew.exists()) {
                    removeAdapterByPosition(position);
                } else {
                    // 获取所有下载任务
                    List<DownloadEntity> list = Aria.download(this).getTaskList();
                    // 判断任务列表是否存在，当应用卸载重装时为NULL会报错
                    if (list != null && list.size() > 0) {
                        for (DownloadEntity entity : list) {
                            // 未下载完成
                            if (tDownloadDataWithFields.getTDownloadData().getAriaTaskId() != -99 && tDownloadDataWithFields.getTDownloadData().getAriaTaskId() == entity.getId()) {
                                // 从Aria数据库中删除任务
                                Aria.download(this).load(entity.getId()).ignoreCheckPermissions().cancel(false);
                                break;
                            }
                        }
                    }
                    // 是否删除文件
                    deleteDownloadData(removeFile, tDownloadDataWithFields, position);
                }
            }
        }
        downloadDataCount = TDownloadDataManager.queryDownloadDataCount(downloadId);
        if (downloadDataBeans.size() == 0) {
            shouldDeleteDownloadDir();
            TDownloadManager.deleteDownload(downloadId);
            finish();
        }
        EventBus.getDefault().post(REFRESH_DOWNLOAD);
        EventBus.getDefault().post(REFRESH_TAB_COUNT);
    }

    /**
     * 删除数据
     * @param removeFile
     * @param tDownloadDataWithFields
     * @param position
     */
    private void deleteDownloadData(boolean removeFile, TDownloadDataWithFields tDownloadDataWithFields, int position) {
        String imgPath = tDownloadDataWithFields.getVideoImgUrl();
        // 如果已完成的任务
        if (tDownloadDataWithFields.getTDownloadData().getComplete() == 1 && removeFile) {
            File mp4File = new File(tDownloadDataWithFields.getTDownloadData().getSavePath());
            if (mp4File.exists()) mp4File.delete();
            File m3u8File = new File(tDownloadDataWithFields.getTDownloadData().getSavePath().replaceAll("mp4", "m3u8"));
            if (m3u8File.exists()) m3u8File.delete();
            // 删除封面
            if (!Utils.isNullOrEmpty(imgPath) && imgPath.contains("cover_")) {
                File imgFile = new File(imgPath);
                if (imgFile.exists()) imgFile.delete();
            }
        }
        removeAdapterByPosition(position);
    }

    private void removeAdapterByPosition(int position) {
        runOnUiThread(() -> {
            application.showToastMsg(getString(R.string.taskDeletedMsg), DialogXTipEnum.SUCCESS);
            adapter.removeAt(position);
        });
    }

    /**
     * 是否应该删除下载主目录
     */
    private void shouldDeleteDownloadDir() {
        try {
            // 文件夹下没有任何文件才删除主目录
            if (downloadDirOld.list().length == 0)
                downloadDirOld.delete();
            if (downloadDirNew.list().length == 0)
                downloadDirNew.delete();
        } catch (Exception e) {}
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.getLoadMoreModule().loadMoreComplete();
    }

    private void loadDownloadData() {
        isMain = true;
        mPresenter = createPresenter();
        loadData();
    }

    @Download.onTaskRunning
    protected void running(DownloadTask downloadTask) {
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getTDownloadData().getAriaTaskId() == downloadTask.getEntity().getId() &&
                    downloadTask.getTaskName().contains(downloadDataBeans.get(i).getTDownloadData().getVideoNumber())) {
                TextView number = (TextView) adapter.getViewByPosition(i, R.id.number);
                if (number != null)
                    number.setText(downloadTask.getConvertSpeed() == null ? "0kb/s" : downloadTask.getConvertSpeed());
                TextView state = (TextView) adapter.getViewByPosition(i, R.id.state);
                if (state != null) {
                    state.setText(Html.fromHtml("<font color=\"#FF5722\">"+Utils.getString(R.string.downloadingState)+"</font>"));
                }
                TextView fileSize = (TextView) adapter.getViewByPosition(i, R.id.file_size);
                if (fileSize != null) {
                    if (fileSize.getVisibility() != View.VISIBLE)
                        fileSize.setVisibility(View.VISIBLE);
                    fileSize.setText(Utils.getNetFileSizeDescription(downloadTask.getEntity().getFileSize()));
                }
//                LinearProgressIndicator linearProgressIndicator = (LinearProgressIndicator) adapter.getViewByPosition(i, R.id.bottom_progress);
                ProgressBar linearProgressIndicator = (ProgressBar) adapter.getViewByPosition(i, R.id.bottom_progress);
                if (linearProgressIndicator != null) {
                    if (linearProgressIndicator.getVisibility() != View.VISIBLE) linearProgressIndicator.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        linearProgressIndicator.setProgress(downloadTask.getPercent(), true);
                    else
                        linearProgressIndicator.setProgress(downloadTask.getPercent());
                }
            }
        }
    }

    /*@Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        Log.e("Activity onTaskComplete", downloadTask.getTaskName() + "，下载完成");
        String title = (String) DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId()).get(0);
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getAnimeTitle().equals(title) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getPlayNumber())) {
                downloadDataBeans.get(i).setComplete(1);
                String path = downloadTask.getFilePath();
                if (path.contains("m3u8")) {
                    path = path.replaceAll("m3u8", "mp4");
                    File file = new File(path);
                    downloadDataBeans.get(i).setFileSize(file == null ? 0 : file.length());
                    downloadDataBeans.get(i).setPath(path);
                } else {
                    downloadDataBeans.get(i).setFileSize(downloadTask.getFileSize());
                    downloadDataBeans.get(i).setPath(downloadTask.getFilePath());
                }
                adapter.notifyItemChanged(i);
                DatabaseUtil.updateDownloadSuccess((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
                break;
            }
        }
    }*/

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        shouldDeleteDownloadDir();
        EventBus.getDefault().post(REFRESH_DOWNLOAD);
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask) {
        try {
            List<Object> objects = TVideoManager.queryDownloadVodInfo(downloadTask.getEntity().getId());
            for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
                if (downloadDataBeans.get(i).getVideoTitle().equals(objects.get(0)) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getTDownloadData().getVideoNumber())) {
                    downloadDataBeans.get(i).getTDownloadData().setComplete(2);
                    TextView number = (TextView) adapter.getViewByPosition(i, R.id.number);
                    if (number != null)
                        number.setText("");
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
            EventBus.getDefault().post(REFRESH_DOWNLOAD);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Utils.showAlert(this,
                    getString(R.string.taskOperationFailedTitle),
                    getString(R.string.taskOperationFailedContent),
                    false,
                    getString(R.string.defaultPositiveBtnText),
                    "",
                    "",
                    (DialogInterface.OnClickListener) (dialog, which) -> dialog.dismiss(),
                    null,
                    null);
        }
    }

    @Override
    public void onDestroy() {
        Aria.download(this).unRegister();
        EventBus.getDefault().unregister(this);
        if (!executorService.isShutdown())
            executorService.shutdownNow();
        super.onDestroy();
    }

    @Override
    protected void initBeforeView() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshDownloadEvent refreshDownloadData) {
        for (int i=0,size=downloadDataBeans.size(); i<size; i++) {
            if (downloadDataBeans.get(i).getTDownloadData().getDownloadDataId().equals(refreshDownloadData.getId())) {
                downloadDataBeans.get(i).getTDownloadData().setWatchProgress(refreshDownloadData.getPlayPosition());
                downloadDataBeans.get(i).getTDownloadData().setVideoDuration(refreshDownloadData.getVideoDuration());
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent downloadEvent) {
        new Handler().postDelayed(() -> {
            for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
                if (downloadDataBeans.get(i).getTDownloadData().getAriaTaskId() == downloadEvent.getTaskId() &&
                        downloadEvent.getDrama().contains(downloadDataBeans.get(i).getTDownloadData().getVideoNumber())) {
                    downloadDataBeans.get(i).getTDownloadData().setComplete(downloadEvent.getComplete());
                    TextView number = (TextView) adapter.getViewByPosition(i, R.id.number);
                    if (number != null)
                        number.setText("");
                    if (downloadEvent.getComplete() == 1) {
                        String path = downloadEvent.getFilePath();
                        if (path.contains("m3u8")) {
                            path = path.replaceAll("m3u8", "mp4");
                            File file = new File(path);
                            downloadDataBeans.get(i).getTDownloadData().setVideoFileSize(file == null ? 0 : file.length());
                            downloadDataBeans.get(i).getTDownloadData().setSavePath(path);
                        } else {
                            downloadDataBeans.get(i).getTDownloadData().setVideoFileSize(downloadEvent.getFileSize());
                            downloadDataBeans.get(i).getTDownloadData().setSavePath(downloadEvent.getFilePath());
                        }
                    }
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }, 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadStateEvent(DownloadStateEvent downloadStateEvent) {
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getTDownloadData().getAriaTaskId() == downloadStateEvent.getTaskId() &&
                    downloadStateEvent.getDrama().contains(downloadDataBeans.get(i).getTDownloadData().getVideoNumber())) {
                downloadDataBeans.get(i).getTDownloadData().setComplete(3);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    protected void setConfigurationChanged() {
        if (downloadDataBeans.size() == 0) return;
        setRecyclerViewView();
    }

    @Override
    protected void retryListener() {

    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, parserInterface.setDownloadDataListItemSize(Utils.isPad(), isPortrait)));
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void loadingView() {
        rvLoading();
    }

    @Override
    public void errorView(String msg) {
        if (isFinishing()) return;
        setLoadState(false);
        runOnUiThread(() -> {
            if (isMain) {
                setRecyclerViewEmpty();
                rvEmpty(msg);
            }
        });
    }

    @Override
    public void emptyView() {

    }

    @Override
    public void downloadList(List<TDownloadWithFields> list) {

    }

    @Override
    public void downloadDataList(List<TDownloadDataWithFields> list) {
        if (isFinishing()) return;
        setLoadState(true);
        runOnUiThread(() -> {
            if (isMain) {
                hideProgress();
                downloadDataBeans = list;
                setRecyclerViewView();
                adapter.setNewInstance(downloadDataBeans);
            } else
                adapter.addData(list);
        });
    }
}
