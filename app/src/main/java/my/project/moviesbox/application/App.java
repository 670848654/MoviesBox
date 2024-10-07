package my.project.moviesbox.application;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import com.arialyy.aria.core.Aria;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.style.IOSStyle;

import org.conscrypt.Conscrypt;

import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import my.project.moviesbox.R;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.config.MyExceptionHandler;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.service.FuckCFService;
import my.project.moviesbox.utils.CropUtil;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoUtils;

/**
  * @包名: my.project.moviesbox.application
  * @类名: App
  * @描述: Application
  * @作者: Li Z
  * @日期: 2024/1/22 19:18
  * @版本: 1.0
 */
public class App extends Application {
    private static App appContext;
    private static Map<String, Activity> destoryMap = new HashMap<>();
    public static Handler mainHandler;
    private static Map<String, String> cookies;

    public static App getInstance() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        mainHandler = new Handler(this.getMainLooper());
        SharedPreferencesUtils.shouldSetDataName();
        ConfigManager.init(this);
        Utils.init(this);
        VideoUtils.init(this);
        DarkModeUtils.init(this);
        // 让API < 29的版本支持TLSv1.3
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        // 忽略Https验证
        HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory());
        handleSSLHandshake();
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
        // 设置最大下载数为1，多个同时下载经常出现下载失败
        Aria.get(this).getDownloadConfig().setMaxTaskNum(1);
        Aria.get(this).getDownloadConfig().setConvertSpeed(true);
        // 检查更新
//        startService(new Intent(getInstance(), CheckUpdateService.class));
//        DialogX.globalStyle = new MaterialYouStyle();
        DialogX.globalStyle = new IOSStyle();
        DialogX.onlyOnePopTip = false;
        DialogX.init(this);
    }

    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustManagers = new TrustManager[] {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // 信任所以证书
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
        } catch (Exception e) {

        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory();
        }
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    public void showToastMsg(String msg, DialogXTipEnum tipEnum) {
        switch (tipEnum) {
            case SUCCESS:
                PopTip.show(msg).iconSuccess();
                break;
            case ERROR:
                PopTip.show(msg).iconError();
                break;
            case WARNING:
                PopTip.show(msg).iconWarning();
                break;
            default:
                PopTip.show(msg);
                break;
        }
    }

    public void showImgSnackbarMsg(View view, @DrawableRes int iconRes, @ColorInt int iconTintColor, String msg) {
        Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        // 设置自定义布局
        LayoutInflater inflater = LayoutInflater.from(this);
        View snackbarView = inflater.inflate(R.layout.custom_snackbar_layout, null);
        layout.addView(snackbarView, 0);
        // 设置图标
        ImageView icon = snackbarView.findViewById(R.id.icon);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(iconTintColor));
        // 设置文本
        TextView textView = snackbarView.findViewById(R.id.title);
        textView.setText(msg);
        textView.setTextColor(appContext.getColor(R.color.night_text_color));
        snackbar.setBackgroundTint(appContext.getColor(R.color.pink200));
        snackbar.show();
        performScaleAnimation(icon);
    }

    public static void performScaleAnimation(View v) {
        v.setScaleX(0);
        v.setScaleY(0);
        v.setVisibility(View.INVISIBLE);
        // 放大动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, View.SCALE_X, 0f, 1.2f);
        scaleX.setDuration(500);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, View.SCALE_Y, 0f, 1.2f);
        scaleY.setDuration(500);

        // 弹性动画
        ObjectAnimator scaleXBack = ObjectAnimator.ofFloat(v, View.SCALE_X, 1f, 0.8f, 1.2f, 1f);
        scaleXBack.setDuration(500);
        scaleXBack.setInterpolator(new BounceInterpolator());
        ObjectAnimator scaleYBack = ObjectAnimator.ofFloat(v, View.SCALE_Y, 1f, 0.8f, 1.2f, 1f);
        scaleYBack.setDuration(500);
        scaleYBack.setInterpolator(new BounceInterpolator());

        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.play(scaleX).with(scaleY);
        AnimatorSet bounceSet = new AnimatorSet();
        bounceSet.play(scaleXBack).with(scaleYBack);

        scaleSet.start();
        v.setVisibility(View.VISIBLE);
        scaleSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bounceSet.start();
            }
        });
    }

    public static void addDestroyActivity(Activity activity, String activityName) {
        destoryMap.put(activityName, activity);
    }

    public static void destroyActivity(String activityName) {
        Iterator<Map.Entry<String, Activity>> iterator = destoryMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Activity> entry = iterator.next();
            if (activityName.equals(entry.getKey())) {
                entry.getValue().finish();
                iterator.remove();
            }
        }
    }

    public static void removeDestroyActivity(String activityName) {
        destoryMap.remove(activityName);
    }

    public static void startMyService(String url, String type) {
        Intent intent = new Intent(appContext, FuckCFService.class);
        intent.putExtra("url", url);
        intent.putExtra("type", type);
        appContext.startService(intent);
    }

    public static void setCookies(String cookie) {
        cookies = new HashMap<>();
        cookies.put("Cookie", cookie);
    }

    public static Map<String, String> getCookies() {
        return cookies;
    }
}
