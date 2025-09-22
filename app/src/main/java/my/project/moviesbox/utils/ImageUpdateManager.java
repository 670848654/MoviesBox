package my.project.moviesbox.utils;

import android.os.Handler;
import android.os.Looper;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import my.project.moviesbox.contract.UpdateImgContract;
import my.project.moviesbox.database.entity.TFavoriteWithFields;
import my.project.moviesbox.database.entity.THistoryWithFields;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.presenter.UpdateImgPresenter;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/10/12 13:28
 */
public class ImageUpdateManager implements UpdateImgContract.View {
    private static ImageUpdateManager instance;
    private final Queue<ImageUpdateTask> taskQueue = new LinkedList<>(); // 任务队列
    private boolean isUpdating = false; // 是否正在执行任务
    private final Handler uiHandler = new Handler(Looper.getMainLooper()); // 主线程 Handler
    private boolean isEventBusRegistered = false;
    private final UpdateImgPresenter updateImgPresenter;
    private ImageUpdateTask task;

    private ImageUpdateManager() {
        updateImgPresenter = new UpdateImgPresenter(this);
    }

    public static synchronized ImageUpdateManager getInstance() {
        if (instance == null) {
            instance = new ImageUpdateManager();
        }
        if (!instance.isEventBusRegistered) {
            instance.updateImgPresenter.registerEventBus();
            instance.isEventBusRegistered = true;

        }
        return instance;
    }

    /**
     * @return
     * @方法名称: loadingView
     * @方法描述: 用于显示加载中视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void loadingView() {

    }

    /**
     * @param msg 错误文本信息
     * @return
     * @方法名称: errorView
     * @方法描述: 用于显示加载失败视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void errorView(String msg) {

    }

    /**
     * @return
     * @方法名称: emptyView
     * @方法描述: 用于显示空数据视图
     * @日期: 2024/1/22 19:52
     * @作者: Li Z
     */
    @Override
    public void emptyView() {

    }

    @Override
    public void successImg(String descUrl, String imgUrl) {
        LogUtil.logInfo("获取 " + descUrl + " 封面成功", imgUrl);
        uiHandler.post(() -> {
            BaseQuickAdapter<?, ? extends BaseViewHolder> adapter = task.getAdapter();
            switch (task.getUpdateImgEnum()) {
                case FAVORITE:
                    List<TFavoriteWithFields> tFavoriteWithFields = (List<TFavoriteWithFields>) task.getData();
                    for (int i=0,size=tFavoriteWithFields.size(); i<size; i++) {
                        if (task.getDescUrl().contains(tFavoriteWithFields.get(i).getTFavorite().getVideoUrl())) {
                            tFavoriteWithFields.get(i).getTFavorite().setVideoImgUrl(imgUrl);
                            tFavoriteWithFields.get(i).setRefreshCover(true);
//                            LogUtil.logInfo("title", tFavoriteWithFields.get(i).getVideoTitle());
//                            LogUtil.logInfo("index", i + adapter.getHeaderLayoutCount() + (adapter.hasEmptyView() ? 1 : 0) + "");
                            adapter.notifyItemChanged(i + adapter.getHeaderLayoutCount() + (adapter.hasEmptyView() ? 1 : 0));
                            TVideoManager.updateImg(tFavoriteWithFields.get(i).getVideoId(), imgUrl, 0);
                            break;
                        }
                    }
                    break;
                case HISTORY:
                    List<THistoryWithFields> tHistoryWithFields = (List<THistoryWithFields>) task.getData();
                    for (int i=0,size=tHistoryWithFields.size(); i<size; i++) {
                        if (task.getDescUrl().contains(tHistoryWithFields.get(i).getTHistory().getVideoDescUrl())) {
                            tHistoryWithFields.get(i).getTHistory().setVideoImgUrl(imgUrl);
                            tHistoryWithFields.get(i).setRefreshCover(true);
//                            LogUtil.logInfo("title", tHistoryWithFields.get(i).getVideoTitle());
//                            LogUtil.logInfo("index", i + adapter.getHeaderLayoutCount() + (adapter.hasEmptyView() ? 1 : 0) + "");
                            adapter.notifyItemChanged(i + adapter.getHeaderLayoutCount() + (adapter.hasEmptyView() ? 1 : 0));
                            TVideoManager.updateImg(tHistoryWithFields.get(i).getVideoId(), imgUrl, 1);
                            break;
                        }
                    }
                    break;
            }
            // 任务完成后，处理下一个任务
            isUpdating = false;
            processNextTask(); // 递归处理下一个任务
        });
    }

