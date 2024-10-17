package my.project.moviesbox.view;

import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.fromIndex;

import android.os.Bundle;
import android.os.Handler;

import androidx.core.view.GravityCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DramaAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.contract.VideoContract;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.enums.VideoUrlChangeEnum;
import my.project.moviesbox.event.DramaEvent;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.presenter.VideoPresenter;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoUtils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: PlayerActivity
  * @描述: 影视在线播放视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:12
  * @版本: 1.0
 */
public class PlayerActivity extends BasePlayerActivity implements VideoContract.View {

    @Override
    protected boolean isLocalVideo() {
        return false;
    }

    @Override
    protected void setActivityName() {
        App.addDestroyActivity(this, "player");
    }

    @Override
    protected void setBundleData(Bundle bundle) {
        EventBus.getDefault().register(this);
        url = bundle.getString("url");
        dramaTitle = bundle.getString("dramaTitle");
        vodTitle = bundle.getString("vodTitle");
        dramaUrl = bundle.getString("dramaUrl");
        dramasItems = (List<DetailsDataBean.DramasItem>) bundle.getSerializable("list");
        clickIndex = bundle.getInt("clickIndex");
        vodId = bundle.getString("vodId");
        nowSource = bundle.getInt("nowSource");
    }

    @Override
    protected void getNextPlayUrl() {
        LogUtil.logInfo("开始获取下一集播放地址", "");
        videoPresenter.loadData(true, vodTitle, dramasItems.get(clickIndex+1).getUrl(),
                nowSource, dramasItems.get(clickIndex+1).getTitle());
    }

