package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.view.GravityCompat;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import my.project.moviesbox.adapter.DramaAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.contract.DownloadContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.enums.VideoUrlChangeEnum;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.presenter.DownloadPresenter;
import my.project.moviesbox.service.DLNAService;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: LocalPlayerActivity
  * @描述: 下载视频本地播放视图（以文件列表播放）
  * @作者: Li Z
  * @日期: 2024/2/4 17:11
  * @版本: 1.0
 */
public class LocalListPlayerActivity extends BasePlayerActivity implements DownloadContract.View {
    private boolean isMain = true;
    /**
     * 本地目录ID
     */
    private String directoryId;
    private DownloadPresenter downloadPresenter;
    private long downloadDataCount;

    @Override
    protected boolean isLocalVideo() {
        return true;
    }

    @Override
    protected void setActivityName() {
        App.addDestroyActivity(this, "player");
    }

    @Override
    protected void setBundleData(Bundle bundle) {
        directoryId = bundle.getString("directoryId");
        downloadPresenter = new DownloadPresenter(this);
        downloadDataCount = TDownloadManager.countAllCompletedDownloadDataByDirectoryId(directoryId);
        LogUtil.logInfo("downloadDataCount", downloadDataCount+ "");
    }

    @Override
    protected void getNextPlayUrl() {

    }

    @Override
    protected void initCustomData() {
        startService(new Intent(this, DLNAService.class));
        otherView.setVisibility(View.GONE);
        Jzvd.WIFI_TIP_DIALOG_SHOWED = true;
    }

    @Override
    protected void setPreNextData() {
        hasPreVideo = clickIndex != 0;
        player.preVideo.setText(hasPreVideo ? "上一个视频" : "");
        hasNextVideo = clickIndex != dramaAdapter.getData().size() - 1;
        player.nextVideo.setText(hasNextVideo ? "下一个视频" : "");
    }

    @Override
    protected void playVideo() {}

    @Override
    protected void setAdapter() {
        dramaAdapter = new DramaAdapter(this,true, dramasItems);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
            DetailsDataBean.DramasItem dramasItem = (DetailsDataBean.DramasItem) adapter.getData().get(clickIndex);
            dramasItem.setSelected(false);
            adapter.notifyItemChanged(clickIndex);
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(VideoUrlChangeEnum.CLICK, position);
        });
        dramaAdapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        dramaAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> recyclerView.postDelayed(() -> {
            if (dramaAdapter.getData().size() >= downloadDataCount) {
                //数据全部加载完毕
                dramaAdapter.getLoadMoreModule().loadMoreEnd();
            } else {
                if (isErr) {
                    //成功获取更多数据
                    isMain = false;
                    loadListData();
                } else {
                    //获取更多数据失败
                    isErr = true;
                    dramaAdapter.getLoadMoreModule().loadMoreFail();
                }
            }
        }, 500));
        recyclerView.setAdapter(dramaAdapter);
        loadListData();
    }

    @Override
    protected void changePlayUrl(VideoUrlChangeEnum changeEnum, int position) {
        DetailsDataBean.DramasItem dramasItem = dramasItems.get(clickIndex);
        dramasItem.setSelected(false);
        dramaAdapter.notifyItemChanged(clickIndex);
        super.changePlayUrl(changeEnum, position);
    }

    /**
     * 刷新当前观看的item
     */
    @Override
    public void notifyItemChangedClickIndexData(int clickIndex) {
        DetailsDataBean.DramasItem dramasItem = dramaAdapter.getData().get(clickIndex);
        dramasItem.setSelected(false);
        dramaAdapter.notifyItemChanged(clickIndex);
    }

    private void loadListData() {
        downloadPresenter.loadDownloadDataListByDirectoryId(isMain, directoryId, dramaAdapter.getData().size(), ConfigManager.getInstance().getDownloadQueryLimit());
    }

    @Override
    protected DetailsDataBean.DramasItem getItemByPosition(VideoUrlChangeEnum changeEnum, int position) {
        return dramaAdapter.getItem(position);
    }


    @Override
    protected void parseVideoUrl(String dramaTitle) {

    }

    @Override
    protected String[] getDanmuParams() {
        return new String[0];
    }

    @Override
    public void loadingView() {

    }

    @Override
    public void errorView(String msg) {

    }

    @Override
    public void emptyView() {

    }

    @Override
    protected void retryListener() {

    }

    @Override
    public void downloadList(List<TDownloadWithFields> list) {

    }

    @Override
    public void downloadDataList(List<TDownloadDataWithFields> list) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
            for (TDownloadDataWithFields tDownloadDataWithFields : list) {
                dramasItems.add(
                        new DetailsDataBean.DramasItem(
                                tDownloadDataWithFields.getTDownloadData().getVideoNumber(),
                                tDownloadDataWithFields.getTDownloadData().getSavePath(),
                                false,
                                tDownloadDataWithFields.getTDownloadData().getDownloadDataId(),
                                tDownloadDataWithFields.getVideoTitle(),
                                tDownloadDataWithFields.getVideoImgUrl(),
                                tDownloadDataWithFields.getTDownloadData().getVideoDuration(),
                                tDownloadDataWithFields.getTDownloadData().getVideoFileSize()
                        ));
            }
            if (isMain) {
                player.selectDramaView.setVisibility(View.VISIBLE);
                hideProgress();
                this.dramasItems = dramasItems;
                dramaAdapter.setNewInstance(dramasItems);
                DetailsDataBean.DramasItem dramasItem = dramasItems.get(0);
                clickIndex = 0;
                dramasItem.setSelected(true);
                downloadDataId = dramasItem.getDownloadDataId();
                vodTitle = dramasItem.getVideoTitle();
                localFilePath = dramasItem.getUrl();
                dramaTitle = dramasItem.getTitle();
                playLocalVideo(vodTitle, localFilePath, dramaTitle);
                initPlayerPreNextTag();
            } else {
                dramaAdapter.addData(dramasItems);
                setLoadState(dramaAdapter, true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (!Utils.isNullOrEmpty(downloadPresenter))
            downloadPresenter.detachView();
        super.onDestroy();
    }
}
