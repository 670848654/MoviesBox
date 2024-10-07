package my.project.moviesbox.view;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.permissionx.guolindev.PermissionX;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.enums.DialogXTipEnum;

/**
  * @包名: my.project.moviesbox.view
  * @类名: RoutingActivity
  * @描述: 开屏视图
  * @作者: Li Z
  * @日期: 2024/1/26 9:50
  * @版本: 1.0
 */
public class RoutingActivity extends AppCompatActivity {
    private App application;
    private SplashScreen splashScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        if (application == null)
            application = (App) getApplication();
        checkPermission();
    }

    /**
     * 检查权限
     */
    private void checkPermission() {
        if (gtSdk33()) {
            // Android13以上通知权限
            PermissionX.init(this)
                    .permissions(PermissionX.permission.POST_NOTIFICATIONS)
                    .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, getString(R.string.notificationsPermissionsTitle), getString(R.string.permissionsAgree), getString(R.string.permissionsDisagree)))
                    .request((allGranted, grantedList, deniedList) -> openMain());
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Android11以下需要存储权限
            PermissionX.init(this)
                    .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, getString(R.string.writeExternalStoragePermissionsTitle), getString(R.string.permissionsAgree), getString(R.string.permissionsDisagree)))
                    .request((allGranted, grantedList, deniedList) -> {
                        if (deniedList.size() > 0)
                            application.showToastMsg(getString(R.string.noPermissionsContent), DialogXTipEnum.WARNING);
                        openMain();
                    });
        } else
            openMain();
    }

    private boolean gtSdk33() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    private void openMain() {
        // Keep the splash screen visible for this Activity.
        splashScreen.setKeepOnScreenCondition(() -> true );
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
