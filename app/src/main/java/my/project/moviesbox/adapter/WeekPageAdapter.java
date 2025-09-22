package my.project.moviesbox.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.view.fragment.WeekFragment;

/**
  * @包名: my.project.moviesbox.adapter
  * @类名: WeekPageAdapter
  * @描述: 星期时间表适配器
  * @作者: Li Z
  * @日期: 2024/1/22 17:29
  * @版本: 1.0
 */
public class WeekPageAdapter extends FragmentStateAdapter {
    private final List<WeekDataBean> weekDataBeans;

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
        return new WeekFragment(weekDataBeans.get(position).getWeekItems());
    }

    @Override
    public int getItemCount() {
        return weekDataBeans.size();
    }
}
