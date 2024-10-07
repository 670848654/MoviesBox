package my.project.moviesbox.custom;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;

public class CustomBottom {

    private static PopupWindow popupWindow;
    private static WeakReference<PopupWindow> popupWindowRef;

    private Activity activity;

    public CustomBottom(Activity activity) {
        this.activity = activity;
    }

    public PopupWindow getInstance() {
        popupWindow = popupWindowRef != null ? popupWindowRef.get() : null;
        if (popupWindow == null) {
            popupWindow = new PopupWindow();
            popupWindowRef = new WeakReference<>(popupWindow);
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewContent = inflater.inflate(R.layout.custom_bottom, null);
            popupWindow.setClippingEnabled(false);
            popupWindow.setContentView(viewContent);
            popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            /*if (Utils.checkHasNavigationBar(activity)) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) viewContent.getLayoutParams();
                layoutParams.setMargins(Utils.dpToPx(activity, 16),
                        Utils.dpToPx(activity, 16),
                        Utils.dpToPx(activity, 16),
                        Utils.getNavigationBarHeight(activity) + 1);
                viewContent.setLayoutParams(layoutParams);
            }*/
            popupWindow.setAnimationStyle(R.style.BottomDialogAnimation);
        }
        return popupWindow;
    }

    public void showToast() {
        App.mainHandler.post(() -> {
            if (activity.isFinishing()) return;
            popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
            final FloatingActionButton fab = popupWindow.getContentView().findViewById(R.id.close);
            popupWindow.getContentView().setOnClickListener(v -> {
//                    ToastUtils.show(activity, tv.getText().toString());
            });
            fab.setOnClickListener(view -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                destroy();
                activity.finish();
            });
        });
        handler.removeMessages(1);
        handler.sendEmptyMessageDelayed(1, 2000);
    }

    private void destroy() {
        handler.removeCallbacksAndMessages(null);
        if (popupWindowRef != null) {
            PopupWindow popupWindow = popupWindowRef.get();
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            popupWindowRef.clear();
            popupWindowRef = null;
        }
    }

    public static void dismiss() {
        popupWindow.dismiss();
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dismiss();
        }
    };

    /**
     * 从dp单位转换为px
     *
     * @param dp dp值
     * @return 返回转换后的px值
     */
    private static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
