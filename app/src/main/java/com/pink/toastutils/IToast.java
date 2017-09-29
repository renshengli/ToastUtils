package com.pink.toastutils;

public interface IToast {
    void show();
    void setText(int resId);
    void setText(CharSequence text);
    void setmDuration(int mDuration);
    void cancel();
}
