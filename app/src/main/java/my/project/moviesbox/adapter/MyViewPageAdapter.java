package my.project.moviesbox.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import my.project.moviesbox.view.DownloadFragment;
import my.project.moviesbox.view.FavoriteFragment;
import my.project.moviesbox.view.HistoryFragment;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: MyViewPageAdapter
  * @描述: APP首页 > 我的ViewPage适配器
  * @作者: Li Z
  * @日期: 2024/1/22 17:13
  * @版本: 1.0
 */
public class MyViewPageAdapter extends FragmentStateAdapter {
    private List<Fragment> list;

    /**
     * @方法名称: MyViewPageAdapter
     * @方法描述: 构造方法
     * @日期: 2024/1/22 17:14
     * @作者: Li Z
     * @param fragmentList Fragment视图集合 {@link FavoriteFragment},{@link HistoryFragment},{@link DownloadFragment}
     * @返回:
     */
    public MyViewPageAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragmentList) {
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
