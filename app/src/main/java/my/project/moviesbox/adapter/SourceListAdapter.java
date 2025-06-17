package my.project.moviesbox.adapter;

import android.graphics.Paint;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.bean.SourceDataBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/8/2 14:58
 */
public class SourceListAdapter extends BaseQuickAdapter<SourceDataBean, BaseViewHolder> {
    private final OnItemClick onItemClick;

    public SourceListAdapter(List<SourceDataBean> list, OnItemClick onItemClick) {
        super(R.layout.item_source_data, list);
        this.onItemClick = onItemClick;
    }

    @Override
    protected void convert(BaseViewHolder helper, SourceDataBean item) {
        MaterialCardView cardView = helper.getView(R.id.card_view);
        TextView titleView = helper.getView(R.id.title);
        Button rssBtn = helper.getView(R.id.rss);
        Button websiteReleaseBtn = helper.getView(R.id.website);
        Button doneBtn = helper.getView(R.id.done);
        TextView stateMsgView = helper.getView(R.id.stateMsg);
        int nowSource = SharedPreferencesUtils.getDefaultSource();
        int itemSourceIndex = item.getSource().getIndex();
        int itemSourceState = item.getSource().getStateEnum().getState();
        cardView.setStrokeColor(Utils.getContext().getColor(R.color.defaultCardStrokeColor));
        titleView.setPaintFlags(titleView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        if (itemSourceState == SourceEnum.SourceStateEnum.UNDONE.getState()) {
            doneBtn.setText(Utils.getString(R.string.notSupported));
            doneBtn.setEnabled(false);
        } else if (itemSourceState == SourceEnum.SourceStateEnum.DEPRECATED.getState()) {
            doneBtn.setEnabled(true);
        } else if (nowSource == itemSourceIndex) {
            doneBtn.setEnabled(false);
            doneBtn.setText(Utils.getString(R.string.currentLocation));
            cardView.setStrokeColor(Utils.getContext().getColor(R.color.pinka200));
        } else {
            doneBtn.setEnabled(true);
            doneBtn.setText(Utils.getString(R.string.change2ThisDataSource));
        }
        String stateMsg = item.getSource().getMsg();
        boolean hasStateMsg = !Utils.isNullOrEmpty(stateMsg);
        if (hasStateMsg) {
            int stateMsgColor = item.getSource().getStateEnum().getColor();
            stateMsgView.setText(String.format(Utils.getString(R.string.siteParserStatus), stateMsg));
            stateMsgView.setTextColor(Utils.getContext().getColor(stateMsgColor));
        }
        stateMsgView.setVisibility(hasStateMsg ? View.VISIBLE : View.GONE);
        // 是否包含弹幕
        boolean hasDanmu = item.isHasDanmu();
        // 是否有描述
        boolean hasInfo = !Utils.isNullOrEmpty(item.getSourceInfo());
        // 是否有发布页
        boolean hasWebsiteRelease = item.isHasWebsiteRelease();
        // 是否有RSS订阅
        boolean hasRss = item.isHasRss();
        String title = item.getTitle();
        char firstChar = title.charAt(0);
        String newStr = "<font color='#f48fb1'><strong>"+firstChar+"</strong></font>" + title.substring(1);
        titleView.setText(Html.fromHtml(newStr));
        helper.setText(R.id.sourceType, item.getSourceType());
        helper.setBackgroundResource(R.id.sourceType, item.getSourceBg());
        helper.setGone(R.id.danmu, !hasDanmu);
        helper.setText(R.id.info, hasInfo ? item.getSourceInfo() : Utils.getString(R.string.noDescription));
        websiteReleaseBtn.setVisibility(hasWebsiteRelease ? View.VISIBLE : View.GONE);
        rssBtn.setVisibility(hasRss ? View.VISIBLE : View.GONE);
        websiteReleaseBtn.setOnClickListener(v -> {
            Utils.setVibration(v);
            onItemClick.onWebsiteReleaseClick(item.getWebsiteReleaseUrl());
        });
        rssBtn.setOnClickListener(v -> {
            Utils.setVibration(v);
            onItemClick.onRssClick(SharedPreferencesUtils.getUserSetDomain(itemSourceIndex)+item.getRssUrl());
        });
        doneBtn.setOnClickListener(v -> {
            Utils.setVibration(v);
            onItemClick.onChangeSource(itemSourceIndex);
        });
    }
    public interface OnItemClick {
        /**
         * 发布页
         * @param url
         */
        void onWebsiteReleaseClick(String url);

        /**
         * RSS
         * @param url
         */
        void onRssClick(String url);

        /**
         * 换源
         * @param source
         */
        void onChangeSource(int source);
    }
}
