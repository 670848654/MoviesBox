package my.project.moviesbox.config;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
  * @包名: my.project.moviesbox.config
  * @类名: ParallaxTransformer
  * @描述: ViewPager2切换动画
  * @作者: Li Z
  * @日期: 2024/2/20 16:50
  * @版本: 1.0
 */
public class ParallaxTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        int width = page.getWidth();
        if (position <= -1f) {
            page.setScrollX(0);
        } else if (position < 1f) {

            if (position < 0f) {
                page.setScrollX((int) (width * 0.75f * position));
            } else {
                page.setScrollX((int) (width * 0.75f * position));
            }

        } else {
            page.setScrollX(0);
        }
    }
}