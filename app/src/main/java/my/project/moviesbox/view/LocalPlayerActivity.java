package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.view.GravityCompat;

import java.util.List;

import my.project.moviesbox.adapter.DramaAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.service.DLNAService;
import my.project.moviesbox.utils.Utils;
/**
  * @包名: my.project.moviesbox.view
  * @类名: LocalPlayerActivity
  * @描述: 下载视频本地播放视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:11
  * @版本: 1.0
 */
public class LocalPlayerActivity extends BasePlayerActivity {

    @Override
    protected boolean isLocalVideo() {
        return true;
    }

    @Override
    protected void setActivityName() {
        App.addDestoryActivity(this, "player");
    }

    @Override
    protected void setBundleData(Bundle bundle) {
        localFilePath = bundle.getString("localFilePath");
        vodTitle = bundle.getString("vodTitle");
        dramaTitle = bundle.getString("dramaTitle");
        downloadDataBeans = (List<TDownloadDataWithFields>) bundle.getSerializable("downloadDataBeans");
    }

    @Override
    protected void getNextPlayUrl() {

    }

    @Override
    protected void initCustomData() {
        startService(new Intent(this, DLNAService.class));
        otherView.setVisibility(View.GONE);
    }

    @Override
    protected void setPreNextData() {
        hasPreVideo = clickIndex != 0;
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, dramasItems.get(clickIndex-1).getTitle()) : "");
        hasNextVideo = clickIndex != dramasItems.size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, dramasItems.get(clickIndex+1).getTitle()) : "");
    }

    @Override
    protected void playVideo() {
        toPlay(localFilePath, dramaTitle);
    }

    @Override
    protected void setAdapter() {
        for (TDownloadDataWithFields tDownloadDataWithFields : downloadDataBeans) {
            if (tDownloadDataWithFields.getTDownloadData().getComplete() == 1)
                dramasItems.add(new DetailsDataBean.DramasItem(tDownloadDataWithFields.getTDownloadData().getVideoNumber(), tDownloadDataWithFields.getTDownloadData().getSavePath(), false, tDownloadDataWithFields.getTDownloadData().getDownloadDataId()));
        }
        for (int i=0,size=dramasItems.size(); i<size; i++) {
            if (dramasItems.get(i).getUrl().equals(localFilePath)) {
                clickIndex = i;
                dramasItems.get(i).setSelected(true);
                downloadDataId = dramasItems.get(i).getDownloadDataId();
                break;
            }
        }
        dramaAdapter = new DramaAdapter(this, dramasItems);
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(position);
        });
    }

    @Override
    protected DetailsDataBean.DramasItem setVodDramas(int position) {
        return dramaAdapter.getItem(position);
    }


    @Override
    protected void changeVideo(String title) {

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
}
