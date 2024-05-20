package my.project.moviesbox.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import cn.jzvd.JZUtils;
import my.project.moviesbox.R;
import my.project.moviesbox.database.entity.TDownloadDataWithFields;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DownloadDataAdapter
  * @描述: 下载组二级列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:36
  * @版本: 1.0
 */
public class DownloadDataAdapter extends BaseQuickAdapter<TDownloadDataWithFields, BaseViewHolder> implements LoadMoreModule {
    private Context context;

    public DownloadDataAdapter(Context context, List<TDownloadDataWithFields> list) {
        super(Utils.isPad() ? R.layout.item_download_data_pad : R.layout.item_download_data, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, TDownloadDataWithFields item) {
        String imgUrl = item.getVideoImgUrl();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        String title = item.getTDownloadData().getVideoNumber() + (item.getTDownloadData().getSavePath().contains(context.getFilesDir().getAbsolutePath()) ? " <font color=\"#FF5722\">[私有目录]</font>" : " <font color=\"#31BDEC\">[公共存储]</font>");
        helper.setText(R.id.title, Html.fromHtml(title));
        helper.setText(R.id.file_size, item.getTDownloadData().getVideoFileSize() != 0 ? Utils.getNetFileSizeDescription(item.getTDownloadData().getVideoFileSize()) : "0B");
        helper.setVisible(R.id.bottom_progress, false);
        String completeText = "";
        switch (item.getTDownloadData().getComplete()) {
            case 0:
                completeText = "<font color=\"#1E9FFF\">"+Utils.getString(R.string.waitState)+"</font>";
                break;
            case 1:
                completeText = "<font color=\"#5FB878\">"+Utils.getString(R.string.successState)+"</font>";
                break;
            case 2:
                completeText = "<font color=\"#FF5722\">"+Utils.getString(R.string.errorState)+"</font>";
                break;
        }
//        LinearProgressIndicator linearProgressIndicator = helper.getView(R.id.show_progress);
        ProgressBar linearProgressIndicator = helper.getView(R.id.show_progress);
        long watchProgress = item.getTDownloadData().getWatchProgress();
        long videoDuration = item.getTDownloadData().getVideoDuration();
        if (videoDuration != 0) { // 只有播放过才有视频时长 因此用来做判断
            helper.setText(R.id.time, watchProgress == 0 ? Utils.getString(R.string.finishedReading) : JZUtils.stringForTime(watchProgress) + "/" + JZUtils.stringForTime(videoDuration));
            helper.getView(R.id.time).setVisibility(View.VISIBLE);
            linearProgressIndicator.setVisibility(watchProgress == 0 ? View.GONE : View.VISIBLE);
            linearProgressIndicator.setMax((int) item.getTDownloadData().getVideoDuration());
            linearProgressIndicator.setProgress((int) item.getTDownloadData().getWatchProgress());
        } else {
            linearProgressIndicator.setVisibility(View.GONE);
            helper.getView(R.id.time).setVisibility(View.GONE);
        }
        helper.setText(R.id.state, Html.fromHtml(completeText));
        if (item.getTDownloadData().getComplete() == 1) {
            helper.getView(R.id.img_box).setBackground(null);
            helper.setText(R.id.number, "");
            Utils.loadVideoScreenshot(context, item.getTDownloadData().getSavePath(), item.getVideoImgUrl(), helper.getView(R.id.img), (item.getTDownloadData().getWatchProgress() == 0 ? 1000 : item.getTDownloadData().getWatchProgress()) * 1000);
        } else {
            helper.setBackgroundColor(R.id.img_box, R.drawable.download_img_gradient);
            Utils.setImgViewBg(item.getVideoImgUrl(), "", imageView);
        }
    }
}