    @Override
    protected void setPreNextData() {
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, dramasItems.get(clickIndex-1).getTitle()) : "");
        hasNextVideo = clickIndex != dramasItems.size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, dramasItems.get(clickIndex+1).getTitle()) : "");
    }

    @Override
    protected void playVideo() {
        playNetworkVideo(url);
    }

    @Override
    protected void setAdapter() {
        dramaAdapter = new DramaAdapter(this, dramasItems);
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(VideoUrlChangeEnum.CLICK, position);
        });
    }

    @Override
    protected DetailsDataBean.DramasItem getItemByPosition(VideoUrlChangeEnum changeEnum, int position) {
        switch (changeEnum) {
            case CLICK:
            case PRE:
                alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
                break;
            case NEXT:
                if (Utils.isNullOrEmpty(nextPlayUrl))
                    alertDialog = Utils.getProDialog(this, R.string.parseVodPlayUrl);
                break;
        }
        EventBus.getDefault().post(new DramaEvent(nowSource, position));
        return dramaAdapter.getItem(position);
    }

    protected DetailsDataBean.DramasItem getItemByPosition(int position) {
        return dramaAdapter.getItem(position);
    }

    @Override
    protected void parseVideoUrl(String dramaTitle) {
        videoPresenter.loadData(false, vodTitle, dramaUrl, nowSource, dramaTitle);
    }

    @Override
    protected String[] getDanmuParams() {
        if (Utils.isNullOrEmpty(dramasItems))
            return null;
        int interfaceSource = parserInterface.getSource();
        SourceEnum.SourceIndexEnum sourceEnum = fromIndex(interfaceSource);
        switch (sourceEnum) {
            case SILISILI:
                // 嘶哩嘶哩弹幕参数 [0]:影视标题 [1]:影视集数下标
                return new String[]{vodTitle, String.valueOf(dramasItems.get(clickIndex).getIndex())};
            case ANFUNS:
                // AnFuns弹幕参数 [0]:影视播放地址url中的影视ID [1]:影视集数下标
                String regex = "([0-9]+)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(dramaUrl);
                StringBuilder stringBuilder = new StringBuilder();
                if (matcher.find())
                    stringBuilder.append(matcher.group());
                return new String[]{stringBuilder.toString(), String.valueOf(dramasItems.get(clickIndex).getIndex())};
            case YJYS:
            case NYYY:
                // 缘觉影视 | 纽约影视 弹幕参数 [0] 播放页地址
                return new String[]{dramaUrl};
            default:
                return null;
        }
    }

    @Override
    protected void initCustomData() {
        videoPresenter = new VideoPresenter(this);
    }

    @Override
    public void cancelDialog() {Utils.cancelDialog(alertDialog);}

    @Override
    public void successPlayUrl(List<DialogItemBean> urls) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            hideNavBar();
            cancelDialog();
            if (urls.size() == 1)
                playNetworkVideo(urls.get(0).getUrl());
            else
                alertDialog = VideoUtils.showMultipleVideoSources(this,
                        urls,
                        (adapter, view, position) -> {
                            playNetworkVideo(urls.get(position).getUrl());
                            alertDialog.dismiss();
                        },
                         true);
        });
    }

    @Override
    public void errorPlayUrl() {
        if (isFinishing()) return;
        //解析出错
        runOnUiThread(() -> {
            player.onStateError();
            hideNavBar();
            if (SharedPreferencesUtils.getEnableSniff()) {
                alertDialog = Utils.getProDialog(this, R.string.sniffVodPlayUrl);
                VideoUtils.startSniffing(dramaUrl, VideoSniffEvent.ActivityEnum.PLAYER, VideoSniffEvent.SniffEnum.PLAY);
            } else {
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
            }
        });
    }

    @Override
    public void errorNet(String msg) {
        if (isFinishing()) return;
        //网络出错
        runOnUiThread(() -> {
            player.onStateError();
            hideNavBar();
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
        });
    }

    @Override
    public void successDramasList(List<DetailsDataBean.DramasItem> dramasItems) {
        if (isFinishing()) return;
        this.dramasItems = dramasItems;
        runOnUiThread(() -> dramaAdapter.notifyDataSetChanged());
    }

    @Override
    public void errorDramasList() {
        if (isFinishing()) return;
        runOnUiThread(() -> application.showToastMsg(getString(R.string.getPlaylistErrorMsg), DialogXTipEnum.ERROR));
    }

    @Override
    public void successOnlyPlayUrl(List<DialogItemBean> urls) {
        if (isFinishing()) return;
        nextPlayUrl = urls;
        LogUtil.logInfo("获取下一集播放地址成功", urls.toString());
    }

    @Override
    public void errorOnlyPlayUrl() {
        if (isFinishing()) return;
        if (nextPlayUrl.size() > 0) return;
        runOnUiThread(() -> {
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++;
                // 延迟指定时间后重试
                new Handler().postDelayed(() -> {
                    if (SharedPreferencesUtils.getEnableSniff())
                        VideoUtils.startSniffing(dramasItems.get(clickIndex+1).getUrl(), VideoSniffEvent.ActivityEnum.PLAYER, VideoSniffEvent.SniffEnum.NEXT_PLAY);
                    else
                        getNextPlayUrl();
                }, RETRY_DELAY_MILLIS);
            } else {
                // 达到最大重试次数，不再执行
                application.showToastMsg(getString(R.string.getNextEpisodeFailedMoreThan3Times), DialogXTipEnum.ERROR);
            }
        });
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSniff(VideoSniffEvent event) {
        if (isFinishing()) return;
        if (event.getActivityEnum() == VideoSniffEvent.ActivityEnum.PLAYER) {
            cancelDialog();
            List<DialogItemBean> urls = event.getUrls();
            boolean success = event.isSuccess();
            switch (event.getSniffEnum()) {
                case PLAY:
                    if (success)
                        successPlayUrl(urls);
                    else
                        VideoUtils.sniffErrorDialog(this);
                    break;
                case NEXT_PLAY:
                    if (success) {
                        nextPlayUrl = urls;
                        LogUtil.logInfo("获取下一集播放地址成功", urls.toString());
                    } else
                        errorOnlyPlayUrl();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
