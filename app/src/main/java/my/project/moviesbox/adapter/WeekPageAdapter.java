package my.project.moviesbox.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.List;

import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.view.WeekFragment;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: WeekPageAdapter
  * @描述: 星期时间表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 17:29
  * @版本: 1.0
 */
public class WeekPageAdapter extends FragmentStateAdapter {
    private List<WeekDataBean> weekDataBeans;
    private HashMap<Integer, Fragment> mFragmentHashMap = new HashMap<>();

     /**
      * @方法名称: WeekPageAdapter
      * @方法描述: 构造方法
      * @日期: 2024/1/22 19:13
      * @作者: Li Z
      * @param weekDataBeans {@link WeekDataBean}
      * @return
      */
    public WeekPageAdapter(@NonNull FragmentActivity fragmentActivity, List<WeekDataBean> weekDataBeans) {
        super(fragmentActivity);
        this.weekDataBeans = weekDataBeans;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = mFragmentHashMap.get(position);
        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = new WeekFragment(weekDataBeans.get(0).getWeekItems());
                    break;
                case 1:
                    fragment = new WeekFragment(weekDataBeans.get(1).getWeekItems());
                    break;
                case 2:
                    fragment = new WeekFragment(weekDataBeans.get(2).getWeekItems());
                    break;
                case 3:
                    fragment = new WeekFragment(weekDataBeans.get(3).getWeekItems());
                    break;
                case 4:
                    fragment = new WeekFragment(weekDataBeans.get(4).getWeekItems());
                    break;
                case 5:
                    fragment = new WeekFragment(weekDataBeans.get(5).getWeekItems());
                    break;
                case 6:
                    fragment = new WeekFragment(weekDataBeans.get(6).getWeekItems());
                    break;
            }
            mFragmentHashMap.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return weekDataBeans.size();
    }
}
