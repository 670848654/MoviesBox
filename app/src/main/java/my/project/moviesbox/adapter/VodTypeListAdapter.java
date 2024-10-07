package my.project.moviesbox.adapter;

import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: DramaAdapter
  * @描述: 影视播放列表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 14:38
  * @版本: 1.0
 */
public class VodTypeListAdapter extends BaseQuickAdapter<DialogItemBean, BaseViewHolder> {

    public VodTypeListAdapter(@Nullable List<DialogItemBean> data) {
        super(R.layout.custom_dialog_item, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, DialogItemBean item) {
        TextView typeView = helper.getView(R.id.type);
        helper.setText(R.id.url, item.getUrl());
        helper.setGone(R.id.recommended, !item.isRecommendedUse());
        switch (item.getVodTypeEnum()) {
            case MP4:
                typeView.setText("MP4");
                typeView.setBackground(Utils.getContext().getDrawable(R.drawable.item_mp4));
                break;
            case M3U8:
                typeView.setText("M3U8");
                typeView.setBackground(Utils.getContext().getDrawable(R.drawable.item_m3u8));
                break;
        }
    }
}