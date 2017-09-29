package com.pink.toastutils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class ToastDialog extends Dialog implements IToast {
    private static BlockingQueue<WeakReference<ToastDialog>> mQueue =
            new LinkedBlockingDeque<WeakReference<ToastDialog>>();
    private static AtomicInteger mAtomicInteger = new AtomicInteger(0);
    private static final int LONG_DELAY = 3500; // 3.5 seconds
    private static final int SHORT_DELAY = 2000; // 2 seconds
    private static int mStatusBarHeight = -1;
    private static Handler mHandler = new Handler();
    private View mLayout = null;
    private long mDuration = Toast.LENGTH_SHORT;

    public ToastDialog(Context context, int theme, int yOffset) {
        super(context, theme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = inflater.inflate(R.layout.toast_dialog_base, null);
        super.addContentView(mLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        super.setContentView(mLayout);
        super.setCancelable(false);
        Window dialogWindow = super.getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogWindow.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (yOffset > 0) {
            lp.y = yOffset;
        }
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);
        dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    @Override
    public void show() {
        //super.show();
        mQueue.offer(new WeakReference<ToastDialog>(this));
        if (0 == mAtomicInteger.get()) {
            mAtomicInteger.incrementAndGet();
            mHandler.post(mActivite);
        }
    }

    @Override
    public void setText(int resId) {
        ((TextView) mLayout.findViewById(R.id.toast_msg)).setText(resId);
    }

    @Override
    public void setText(CharSequence text) {
        ((TextView) mLayout.findViewById(R.id.toast_msg)).setText(text);
    }

    public void setmDuration(int mDuration) {
        if (mDuration == Toast.LENGTH_SHORT) {
            this.mDuration = SHORT_DELAY;
        } else {
            this.mDuration = LONG_DELAY;
        }
    }

    @Override
    public void cancel() {
        if (0 == mAtomicInteger.get() && mQueue.isEmpty()) {
            return;
        }
        if (mQueue.peek() != null && this.equals(mQueue.peek().get())) {
            mHandler.removeCallbacks(mActivite);
            mHandler.post(mHide);
            mHandler.post(mActivite);
        }
    }

    private void handleShow() {
        try {
            super.show();
        } catch(Exception e) {
            // may be application exceiton
        }
    }

    private void handleHide() {
        try {
            super.cancel();
        } catch (Exception e) {
            // not attached to window manager
        }
        mQueue.poll();
    }

    private static void activeQueue() {
        WeakReference<ToastDialog> toastWR = mQueue.peek();
        if (toastWR == null || toastWR.get() == null) {
            mAtomicInteger.decrementAndGet();
        } else {
            ToastDialog toast = toastWR.get();
            mHandler.post(toast.mShow);
            mHandler.postDelayed(toast.mHide, toast.mDuration);
            mHandler.postDelayed(mActivite, toast.mDuration);
        }

    }

    private final Runnable mShow = new Runnable() {
        @Override
        public void run() {
            handleShow();
        }
    };

    private final Runnable mHide = new Runnable() {
        @Override
        public void run() {
            handleHide();
        }
    };

    private final static Runnable mActivite = new Runnable() {
        @Override
        public void run() {
            activeQueue();
        }
    };

}
