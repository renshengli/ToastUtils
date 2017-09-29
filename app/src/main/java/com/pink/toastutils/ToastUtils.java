package com.pink.toastutils;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by shawn-li on 17/9/29.
 */

public class ToastUtils {
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    private static boolean mIsNotificationEnabled = false;
    private static boolean mIsGetNotificationEnabled = false;
    private static int mStatusBarHeight = -1;
    //在MX2上QQToast会贴顶显示，需要指定statusBarHeight为yOffset
    private static boolean mIsMX2 = false;

    private static IToast getToast(Context context) {
        if (!mIsGetNotificationEnabled) {
            mIsNotificationEnabled = isNotificationEnabled(context);
            mStatusBarHeight = getStatusBarHeight(context);
            mIsMX2 = isMX2();
        }
        mIsNotificationEnabled = true;
        if (mIsNotificationEnabled) {
            return new SystemToast(context, mIsMX2 ? mStatusBarHeight : 0);
        } else {
            return new ToastDialog(context, R.style.toast_dialog, mIsMX2 ? mStatusBarHeight : 0);
        }
    }

    /**
     * 获取系统状态栏高度
     * @return
     */
    private static int getStatusBarHeight(Context context) {
        if (mStatusBarHeight != -1) {
            return mStatusBarHeight;
        }
        int height = 0;
        try {
            height = Resources.getSystem().getDimensionPixelSize(
                    Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
        } catch (Exception e) {
            //默认状态栏高度是25dp
            height = (int)(context.getResources().getDisplayMetrics().density * 25 + 0.5);
        }
        mStatusBarHeight = height;
        return height;
    }

    /**
     * 取消息通知系统开关状态，4.4以上版本可用
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean isNotificationEnabled(Context context) {
        try {
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            Class appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE,
                    Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int) opPostNotificationValue.get(Integer.class);
            return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager
                    .MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean isMX2(){
        if (android.os.Build.BOARD.contains("mx2")) {
            return true;
        } else {
            return false;
        }
    }

    public static IToast makeText(Context context, CharSequence text, int duration) {
        IToast toast = getToast(context);
        toast.setText(text);
        toast.setmDuration(duration);
        return toast;
    }

    public static IToast makeText(Context context, int resId, int duration) throws Resources.NotFoundException {
        IToast toast = getToast(context);
        toast.setText(resId);
        toast.setmDuration(duration);
        return toast;
    }
}
