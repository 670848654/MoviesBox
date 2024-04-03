package my.project.moviesbox.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import my.project.moviesbox.view.HomeFragment;
import my.project.moviesbox.view.MyFragment;
import my.project.moviesbox.view.SettingFragment;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: HomeViewPageAdapter
  * @描述: APP主页ViewPage适配器
  * @作者: Li Z
  * @日期: 2024/1/22 17:11
  * @版本: 1.0
 */
public class HomeViewPageAdapter extends FragmentStateAdapter {
    private List<Fragment> list;

    /**
     * @方法名称: HomeViewPageAdapter
     * @方法描述: 构造方法
     * @日期: 2024/1/22 17:11
     * @作者: Li Z
     * @param fragmentList Fragment视图集合 {@link HomeFragment},{@link MyFragment},{@link SettingFragment}
     * @返回:
     */
    public HomeViewPageAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragmentList) {
        super(fragmentActivity);
        list = fragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
