package my.project.moviesbox.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.splashscreen.SplashScreen;

/**
  * @包名: my.project.moviesbox.view
  * @类名: RoutingActivity
  * @描述: 开屏视图
  * @作者: Li Z
  * @日期: 2024/1/26 9:50
  * @版本: 1.0
 */
public class RoutingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        // Keep the splash screen visible for this Activity.
        splashScreen.setKeepOnScreenCondition(() -> true );
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
