package my.project.moviesbox.utils;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOWNLOAD;
import static my.project.moviesbox.utils.Utils.DOWNLOAD_SAVE_PATH;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.HttpOption;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.DownloadDramaAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.bean.DownloadDramaBean;
import my.project.moviesbox.config.M3U8DownloadConfig;
import my.project.moviesbox.database.manager.TDownloadDataManager;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.service.DownloadService;

/**
 * @author Li
 * @version 1.0
 * @description: 抽离下载方法
 * @date 2025/6/23 9:27
 */
public class DownloadUtils {
    private final WeakReference<Activity> activityRef;
    private String videoId; // 影视ID
    private final WeakReference<DownloadDramaAdapter> adapterRef;
    private final WeakReference<BottomSheetDialog> dialogRef;
    private String savePath; // 下载保存路劲
    private final ParserInterface parserInterface = ParserInterfaceFactory.getParserInterface();

    public DownloadUtils(Activity activity, String videoId, DownloadDramaAdapter adapter, BottomSheetDialog dialog) {
        this.activityRef = new WeakReference<>(activity);
        this.videoId = videoId;
        this.adapterRef = new WeakReference<>(adapter);
        this.dialogRef = new WeakReference<>(dialog);
    }

    public void updateVodId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * 下载弹窗
     */
    public void select2Download() {
        DownloadDramaAdapter adapter = adapterRef.get();
        if (adapter == null || adapter.getData().isEmpty()) {
            App.getInstance().showToastMsg("无可下载列表", DialogXTipEnum.ERROR);
            return;
        }
        if (!Utils.isWifiConnected()) {
            Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                Utils.showAlert(activity,
                        Utils.getString(R.string.noWifiDialogTitle),
                        Utils.getString(R.string.noWifiDialogContent),
                        false,
                        Utils.getString(R.string.defaultPositiveBtnText),
                        Utils.getString(R.string.defaultNegativeBtnText),
                        "",
                        (dialog, which) -> showDownloadBsd(),
                        (dialog, which) -> dialog.dismiss(),
                        null);
            }
        } else
            showDownloadBsd();
    }

    /**
     * 显示下载弹出窗
     */
    private void showDownloadBsd() {
        checkHasDownload();
        BottomSheetDialog dialog = dialogRef.get();
        if (dialog != null && !dialog.isShowing()) {
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            dialog.show();
        }
    }

    /**
     * 检查是否在下载任务中
     */
    public void checkHasDownload() {
        DownloadDramaAdapter adapter = adapterRef.get();
        if (adapter == null) return;
        List<DownloadDramaBean> data = adapter.getData();
        for (int i=0,size=data.size(); i<size; i++) {
            int complete = TDownloadManager.queryDownloadDataIsDownloadError(videoId, data.get(i).getTitle(), 0);
            switch (complete) {
                case -1:
                case 2:
                    data.get(i).setHasDownload(false);
                    break;
                default:
                    data.get(i).setHasDownload(true);
                    break;
            }
            adapter.notifyItemChanged(i);
        }
    }

    /**
     * 创建下载保存目录
     * @param detailsTitle 影视标题
     */
    public void createDownloadConfig(String detailsTitle) {
        String fileName = Utils.getHashedFileName(detailsTitle);
        savePath = String.format(DOWNLOAD_SAVE_PATH, ParserInterfaceFactory.getParserInterface().getSourceName() , fileName);
        if (SAFUtils.canReadDownloadDirectory())
            Utils.createDataFolder(savePath);
        else {
            Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                savePath = activity.getFilesDir().getAbsolutePath()+String.format(DOWNLOAD_SAVE_PATH, ParserInterfaceFactory.getParserInterface().getSourceName() , fileName);
                Utils.createDataFolder(savePath);
            }
        }
    }

    /**
     * 开始执行下载操作
     * @param detailsTitle          影视标题
     * @param detailsUrl            影视链接
     * @param downloadUrl           下载地址
     * @param playNumber            下载集数
     * @param imgUrl                影视图片
     * @param downloaDdirectoryId   保存清单ID
     */
    public void startDownload(String detailsTitle, String detailsUrl, String downloadUrl, String playNumber, String imgUrl, String downloaDdirectoryId) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing())
            return;
        if (!downloadUrl.contains("http")) {
            showInfoDialog(String.format(Utils.getString(R.string.notSupportDownloadMsg), downloadUrl));
            return;
        }
        String fileSavePath = savePath + playNumber;
        String localImgPath = savePath + "cover_" + new Date().getTime() + ".jpg";
        ImageUtils.saveImageToLocalAsync(imgUrl, localImgPath, saveSuccess -> {
            String img = saveSuccess ? localImgPath : imgUrl;
            boolean isM3U8 = downloadUrl.contains("m3u8");
            long taskId = createDownloadTask(isM3U8, downloadUrl, fileSavePath);
            if (isM3U8) showInfoDialog(Utils.getString(R.string.downloadM3u8Tips));
            TDownloadManager.insertDownload(detailsTitle,  img, detailsUrl, downloaDdirectoryId);
            TDownloadDataManager.insertDownloadData(detailsTitle, playNumber, 0, taskId);
            App.getInstance().showToastMsg(String.format( Utils.getString(R.string.downloadStart), playNumber), DialogXTipEnum.SUCCESS);
            // 开启下载服务
            activity.startService(new Intent(activity, DownloadService.class));
            EventBus.getDefault().post(REFRESH_DOWNLOAD);
            checkHasDownload();
        });
    }

    /**
     * 创建下载任务
     * @param isM3u8    是否是M3U8
     * @param url       下载地址
     * @param savePath  保存路劲
     * @return
     */
    public long createDownloadTask(boolean isM3u8, String url, String savePath) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return -1;
        url = url.replaceAll("\\\\", "");
        HttpOption httpOption = new HttpOption();
        HashMap<String, String> headerMap = parserInterface.setPlayerHeaders();
        if (!Utils.isNullOrEmpty(headerMap))
            httpOption.addHeaders(headerMap);
        if (isM3u8)
            KeyDownloader.downloadKey(url, savePath);
        return isM3u8 ?
                Aria.download(activity)
                        .load(url)
                        .setFilePath(savePath + ".m3u8")
                        .ignoreFilePathOccupy()
                        .option(httpOption)
                        .m3u8VodOption(new M3U8DownloadConfig().setM3U8Option())
                        .ignoreCheckPermissions()
                        .create() :
                Aria.download(activity)
                        .load(url)
                        .setFilePath(savePath + ".mp4")
                        .ignoreFilePathOccupy()
                        .option(httpOption)
                        .ignoreCheckPermissions()
                        .create();
    }

    /**
     * 相关提示
     * @param msg 提示信息
     */
    public void showInfoDialog(String msg) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing())
            return;
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogStyle);
        builder.setCancelable(true);
        builder.setTitle(Utils.getString(R.string.otherOperation));
        builder.setMessage(msg);
        builder.setPositiveButton(Utils.getString(R.string.defaultPositiveBtnText), (dialog, which) -> dialog.dismiss());
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 释放资源
     */
    public void release() {
        BottomSheetDialog dialog = dialogRef.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialogRef.clear();
        activityRef.clear();
        adapterRef.clear();
    }
}
