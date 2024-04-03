package my.project.moviesbox.adapter;

import android.content.Context;
import android.text.Html;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.database.entity.TDownloadWithFields;
import my.project.moviesbox.utils.Utils;

/**
 * @包名: my.project.moviesbox.adapter
 * @类名: DownloadAdapter
 * @描述: 下载组列表适配器
 * @作者: Li Z
 * @日期: 2024/1/22 14:36
 * @版本: 1.0
 */
public class DownloadAdapter extends BaseQuickAdapter<TDownloadWithFields, BaseViewHolder> implements LoadMoreModule {
    private Context context;

    public DownloadAdapter(Context context, List<TDownloadWithFields> list) {
        super(Utils.isPad() ? R.layout.item_download_pad : R.layout.item_download, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, TDownloadWithFields item) {
        String imgUrl = item.getTDownload().getVideoImgUrl();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        helper.setText(R.id.title, item.getVideoTitle());
        String imgContent = String.format(Utils.getString(R.string.downloadVodListContent), item.getDownloadDataSize());
        if (item.getNoCompleteSize() > 0)
            imgContent += "<br><font color=\"RED\">" + String.format(Utils.getString(R.string.downloadVodNotCompleteListContent), item.getNoCompleteSize()) + "</font>";
        helper.setText(R.id.number, Html.fromHtml(imgContent));
        if (Utils.isPad())
            Utils.setImgViewBg(item.getTDownload().getVideoImgUrl(), item.getTDownload().getVideoDescUrl(), imageView);
        else
            Utils.setDefaultImage(item.getTDownload().getVideoImgUrl(), item.getTDownload().getVideoDescUrl(), imageView, false, null, null);
        helper.setVisible(R.id.file_size, false);
        helper.setVisible(R.id.bottom_progress, false);
        helper.setText(R.id.all_size, String.format(Utils.getString(R.string.downloadFileSize), item.getFilesSize()));
    }
}