    @Override
    public void errorImg(String descUrl) {
        LogUtil.logInfo("获取 " + descUrl + " 封面失败", "");
        uiHandler.post(() -> {
            BaseQuickAdapter<?, ? extends BaseViewHolder> adapter = task.getAdapter();
            switch (task.getUpdateImgEnum()) {
                case FAVORITE:
                    List<TFavoriteWithFields> tFavoriteWithFields = (List<TFavoriteWithFields>) task.getData();
                    for (int i=0,size=tFavoriteWithFields.size(); i<size; i++) {
                        if (task.getDescUrl().contains(tFavoriteWithFields.get(i).getTFavorite().getVideoUrl())) {
                            tFavoriteWithFields.get(i).setRefreshCover(true);
                            adapter.notifyItemChanged(i + adapter.getHeaderLayoutCount() + (adapter.hasEmptyView() ? 1 : 0));
                            break;
                        }
                    }
                    break;
                case HISTORY:
                    List<THistoryWithFields> tHistoryWithFields = (List<THistoryWithFields>) task.getData();
                    for (int i=0,size=tHistoryWithFields.size(); i<size; i++) {
                        if (task.getDescUrl().contains(tHistoryWithFields.get(i).getTHistory().getVideoDescUrl())) {
                            tHistoryWithFields.get(i).setRefreshCover(true);
                            adapter.notifyItemChanged(i + adapter.getHeaderLayoutCount() + (adapter.hasEmptyView() ? 1 : 0));
                            break;
                        }
                    }
                    break;
            }
            // 任务完成后，处理下一个任务
            isUpdating = false;
            processNextTask();
        });
    }

    public enum UpdateImgEnum {
        FAVORITE, HISTORY
    }

    @Data
    @AllArgsConstructor
    private static class ImageUpdateTask {
        String descUrl;
        String oldImgUrl;
        List<?> data;
        BaseQuickAdapter<?, ? extends BaseViewHolder> adapter;
        UpdateImgEnum updateImgEnum;
    }

    // 添加任务到队列中
    public synchronized void addUpdateImgTask(String descUrl, String oldImgUrl, List<?> data, BaseQuickAdapter<?, ? extends BaseViewHolder> adapter, UpdateImgEnum updateImgEnum) {
        taskQueue.add(new ImageUpdateTask(descUrl, oldImgUrl, data, adapter, updateImgEnum));
        if (!isUpdating) {
            processNextTask(); // 如果当前没有任务在处理，开始处理下一个任务
//            LogUtil.logInfo("addUpdateImgTask start", oldImgUrl);
        }
    }

    // 处理下一个任务
    private synchronized void processNextTask() {
        if (taskQueue.isEmpty()) {
            isUpdating = false; // 没有任务在队列中，停止更新
            if (isEventBusRegistered) {
                updateImgPresenter.unregisterEventBus();
                isEventBusRegistered = false;
            }
            updateImgPresenter.detachView();
//            LogUtil.logInfo("No Tasks", "");
            return;
        }

        isUpdating = true;
        task = taskQueue.poll(); // 取出队列中的下一个任务
        updateImgPresenter.loadData(task.getOldImgUrl(), task.getDescUrl());
    }
}
