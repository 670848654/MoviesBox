package my.project.moviesbox.view;

import static my.project.moviesbox.view.BaseActivity.ADAPTER_SCALE_IN_ANIMATION;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.FavoriteListAdapter;
import my.project.moviesbox.contract.FavoriteContract;
import my.project.moviesbox.contract.UpdateImgContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.database.entity.TFavoriteWithFields;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.event.RefreshEvent;
import my.project.moviesbox.event.RefreshFavoriteEvent;
import my.project.moviesbox.event.UpdateImgEvent;
import my.project.moviesbox.presenter.FavoritePresenter;
import my.project.moviesbox.presenter.UpdateImgPresenter;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: FavoriteFragment
  * @描述: 收藏夹视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:10
  * @版本: 1.0
 */
public class FavoriteFragment extends BaseFragment<FavoriteContract.View, FavoritePresenter> implements FavoriteContract.View, UpdateImgContract.View {
    private View view;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private FavoriteListAdapter adapter;
    private List<TFavoriteWithFields> favoriteList = new ArrayList<>();
    private int limit = 10;
    private int favoriteCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private boolean updateOrder; //
    private UpdateImgPresenter updateImgPresenter;

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_my_list, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        initAdapter();
        loadFavoriteData();
        return view;
    }

    private void initAdapter() {
        adapter = new FavoriteListAdapter(favoriteList);
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
            homeActivity.setAdapterAnimation(adapter, ADAPTER_SCALE_IN_ANIMATION, true);
        adapter.setEmptyView(rvView);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            TFavoriteWithFields bean = (TFavoriteWithFields) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("title", bean.getVideoTitle());
            bundle.putString("url", bean.getTFavorite().getVideoUrl());
            startActivityForResult(new Intent(getActivity(), DetailsActivity.class).putExtras(bundle), 3000);
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return false;
            setMenu(view, R.menu.favorite_menu, R.id.remove, item -> {
                switch (item.getItemId()) {
                    case R.id.remove:
                        removeFavorite(position);
                        break;
                }
                return true;
            });
            return true;
        });
        adapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
        if (favoriteList.size() >= favoriteCount) {
            adapter.getLoadMoreModule().loadMoreEnd();
        } else {
            if (isErr) {
                isMain = false;
                mPresenter = new FavoritePresenter(favoriteList.size(), limit, updateOrder, this);
                loadData();
            } else {
                isErr = true;
                adapter.getLoadMoreModule().loadMoreFail();
            }
        }
    }, 500));
        if (Utils.checkHasNavigationBar(getActivity())) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
        mRecyclerView.setAdapter(adapter);
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.getLoadMoreModule().loadMoreComplete();
    }

    private void loadFavoriteData() {
//        updateOrder = (Boolean) SharedPreferencesUtils.getParam(this, "checkFavoriteUpdate", true);
        favoriteCount = TFavoriteManager.queryFavoriteCount();
        isMain = true;
        favoriteList.clear();
        adapter.notifyDataSetChanged();
        if (favoriteCount > 0 && updateOrder) {
//            application.showSnackbarMsg(msg, getString(R.string.checkFavoriteUpdate));
           /* mPresenter = new FavoritePresenter(0, application.animeUpdateInfoBeans, this);
            mPresenter.loadUpdateInfo();*/ // TODO
        } else {
            mPresenter = new FavoritePresenter(favoriteList.size(), limit, updateOrder, this);
            loadData();
        }
    }

    /**
     * 移除收藏
     */
    private void removeFavorite(int position) {
        TFavoriteManager.deleteFavorite(favoriteList.get(position).getVideoId());
        adapter.removeAt(position);
        favoriteCount = TFavoriteManager.queryFavoriteCount();
        application.showToastMsg(getString(R.string.removeFavorite));
        if (favoriteCount == 0) {
            setRecyclerViewEmpty();
            rvEmpty(getString(R.string.emptyMyList));
        }
        EventBus.getDefault().post(new RefreshEvent(4));
    }

    @Override
    protected FavoritePresenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(isMain);
    }

    @Override
    protected void retryListener() {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent refresh) {
        switch (refresh.getIndex()) {
            case 1:
                loadFavoriteData();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshFavoriteEvent(RefreshFavoriteEvent refreshFavoriteEvent) {
        for (int i=0,size=favoriteList.size(); i<size; i++) {
            if (favoriteList.get(i).getVideoId().equals(refreshFavoriteEvent.getVodId())) {
                favoriteList.get(i).getTFavorite().setLastVideoUpdateNumber(refreshFavoriteEvent.getLastPlayNumber());
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void loadingView() {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            favoriteList.clear();
            adapter.notifyDataSetChanged();
            setRecyclerViewEmpty();
            rvLoading();
        });
    }

    @Override
    public void errorView(String msg) {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            setRecyclerViewEmpty();
            rvEmpty(msg);
        });
    }

    @Override
    public void emptyView() {
    }

    @Override
    public void favoriteList(List<TFavoriteWithFields> list) {
        if (getActivity().isFinishing()) return;
        setLoadState(true);
        getActivity().runOnUiThread(() -> {
            if (isMain) {
                new Handler().postDelayed(() -> {
                    hideProgress();
                    favoriteList = list;
                    adapter.setNewInstance(favoriteList);
                    setRecyclerViewView();
                }, 500);
            } else
                adapter.addData(list);
        });
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), parserInterface.setFavoriteListItemSize(Utils.isPad(), isPortrait)));
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void completion(boolean complete) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != updateImgPresenter) updateImgPresenter.detachView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgEvent updateImgEvent) {
        if (getActivity().isFinishing()) return;
        updateImgPresenter = new UpdateImgPresenter(updateImgEvent.getOldImgUrl(), updateImgEvent.getDescUrl(), this);
        updateImgPresenter.loadData();
    }

    @Override
    public void successImg(String oldImgUrl, String imgUrl) {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            for (int i=0,size=favoriteList.size(); i<size; i++) {
                if (favoriteList.get(i).getTFavorite().getVideoImgUrl().contains(oldImgUrl)) {
                    favoriteList.get(i).getTFavorite().setVideoImgUrl(imgUrl);
                    adapter.notifyItemChanged(i);
                    TVideoManager.updateImg(favoriteList.get(i).getVideoId(), imgUrl, 0);
                    break;
                }
            }
        });
    }

    @Override
    public void errorImg() {

    }
}
