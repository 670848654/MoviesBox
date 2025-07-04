package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_TAB_COUNT;
import static my.project.moviesbox.utils.ImageUpdateManager.UpdateImgEnum.FAVORITE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.FavoriteListAdapter;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.contract.FavoriteContract;
import my.project.moviesbox.custom.CustomLoadMoreView;
import my.project.moviesbox.database.entity.TDirectory;
import my.project.moviesbox.database.entity.TFavoriteWithFields;
import my.project.moviesbox.database.enums.DirectoryTypeEnum;
import my.project.moviesbox.database.manager.TDirectoryManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.event.RefreshFavoriteEvent;
import my.project.moviesbox.event.UpdateImgEvent;
import my.project.moviesbox.model.FavoriteModel;
import my.project.moviesbox.presenter.FavoritePresenter;
import my.project.moviesbox.utils.ImageUpdateManager;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: FavoriteFragment
  * @描述: 收藏夹视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:10
  * @版本: 1.0
 */
public class FavoriteFragment extends BaseFragment<FavoriteModel, FavoriteContract.View, FavoritePresenter> implements FavoriteContract.View, BaseFragment.DirectoryPopupWindowAdapterClickListener {
    private View headerView;
    private Button headerSelectView;
    private Button headerConfigView;
    private View view;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private FavoriteListAdapter adapter;
    private String directoryId = "";
    private int favoriteCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private boolean updateOrder; //

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
        setDirectoryPopupWindowAdapterClickListener(this);
        initAdapter();
        loadFavoriteData();
        return view;
    }

    private void initAdapter() {
        headerView = getLayoutInflater().inflate(R.layout.base_header_view, null);
        headerSelectView = headerView.findViewById(R.id.select);
        headerConfigView = headerView.findViewById(R.id.config);
        adapter = new FavoriteListAdapter(parserInterface.favoriteItemStyleLayout(), new ArrayList<>());
        headerSelectView.setOnClickListener(v -> {
            Utils.setVibration(v);
            List<TDirectory> tDirectories = TDirectoryManager.queryFavoriteDirectoryList(false);
            showDirectoryPopupWindow(tDirectories, headerSelectView);
        });
        headerConfigView.setOnClickListener(v -> {
            Utils.setVibration(v);
            Bundle bundle = new Bundle();
            bundle.putString("type", DirectoryTypeEnum.FAVORITE.getName());
            bundle.putBoolean("showConfigBtn", true);
            startActivityForResult(new Intent(getActivity(), DirectoryConfigActivity.class).putExtras(bundle), DIRECTORY_REQUEST_CODE);
        });
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
            homeActivity.setAdapterAnimation(adapter);
        adapter.setHeaderView(headerView);
        adapter.setEmptyView(rvView);
        adapter.setHeaderWithEmptyEnable(true);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Utils.setVibration(view);
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
                    case R.id.refreshImage:
                        ImageView imageView = (ImageView) adapter.getViewByPosition(position+adapter.getHeaderLayoutCount(), R.id.img);
                        imageView.setImageDrawable(getActivity().getDrawable(R.drawable.loading));
//                        updateImgPresenter.loadData(favoriteList.get(position).getTFavorite().getVideoImgUrl(), favoriteList.get(position).getTFavorite().getVideoUrl());
                        TFavoriteWithFields tFavoriteWithField = (TFavoriteWithFields) adapter.getData().get(position);
                        ImageUpdateManager.getInstance().addUpdateImgTask(
                                tFavoriteWithField.getTFavorite().getVideoUrl(),
                                tFavoriteWithField.getTFavorite().getVideoImgUrl(),
                                adapter.getData(),
                                adapter,
                                FAVORITE);
                        break;
                    case R.id.moveDirectory:
                        Bundle bundle = new Bundle();
                        bundle.putString("type", DirectoryTypeEnum.FAVORITE.getName());
                        bundle.putInt("position", position);
                        startActivityForResult(new Intent(getActivity(), DirectoryChangeActivity.class).putExtras(bundle), DIRECTORY_REQUEST_CODE);
                        break;
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
        if (adapter.getData().size() >= favoriteCount) {
            adapter.getLoadMoreModule().loadMoreEnd();
        } else {
            if (isErr) {
                isMain = false;
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
        favoriteCount = TFavoriteManager.queryFavoriteCountByDirectoryId(directoryId);
        isMain = true;
        adapter.setNewInstance(new ArrayList<>());
        if (favoriteCount > 0 && updateOrder) {
//            application.showSnackbarMsg(msg, getString(R.string.checkFavoriteUpdate));
           /* mPresenter = new FavoritePresenter(0, application.animeUpdateInfoBeans, this);
            mPresenter.loadUpdateInfo();*/ // TODO
        } else
            loadData();
    }

    /**
     * 移除收藏
     */
    private void removeFavorite(int position) {
        TFavoriteWithFields tFavoriteWithField = adapter.getData().get(position);
        TFavoriteManager.deleteFavorite(tFavoriteWithField.getVideoId());
        adapter.removeAt(position);
        favoriteCount = TFavoriteManager.queryFavoriteCountByDirectoryId(directoryId);
        application.showToastMsg(getString(R.string.removeFavorite), DialogXTipEnum.SUCCESS);
        if (favoriteCount == 0) {
            setRecyclerViewEmpty();
            rvEmpty(getString(R.string.emptyMyList));
        }
        EventBus.getDefault().post(REFRESH_TAB_COUNT);
    }

    @Override
    protected FavoritePresenter createPresenter() {
        return mPresenter = new FavoritePresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(isMain, directoryId, adapter.getData().size(), ConfigManager.getInstance().getFavoriteQueryLimit(), updateOrder);
    }

    @Override
    protected void retryListener() {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEnum refresh) {
        switch (refresh) {
            case REFRESH_FAVORITE:
                loadFavoriteData();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshFavoriteEvent(RefreshFavoriteEvent refreshFavoriteEvent) {
        for (int i=0,size=adapter.getData().size(); i<size; i++) {
            TFavoriteWithFields tFavoriteWithField = adapter.getData().get(i);
            if (tFavoriteWithField.getVideoId().equals(refreshFavoriteEvent.getVodId())) {
                tFavoriteWithField.getTFavorite().setLastVideoUpdateNumber(refreshFavoriteEvent.getLastPlayNumber());
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void loadingView() {
        if (getActivity().isFinishing()) return;
        getActivity().runOnUiThread(() -> {
            adapter.setNewInstance(new ArrayList<>());
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
            for (TFavoriteWithFields tFavoriteWithFields : list) {
                tFavoriteWithFields.setBlurBg(parserInterface.favoriteItemBlurBg());
            }
            if (isMain) {
                new Handler().postDelayed(() -> {
                    hideProgress();
                    adapter.setNewInstance(list);
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
        int spanCount = parserInterface.setFavoriteListItemSize(Utils.isPad(), isPortrait);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? spanCount : 1;
            }
        });
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void completion(boolean complete) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgEvent updateImgEvent) {
        if (getActivity().isFinishing()) return;
//        updateImgPresenter.loadData(updateImgEvent.getOldImgUrl(), updateImgEvent.getDescUrl());
        ImageUpdateManager.getInstance().addUpdateImgTask(
                updateImgEvent.getDescUrl(),
                updateImgEvent.getOldImgUrl(),
                adapter.getData(),
                adapter,
                FAVORITE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIRECTORY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String type = data.getStringExtra("type");
                String selectDirectoryId = data.getStringExtra("directoryId");
                int position = data.getIntExtra("position", 0);
                if (Objects.equals(type, DirectoryTypeEnum.FAVORITE.getName())) {
                    if (!Objects.equals(selectDirectoryId, directoryId)) {
                        TFavoriteWithFields tFavoriteWithField = adapter.getData().get(position);
                        TFavoriteManager.updateFavoriteDirectoryId(tFavoriteWithField.getVideoId(), selectDirectoryId);
                        adapter.removeAt(position);
                        favoriteCount = TFavoriteManager.queryFavoriteCountByDirectoryId(directoryId);
                        if (favoriteCount == 0) {
                            setRecyclerViewEmpty();
                            rvEmpty(getString(R.string.emptyMyList));
                        }
                    }
                }
            } else if (resultCode == DIRECTORY_CONFIG_RESULT_CODE) {
                TDirectory tDirectory = TDirectoryManager.queryById(directoryId, false);
                if (tDirectory == null) {
                    // 被删除了回到默认清单
                    directoryId = null;
                    List<TDirectory> tDirectories = TDirectoryManager.queryFavoriteDirectoryList(false);
                    headerSelectView.setText(tDirectories.get(0).getName());
                } else
                    headerSelectView.setText(tDirectory.getName());
                loadFavoriteData();
            }
        }
    }

    @Override
    public void onItemClickListener(TDirectory tDirectory) {
        directoryId = tDirectory.getId();
        headerSelectView.setText(tDirectory.getName());
        loadFavoriteData();
        popupWindow.dismiss();
    }
}
