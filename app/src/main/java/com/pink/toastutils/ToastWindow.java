package com.pink.toastutils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class ToastWindow implements IToast {
    private static BlockingQueue<WeakReference<ToastWindow>> mQueue =
            new LinkedBlockingDeque<WeakReference<ToastWindow>>();
    private static AtomicInteger mAtomicInteger = new AtomicInteger(0);
    private static final int LONG_DELAY = 3500; // 3.5 seconds
    private static final int SHORT_DELAY = 2000; // 2 seconds
    private static Handler mHandler = new Handler();
    private View mLayout = null;
    private long mDuration = Toast.LENGTH_SHORT;
    private Context mContext;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;

    public ToastWindow(Context context, int theme, int yOffset) {
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = inflater.inflate(R.layout.toast_dialog_base, null);
        mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        if (yOffset > 0) {
            mParams.y = yOffset;
        }
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.windowAnimations = android.R.style.Animation_Toast;
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mParams.setTitle("");
        mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager
                .LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mParams.gravity = Gravity.TOP|Gravity.FILL_HORIZONTAL;

    }

    @Override
    public void show() {
        mQueue.offer(new WeakReference<ToastWindow>(this));
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
            if (mLayout != null) {
                if (mLayout.getParent() != null) {
                    mWindowManager.removeView(mLayout);
                }
                mWindowManager.addView(mLayout, mParams);
            }
        } catch(Exception e) {
        }
    }

    private void handleHide() {
        try {
            if (mLayout != null) {
                if (mLayout.getParent() != null) {
                    mWindowManager.removeView(mLayout);
                    mQueue.poll();
                }
                mLayout = null;
            }
        } catch (Exception e) {
            // not attached to window manager
        }
        mQueue.poll();
    }

    private static void activeQueue() {
        WeakReference<ToastWindow> toastWR = mQueue.peek();
        if (toastWR == null || toastWR.get() == null) {
            mAtomicInteger.decrementAndGet();
        } else {
            ToastWindow toast = toastWR.get();
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
