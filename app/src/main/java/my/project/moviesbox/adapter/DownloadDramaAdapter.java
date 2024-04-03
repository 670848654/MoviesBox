package my.project.moviesbox.adapter;

import android.content.Context;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.bean.DownloadDramaBean;
import my.project.moviesbox.utils.DarkModeUtils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DownloadDramaAdapter
  * @描述: 影视详情下载列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:37
  * @版本: 1.0
 */
public class DownloadDramaAdapter extends BaseQuickAdapter<DownloadDramaBean, BaseViewHolder> {
    private Context context;

    public DownloadDramaAdapter(Context context, @Nullable List<DownloadDramaBean> data) {
        super(R.layout.item_desc_drama, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, DownloadDramaBean item) {
        Button button = helper.getView(R.id.title);
        helper.setText(R.id.title, item.getTitle());
        if (item.isHasDownload())
            button.setTextColor(context.getColor(R.color.green500));
        else
            helper.setTextColor(R.id.title, DarkModeUtils.isDarkMode(context) ? context.getColor(R.color.night_text_color) : context.getColor(R.color.light_text_color ));
    }
}