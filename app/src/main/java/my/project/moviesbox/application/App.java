package my.project.moviesbox.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;

import com.arialyy.aria.core.Aria;
import com.bumptech.glide.Glide;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.style.MaterialStyle;

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

import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.config.MyExceptionHandler;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.service.FuckCFService;
import my.project.moviesbox.utils.CropUtil;
import my.project.moviesbox.utils.DarkModeUtils;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

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
        DarkModeUtils.init(this);
        // 让API < 29的版本支持TLSv1.3
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        // 忽略Https验证
        HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory());
        handleSSLHandshake();
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
        // 设置最大下载数为1，多个同时下载经常出现下载失败
        Aria.get(this).getDownloadConfig().setMaxTaskNum(3);
        Aria.get(this).getDownloadConfig().setConvertSpeed(true);
        // 检查更新
//        startService(new Intent(getInstance(), CheckUpdateService.class));
//        DialogX.globalStyle = new MaterialYouStyle();
//        DialogX.globalStyle = new IOSStyle();
        DialogX.globalStyle = new MaterialStyle();
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
