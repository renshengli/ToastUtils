package com.pink.toastutils;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by shawn-li on 17/9/29.
 */

public class SystemToast implements IToast {
    private Toast mToast;
    private LayoutInflater mInflater;
    View view;
    Resources mRes;

    public SystemToast(Context context, int yOffset) {
        mRes = context.getResources();
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL, 0, yOffset);
        mInflater = LayoutInflater.from(context);
        view = mInflater.inflate(R.layout.toast_dialog_base, null);
        mToast.setView(view);
    }

    @Override
    public void show() {
        mToast.show();
    }

    @Override
    public void setText(int resId) {
        setText(mRes.getString(resId));
    }

    @Override
    public void setText(CharSequence text) {
        TextView v = (TextView) view.findViewById(R.id.toast_msg);
        v.setText(text);
    }

    public void setmDuration(int mDuration) {
        mToast.setDuration(mDuration);
    }

    @Override
    public void cancel() {
        mToast.cancel();
    }
}
