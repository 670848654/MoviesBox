package my.project.moviesbox.photoView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class PhotoViewWrapper extends FrameLayout {
    private boolean isParentInterceptionDisallowed = false;

    public PhotoViewWrapper(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        isParentInterceptionDisallowed = disallowIntercept;
        if (disallowIntercept) {
            // PhotoView wants to disallow parent interception, let it be.
            getParent().requestDisallowInterceptTouchEvent(isParentInterceptionDisallowed); // don't ban wrapper itself
        }
        else {
            // PhotoView wants to allow parent interception, we need to re-check it.
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // always false when up or cancel event,
        // which will allow parent interception normally.
        boolean isMultiTouch = ev.getPointerCount() > 1;

        // re-check if it's multi touch
        getParent().requestDisallowInterceptTouchEvent(
                isParentInterceptionDisallowed || isMultiTouch
        );
        return false;
    }
}
