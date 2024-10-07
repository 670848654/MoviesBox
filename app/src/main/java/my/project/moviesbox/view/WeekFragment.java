package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.WeekAdapter;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.presenter.Presenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: WeekFragment
  * @描述: 星期Fragment
  * @作者: Li Z
  * @日期: 2024/2/4 17:14
  * @版本: 1.0
 */
public class WeekFragment extends BaseFragment {
    private View view;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private List<WeekDataBean.WeekItem> weekItems;
    private WeekAdapter adapter;

    public WeekFragment(List<WeekDataBean.WeekItem> weekItems) {
        this.weekItems = weekItems;
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_week, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        initAdapter();
        return view;
    }

    public void initAdapter() {
        if (adapter == null) {
            adapter = new WeekAdapter(getActivity(), parserInterface.setWeekItemType(), weekItems);
            WeekActivity weekActivity = (WeekActivity) getActivity();
            if (weekActivity != null)
                weekActivity.setAdapterAnimation(adapter);
            adapter.setEmptyView(rvView);
            adapter.setOnItemClickListener((adapter, view, position) -> {
                if (!Utils.isFastClick()) return;
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
    protected Presenter createPresenter() {
        return null;
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
