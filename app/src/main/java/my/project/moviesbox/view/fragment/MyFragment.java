package my.project.moviesbox.view.fragment;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.MyViewPageAdapter;
import my.project.moviesbox.database.manager.TDownloadManager;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.databinding.FragmentMyBinding;
import my.project.moviesbox.event.RefreshEnum;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.view.base.BaseFragment;

/**
  * @包名: my.project.moviesbox.view
  * @类名: MyFragment
  * @描述: 我的列表视图
  * @作者: Li Z
  * @日期: 2024/2/4 17:12
  * @版本: 1.0
 */
public class MyFragment extends BaseFragment<FragmentMyBinding> {
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MyViewPageAdapter myViewPageAdapter;
    private List<Fragment> list = new ArrayList<>();

    @Override
    protected FragmentMyBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        // 防止重复 inflate
        if (binding == null) {
            binding = FragmentMyBinding.inflate(inflater, container, false);
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
        tabLayout = binding.tab;
        viewPager2 = binding.viewpage2;
        initViewPage();
    }

    @Override
    public void initClickListeners() {

    }

    @Override
    protected void setConfigurationChanged() {

    }

    private void initViewPage() {
        list.add(new FavoriteFragment());
        list.add(new HistoryFragment());
        list.add(new DownloadFragment());
        myViewPageAdapter = new MyViewPageAdapter(getActivity(), list);
        viewPager2.setAdapter(myViewPageAdapter);
//        viewPager2.setPageTransformer(new ParallaxTransformer());
        int iconSelectedColor = ContextCompat.getColor(getActivity(), R.color.pink200);
        tabLayout.getTabAt(0).getIcon().setColorFilter(iconSelectedColor, PorterDuff.Mode.SRC_IN);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
                tab.getIcon().setColorFilter(iconSelectedColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int unselectedColor = ContextCompat.getColor(getActivity(), DarkModeUtils.isDarkMode(getActivity()) ? R.color.night_text_color : R.color.light_text_color);
                tab.getIcon().setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //实现滑动的时候 联动 bottomNavigationView的selectedItem
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
                /*if (position == 1)
                    EventBus.getDefault().post(new ShowProgressEvent());*/
            }
        });
        setTabBadge(0);
        setTabBadge(1);
        setTabBadge(2);
    }

    /**
     * 显示相关数量角标
     * @param index 0 当前源收藏夹 1 当前源历史记录 2 下载记录
     */
    private void setTabBadge(int index) {
        int count = 0;
        switch (index) {
            case 0:
                count = TFavoriteManager.queryFavoriteCount();
                break;
            case 1:
                count = THistoryManager.queryHistoryCount();
                break;
            case 2:
                count = TDownloadManager.queryAllDownloadCount();
                break;
        }
        BadgeDrawable badgeDrawable = tabLayout.getTabAt(index).getOrCreateBadge();
        if (count > 0)
            badgeDrawable.setNumber(count);
        else
            tabLayout.getTabAt(index).removeBadge();
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
        switch (refresh) {
            case REFRESH_FAVORITE:
                setTabBadge(0);
                break;
            case REFRESH_HISTORY:
                setTabBadge(1);
                break;
            case REFRESH_DOWNLOAD:
                setTabBadge(2);
                break;
            case REFRESH_TAB_COUNT:
                setTabBadge(0);
                setTabBadge(1);
                setTabBadge(2);
                break;
        }
    }
}
