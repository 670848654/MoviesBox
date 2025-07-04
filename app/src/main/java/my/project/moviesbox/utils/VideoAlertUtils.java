package my.project.moviesbox.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.VodTypeListAdapter;
import my.project.moviesbox.application.App;
import my.project.moviesbox.event.VideoSniffEvent;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.service.SniffingVideoService;
import my.project.moviesbox.view.PlayerActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 弹窗工具类
 * @date 2025/6/23 10:40
 */
public class VideoAlertUtils {
    private final WeakReference<Activity> activityRef;

    public VideoAlertUtils(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    /**
     * 打开播放器
     * @param isDescActivity    是否是详情界面
     * @param dramaTitle        集数标题
     * @param url               播放地址
     * @param vodTitle          影视标题
     * @param dramaUrl          集数链接
     * @param list              剧集列表
     * @param clickIndex        当前点击剧集下标
     * @param vodId             影视ID
     * @param nowSource         当前播放源下标
     */
    public void openPlayer(boolean isDescActivity, String dramaTitle, String url, String vodTitle, String dramaUrl,
                                  List<DetailsDataBean.DramasItem> list, int clickIndex, String vodId, int nowSource) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing())
            return;
        Bundle bundle = new Bundle();
        bundle.putString("dramaTitle", dramaTitle);
        bundle.putString("url", url);
        bundle.putString("vodTitle", vodTitle);
        bundle.putString("dramaUrl", dramaUrl);
        bundle.putSerializable("list", (Serializable) list);
        bundle.putInt("clickIndex", clickIndex);
        bundle.putString("vodId", vodId);
        bundle.putInt("nowSource", nowSource);
        App.destroyActivity("player");
        if (isDescActivity)
            activity.startActivityForResult(new Intent(activity, PlayerActivity.class).putExtras(bundle), 0x10);
        else {
            activity.startActivity(new Intent(activity, PlayerActivity.class).putExtras(bundle));
            activity.finish();
        }
    }

    /**
     * 发现多个播放地址时弹窗 下载用
     * @param dialogItemBeans       播放地址列表
     * @param onItemClickListener   弹窗item点击回调
     * @return
     */
    public AlertDialog showMultipleVideoSources4Download(List<DialogItemBean> dialogItemBeans, OnItemClickListener onItemClickListener) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing())
            return null;
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.downloadMultipleVideoDialogTitle));
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.custom_dialog_recyclerview, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        VodTypeListAdapter adapter = new VodTypeListAdapter(dialogItemBeans);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    /**
     * 发现多个播放地址时弹窗
     * @param dialogItemBeans       播放地址列表
     * @param onItemClickListener   弹窗item点击回调
     * @param isPlayerActivity      是否是播放界面
     */
    public AlertDialog showMultipleVideoSources(List<DialogItemBean> dialogItemBeans, OnItemClickListener onItemClickListener, boolean isPlayerActivity) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing())
            return null;
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.selectVideoSource));
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.custom_dialog_recyclerview, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        VodTypeListAdapter adapter = new VodTypeListAdapter(dialogItemBeans);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        builder.setView(dialogView);
        if (!isPlayerActivity)
            builder.setNegativeButton(Utils.getString(R.string.defaultNegativeBtnText), null);
        alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    /**
     * 启动嗅探服务
     * @param url 播放页地址
     * @param activityEnum EventBus订阅处理判断
     * @param sniffEnum 嗅探结果处理类型
     */
    public void startSniffing(String url, VideoSniffEvent.ActivityEnum activityEnum, VideoSniffEvent.SniffEnum sniffEnum) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing())
            return;
        Intent intent = new Intent(activity, SniffingVideoService.class);
        intent.putExtra("url", url);
        intent.putExtra("activityEnum", activityEnum.name());
        intent.putExtra("sniffEnum", sniffEnum.name());
        activity.startService(intent);
    }

    /**
     * 嗅探失败弹窗
     */
    public void sniffErrorDialog() {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing())
            return;
        Utils.showAlert(activity,
                activity.getString(R.string.errorDialogTitle),
                activity.getString(R.string.sniffVodPlayUrlError),
                false,
                activity.getString(R.string.defaultPositiveBtnText),
                "",
                "",
                (dialog, which) -> dialog.dismiss(),
                null,
                null);
    }

    /**
     * 释放资源
     */
    public void release() {
        activityRef.clear();
    }
}
