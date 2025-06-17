package my.project.moviesbox.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.moviesbox.BuildConfig;
import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.event.UpdateImgEvent;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/29 9:43
 */
public class Utils {
    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("不能实例化...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("上下文应该首先初始化！");
    }

    /**
     * 公共下载目录地址
     */
    public static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    public static final String APP_DATA_PATH = DOWNLOAD_PATH + File.separator + "MoviesBox[" + SharedPreferencesUtils.getDataName() + "]";
    public static final String APP_DOWNLOAD_PATH = APP_DATA_PATH + File.separator + "Downloads";
    public final static String DOWNLOAD_SAVE_PATH = APP_DOWNLOAD_PATH + "/%s/%s/";
    public static final String APP_CRASH_LOGS_PATH = APP_DATA_PATH + File.separator + "CrashLogs";
    public static final String APP_PARSER_LOGS_PATH = APP_DATA_PATH + File.separator + "ParserLogs";

    /**
     * 创建缓存目录
     */
    public static void createDataFolders() {
        createDataFolder(APP_DATA_PATH);
        createDataFolder(APP_DOWNLOAD_PATH);
        createDataFolder(APP_CRASH_LOGS_PATH);
        createDataFolder(APP_PARSER_LOGS_PATH);
    }

    /**
     * 创建数据文件夹
     * @param folderPath
     */
    public static boolean createDataFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return false;
    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad() {
        return (getContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 判断是否有NavigationBar
     * @param activity
     * @return
     */
    public static boolean checkHasNavigationBar(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);
        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;
        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * 统一处理弹出窗
     * @param activity 上下文
     * @param title 标题
     * @param msg 消息
     * @param cancelable 点击外部是否关闭
     * @param positiveButtonTitle 确定按钮标题
     * @param negativeButtonTitle 否定按钮标题
     * @param neutralButtonTitle 中立按钮标题
     * @param positiveButtonListener 确定按钮监听
     * @param negativeButtonListener 否定按钮监听
     * @param neutralButtonListener 中立按钮监听
     */
    public static void showAlert(@NotNull Activity activity,
                                 @NotNull String title, @NotNull String msg, boolean cancelable,
                                 String positiveButtonTitle,
                                 String negativeButtonTitle,
                                 String neutralButtonTitle,
                                 DialogInterface.OnClickListener positiveButtonListener,
                                 DialogInterface.OnClickListener negativeButtonListener,
                                 DialogInterface.OnClickListener neutralButtonListener) {
        WeakReference<Activity> weakActivity = new WeakReference<>(activity);
        if (weakActivity.get() != null && !weakActivity.get().isFinishing()) {
            AlertDialog alertDialog;
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogStyle);
            if (!isNullOrEmpty(positiveButtonTitle))
                builder.setPositiveButton(positiveButtonTitle, positiveButtonListener);
            if (!isNullOrEmpty(negativeButtonTitle))
                builder.setNegativeButton(negativeButtonTitle, negativeButtonListener);
            if (!isNullOrEmpty(neutralButtonTitle))
                builder.setNeutralButton(neutralButtonTitle, neutralButtonListener);
            builder.setTitle(title);
            builder.setMessage(msg);
            builder.setCancelable(cancelable);
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * 单选弹出窗统一处理
     * @param activity 上下文
     * @param title 标题
     * @param items 单选数据数组
     * @param cancelable 点击外部是否关闭
     * @param defaultChoice 默认选择项下标
     * @param listener 点击监听
     */
    public static void showSingleChoiceAlert(@NotNull Activity activity, @NotNull String title, @NotNull String[] items, boolean cancelable, int defaultChoice, @Nullable DialogInterface.OnClickListener listener) {
        WeakReference<Activity> weakActivity = new WeakReference<>(activity);
        if (weakActivity.get() != null && !weakActivity.get().isFinishing()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogStyle);
            builder.setTitle(title);
            Spanned[] spanned = new Spanned[items.length];
            for (int i=0,size=items.length; i<size; i++) {
                spanned[i] = Html.fromHtml(items[i]);
            }
            builder.setSingleChoiceItems(spanned, defaultChoice, listener);
            builder.setCancelable(cancelable);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * 列表弹窗统一处理
     * @param activity 上下文
     * @param title 标题
     * @param items 数据数组
     * @param listener 点击监听
     */
    public static void showSingleListAlert(@NotNull Activity activity, String title, @NotNull String[] items, boolean cancelable, @Nullable DialogInterface.OnClickListener listener) {
        WeakReference<Activity> weakActivity = new WeakReference<>(activity);
        if (weakActivity.get() != null && !weakActivity.get().isFinishing()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogStyle);
            if (!isNullOrEmpty(title))
                builder.setTitle(title);
            Spanned[] spanned = new Spanned[items.length];
            for (int i=0,size=items.length; i<size; i++) {
                spanned[i] = Html.fromHtml(items[i]);
            }
            builder.setItems(spanned, listener);
            builder.setCancelable(cancelable);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * 显示加载框
     * @param activity
     * @param id
     * @return
     */
    public static AlertDialog getProDialog(Activity activity, @StringRes int id) {
        WeakReference<Activity> weakActivity = new WeakReference<>(activity);
        if (weakActivity.get() != null && !weakActivity.get().isFinishing()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(weakActivity.get());
            View view = LayoutInflater.from(weakActivity.get()).inflate(R.layout.dialog_proress, null);
            TextView msg = view.findViewById(R.id.msg);
            msg.setText(weakActivity.get().getString(id));
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.setView(view).create();
            alertDialog.show();
            return alertDialog;
        }
        return null;
    }

    /**
     * 关闭对话框
     * @param alertDialog
     */
    public static void cancelDialog(AlertDialog alertDialog) {
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    /**
     * 选择视频播放器
     *
     * @param url
     */
    public static void selectVideoPlayer(Context context, String url) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse(url), "video/*");
        try {
            context.startActivity(Intent.createChooser(intent, getString(R.string.selectVideoPlayer)));
        } catch (ActivityNotFoundException e) {
            App.getInstance().showToastMsg(getString(R.string.noAppFound), DialogXTipEnum.ERROR);
        }
    }

    /**
     * GlideUrl
     * @param imgUrl
     * @return
     */
    public static GlideUrl getGlideUrl(String imgUrl) {
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        Map<String, String> headerMap = ParserInterfaceFactory.getParserInterface().setImgHeaders();
        if (headerMap != null && headerMap.size() > 0) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.addHeader(key, value);
            }
        }
        return new GlideUrl(imgUrl, builder.build());
    }

    /**
     * 设置默认图片
     * @param imgUrl
     * @param imageView
     */
    public static void setDefaultImage(String imgUrl, ImageView imageView) {
        if (isNullOrEmpty(imgUrl)) {
            imageView.setImageDrawable(context.getDrawable(R.drawable.loading_failed));
            return;
        }
        if (!isNullOrEmpty(imageView))
            clearImageView(imageView);
        RequestOptions options = new RequestOptions()
//                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                .encodeQuality(70);
        GlideApp.with(context)
                .load(getGlideUrl(imgUrl))
//                .override(500)
                .apply(options)
                .placeholder(context.getDrawable(R.drawable.loading))
                .error(context.getDrawable(R.drawable.loading_failed))
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(imageView);
    }

    /**
     * 设置默认图片
     * @param imgUrl
     * @param descUrl
     * @param imageView
     * @param setPalette
     * @param cardView
     * @param textView
     */
    public static void setDefaultImage(String imgUrl, String descUrl, ImageView imageView, boolean setPalette, MaterialCardView cardView, TextView textView, boolean isFavorite) {
        if (isNullOrEmpty(imgUrl)) {
            imageView.setImageDrawable(context.getDrawable(R.drawable.loading_failed));
            return;
        }
        if (!isNullOrEmpty(imageView))
            clearImageView(imageView);
        if (imgUrl.contains("http")) {
            imageView.setImageDrawable(context.getDrawable(R.drawable.loading));
            GlideApp.with(context)
                    .asBitmap()
                    .load(getGlideUrl(imgUrl))
                    .override(500)
                    .apply(new RequestOptions()
                            .encodeQuality(70)
                    )
                    .transition(BitmapTransitionOptions.withCrossFade(500))
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            imageView.setImageDrawable(context.getDrawable(R.drawable.loading_failed));
                            if (!SharedPreferencesUtils.getByPassCF() && isFavorite) // 当开启绕过CF时不更新图片（效率低成功率不高，建议手动刷新）
                            {
                                imageView.setImageDrawable(context.getDrawable(R.drawable.loading));
                                EventBus.getDefault().post(new UpdateImgEvent(imgUrl, descUrl));
                            }
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (imgUrl.equals(imageView.getTag(R.id.imageid))) {
                                imageView.setImageBitmap(resource);
                                if (setPalette && !DarkModeUtils.isDarkMode(context)) {
                                    // 设置Palette
                                    Palette.from(resource).generate(palette -> {
                                        Palette.Swatch swatch = palette.getDominantSwatch();
                                        if (swatch != null) {
                                            int startColor = cardView.getCardBackgroundColor().getDefaultColor();
                                            int endColor = swatch.getRgb();
                                            ValueAnimator colorAnimator = ValueAnimator.ofArgb(startColor, endColor);
                                            colorAnimator.setDuration(500);
                                            colorAnimator.addUpdateListener(animator -> {
                                                int animatedColor = (int) animator.getAnimatedValue();
                                                cardView.setCardBackgroundColor(ColorStateList.valueOf(animatedColor));
                                                cardView.setStrokeColor(ColorStateList.valueOf(animatedColor));
                                            });
                                            colorAnimator.start();

                                            int startTextColor = textView.getCurrentTextColor();
                                            int endTextColor = swatch.getTitleTextColor();
                                            ValueAnimator textColorAnimator = ValueAnimator.ofArgb(startTextColor, endTextColor);
                                            textColorAnimator.setDuration(500);
                                            textColorAnimator.addUpdateListener(animator -> {
                                                int animatedTextColor = (int) animator.getAnimatedValue();
                                                textView.setTextColor(animatedTextColor);
                                            });
                                            textColorAnimator.start();
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 当图片加载被取消时调用，可以在这里清除或重置imageView
                            imageView.setImageDrawable(null);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }
                    });
        } else if (imgUrl.contains("base64")) {
            Glide.with(context)
                    .load(imgUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(imageView);
        } else {
            File file = new File(imgUrl);
            Glide.with(context)
                    .load(file)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(imageView);
        }
    }

    /**
     * 用于CENTER_INSIDE图片背景模糊
     * @param imgUrl
     * @param imageView
     */
    public static void setImgViewBlurBg(String imgUrl, ImageView imageView) {
        if (!isNullOrEmpty(imageView))
            clearImageView(imageView);
        Glide.with(context)
                .load(imgUrl)
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(10, 8)))
                .into(imageView);
    }

    public static void setImgViewBg(String imgUrl, String descUrl, ImageView imageView) {
        if (isNullOrEmpty(imgUrl)) {
            imageView.setImageDrawable(context.getDrawable(R.drawable.loading_failed));
            return;
        }
        if (!isNullOrEmpty(imageView))
            clearImageView(imageView);
        if (imgUrl.contains("http")) {
            imageView.setImageDrawable(context.getDrawable(R.drawable.loading));
            GlideApp.with(context)
                    .asBitmap()
                    .override(500)
                    .load(getGlideUrl(imgUrl))
                    .apply(RequestOptions.bitmapTransform( new BlurTransformation(15, 5)))
                    .transition(BitmapTransitionOptions.withCrossFade(500))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (imgUrl.equals(imageView.getTag(R.id.imageid)))
                                imageView.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 当图片加载被取消时调用，可以在这里清除或重置imageView
                            imageView.setImageDrawable(null);
                        }

                        @Override
                        public void onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable Drawable errorDrawable) {
                            EventBus.getDefault().post(new UpdateImgEvent(imgUrl, descUrl));
                        }
                    });
        } else {
            File file = new File(imgUrl);
            Glide.with(context)
                    .load(file)
                    .override(500)
                    .apply(RequestOptions.bitmapTransform( new BlurTransformation(15, 5)))
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(imageView);
        }
    }

    public static void loadVideoScreenshot(final Context context, String uri, String defaultImg, ImageView imageView, long frameTimeMicros) {
        RequestOptions options = new RequestOptions().frame(frameTimeMicros);
        GlideApp.with(context)
                .asBitmap()
                .load(uri)
                .apply(options)
                .transition(BitmapTransitionOptions.withCrossFade(500))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (resource.getAllocationByteCount() > 100)
                            imageView.setImageBitmap(resource);
                        else
                            onLoadFailed(null);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // 当图片加载被取消时调用，可以在这里清除或重置imageView
                        imageView.setImageDrawable(null);
                    }

                    @Override
                    public void onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable Drawable errorDrawable) {
                        GlideApp.with(context).load(defaultImg).apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 5))).into(imageView);
                    }
                });
    }

    private static void clearImageView(ImageView imageView) {
        GlideApp.with(context).clear(imageView);
    }

    /**
     * 获取string.xml文本
     * @param id
     * @return
     */
    public static String getString(@StringRes int id) {
        return getContext().getResources().getString(id);
    }

    public static String[] getArray(@ArrayRes int id) {
        return getContext().getResources().getStringArray(id);
    }

    public static int[] getIntArray(@ArrayRes int id) {
        return getContext().getResources().getIntArray(id);
    }

    /**
     * 获得NavigationBar的高度 +15
     */
    public static int getNavigationBarHeight(Activity activity) {
        int result = 0;
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && checkHasNavigationBar(activity)) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result + 15;
    }

    /**
     * 通过浏览器打开
     * @param url
     */
    public static void viewInChrome(Context context, String url) {
        if (!url.startsWith("http"))
            url = ParserInterfaceFactory.getParserInterface().getDefaultDomain() + url;
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true); //显示网页标题
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    // 两次点击按钮之间的点击间隔不能少于500毫秒
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    public static void fadeIn( View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation animation = new AlphaAnimation(0F, 1F);
        animation.setDuration(800);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setEnabled(true);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeOut(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }
        view.setEnabled(false);
        Animation animation = new AlphaAnimation(1F, 0F);
        animation.setDuration(800);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animation);
    }

    private static String stringForHoursAndMinutes(long timeMs) {
        if (timeMs > 0L && timeMs < 86400000L) {
            long totalSeconds = timeMs / 1000L;
            int minutes = (int)(totalSeconds / 60L % 60L);
            int hours = (int)(totalSeconds / 3600L);
            StringBuilder stringBuilder = new StringBuilder();
            Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
            return hours > 0 ? mFormatter.format("%d:%02d", hours, minutes).toString() : mFormatter.format("%02d", minutes).toString();
        } else {
            return "00:00";
        }
    }

    public static boolean videoHasComplete(long playPosition, long videoDuration) {
        return stringForHoursAndMinutes(playPosition).equals(stringForHoursAndMinutes(videoDuration));
    }

    public static String getNetFileSizeDescription(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    /**
     * 检查网络是否是wifi
     *
     * @param context
     * @return
     */
    public static boolean isWifi() {
        ConnectivityManager connectivityManager =(ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo =connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null &&activeNetworkInfo.getType() == connectivityManager.TYPE_WIFI){
            return true;
        }
        return false;
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return "http://"+int2ip(i)+":8080";
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 隐藏软键盘
     * @param view
     */
    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * dp转px
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null)
            return true;
        if (obj instanceof List<?>)
            return ((List<?>) obj).isEmpty();
        return false;
    }

    /**
     * 服务是否在运行
     * @param context
     * @param serviceClass
     * @return
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否连接WIFI
     * @param context
     * @return
     */
    public static boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }
        return false;
    }

    public static String getASVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 哈希函数将超长的文件名转换为固定长度的字符串
     * @param fileName
     * @return
     */
    public static String getHashedFileName(String fileName) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileName.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存文件
     * @param processedData
     * @param fileName
     * @return
     */
    public static String writeToFile(byte[] processedData, String fileName) {
        FileOutputStream fos = null;
        try {
            File file = new File(context.getFilesDir(), fileName);
            fos = new FileOutputStream(file);
            fos.write(processedData);
            fos.flush();
            LogUtil.logInfo("写入文件到私有目录", file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        File directory = context.getFilesDir();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    public static void setVibration(View view) {
        if (!isNullOrEmpty(view))
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }
}
