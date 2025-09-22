package my.project.moviesbox.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import my.project.moviesbox.adapter.WeekAdapter;
import my.project.moviesbox.databinding.FragmentWeekBinding;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.DetailsActivity;
import my.project.moviesbox.view.WeekActivity;
import my.project.moviesbox.view.base.BaseFragment;

/**
  * @包名: my.project.moviesbox.view
  * @类名: WeekFragment
  * @描述: 星期Fragment
  * @作者: Li Z
  * @日期: 2024/2/4 17:14
  * @版本: 1.0
 */
public class WeekFragment extends BaseFragment<FragmentWeekBinding> {
    private View view;
    RecyclerView recyclerView;
    private List<WeekDataBean.WeekItem> weekItems;
    private WeekAdapter adapter;

    public WeekFragment(List<WeekDataBean.WeekItem> weekItems) {
        this.weekItems = weekItems;
    }

    @Override
    protected FragmentWeekBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        // 防止重复 inflate
        if (binding == null) {
            binding = FragmentWeekBinding.inflate(inflater, container, false);
        } else {
            // 从父容器移除
            ViewParent parent = binding.getRoot().getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(binding.getRoot());
            }
        }
        return binding;
    }

    @Override
    public void initViews() {
        recyclerView = binding.rvList;
        initAdapter();
    }

    @Override
    public void initClickListeners() {}

    @Override
    protected void setConfigurationChanged() {}

    public void initAdapter() {
        if (adapter == null) {
            adapter = new WeekAdapter(parserInterface.setWeekItemType(), weekItems);
            WeekActivity weekActivity = (WeekActivity) getActivity();
            if (weekActivity != null)
                weekActivity.setAdapterAnimation(adapter);
            adapter.setEmptyView(rvView);
            adapter.setOnItemClickListener((adapter, view, position) -> {
                if (!Utils.isFastClick()) return;
                Utils.setVibration(view);
                Bundle bundle = new Bundle();
                bundle.putString("title", weekItems.get(position).getTitle());
                bundle.putString("url", weekItems.get(position).getUrl());
                startActivity(new Intent(getActivity(), DetailsActivity.class).putExtras(bundle));
            });
            recyclerView.setAdapter(adapter);
            if (Utils.checkHasNavigationBar(getActivity()))
                recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
            setRecyclerViewView();
        }
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected void retryListener() {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEnum refresh) {

    }

    private void setRecyclerViewView() {
        position = recyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), parserInterface.setWeekItemListItemSize(Utils.isPad(), isPortrait)));
        recyclerView.getLayoutManager().scrollToPosition(position);
    }
}
