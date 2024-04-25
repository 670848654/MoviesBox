package my.project.moviesbox.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import cn.jzvd.JZUtils;
import my.project.moviesbox.R;
import my.project.moviesbox.database.entity.THistoryWithFields;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: HistoryListAdapter
  * @描述: 历史记录列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 16:39
  * @版本: 1.0
 */
public class HistoryListAdapter extends BaseQuickAdapter<THistoryWithFields, BaseViewHolder> implements LoadMoreModule {
    private Context context;

    public HistoryListAdapter(Context context, List<THistoryWithFields> list) {
        super(Utils.isPad() ? R.layout.item_history_pad : R.layout.item_history, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, THistoryWithFields item) {
        String imgUrl = item.getTHistory().getVideoImgUrl();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        Utils.setDefaultImage(imgUrl, item.getTHistory().getVideoDescUrl(), imageView, false, null, helper.getView(R.id.title));
        helper.setText(R.id.title, item.getVideoTitle());
        helper.setText(R.id.play_date, item.getTHistory().getUpdateTime());
        long watchProgress = item.getWatchProgress();
        long videoDuration = item.getVideoDuration();
        if (watchProgress== 0 && videoDuration == 0)
            helper.setText(R.id.time, Utils.getString(R.string.playbackError));
        else
            helper.setText(R.id.time, watchProgress == 0 ? Utils.getString(R.string.finishedReading) : JZUtils.stringForTime(watchProgress) + "/" + JZUtils.stringForTime(videoDuration));
//        helper.setText(R.id.info, String.format(Utils.getString(R.string.playSource), item.getVideoNumber(), (item.getVideoPlaySource()+1)));
        helper.setText(R.id.info, item.getVideoNumber());
//        LinearProgressIndicator linearProgressIndicator = helper.getView(R.id.bottom_progress);
        ProgressBar linearProgressIndicator = helper.getView(R.id.bottom_progress);
        linearProgressIndicator.setVisibility(watchProgress == 0 ? View.GONE : View.VISIBLE);
        linearProgressIndicator.setMax((int) videoDuration);
        linearProgressIndicator.setProgress((int) watchProgress);
    }
}