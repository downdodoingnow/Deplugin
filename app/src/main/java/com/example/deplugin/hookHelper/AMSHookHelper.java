package com.example.deplugin.hookHelper;

import android.os.Handler;
import android.util.Log;

import com.example.deplugin.Constants;
import com.example.deplugin.hookHelper.hookInvocationHandler.AMSHookHelperInvocationHandler;
import com.example.deplugin.hookHelper.hookInvocationHandler.ActivityThreadHandler;
import com.example.deplugin.hookHelper.hookInvocationHandler.IPackageManagerHandler;
import com.example.deplugin.utils.RefInvoke;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class AMSHookHelper {
    private static final String TAG = Constants.TAG + "AMSHookHelper";

    public static void hookAMN() {
        try {
            Class<?> mActivityManagerCls = RefInvoke.getClass("android.app.ActivityManager");
            Object mIActivityManagerSingletonObj = RefInvoke.getStaticFieldValue(RefInvoke.getField(mActivityManagerCls, "IActivityManagerSingleton"), mActivityManagerCls);
            Class<?> mIActivityManagerCls = RefInvoke.getClass("android.app.IActivityManager");
            if (null != mIActivityManagerSingletonObj) {
                Field mInstanceField = RefInvoke.getField("android.util.Singleton", "mInstance");
                Object mInstance = RefInvoke.getFieldValue(mInstanceField, mIActivityManagerSingletonObj);

                Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{mIActivityManagerCls}, new AMSHookHelperInvocationHandler(mInstance));
                RefInvoke.setFieldValue(mInstanceField, mIActivityManagerSingletonObj, proxy);
            } else {
                Log.i(TAG, "IActivityManagerSingleton not exists");
            }
        } catch (Exception e) {
            Log.i(TAG, "hook ATM failed " + e);
        }
    }

    public static void hookH() {
        try {
            Object sCurrentActivityThread = RefInvoke.getStaticFieldValue(RefInvoke.getField("android.app.ActivityThread", "sCurrentActivityThread"), RefInvoke.getClass("android.app.ActivityThread"));

            Field mHField = RefInvoke.getField(sCurrentActivityThread.getClass(), "mH");
            Handler mH = (Handler) RefInvoke.getFieldValue(mHField, sCurrentActivityThread);

            RefInvoke.setFieldValue(RefInvoke.getField(Handler.class, "mCallback"), mH, new ActivityThreadHandler(mH));
            Log.i(TAG, "hook H complete");
        } catch (Exception e) {
            Log.i(TAG, "hook H failed " + e);
        }
    }

    public static void hookPkgManager() {
        try {
            Class<?> activityThreadCls = RefInvoke.getClass("android.app.ActivityThread");
            Object sPackage = RefInvoke.on(activityThreadCls, "getPackageManager").invoke();

            Class<?> cls = RefInvoke.getClass("android.content.pm.IPackageManager");
            Object object = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{cls}, new IPackageManagerHandler(sPackage));

            RefInvoke.setStaticFieldValue(RefInvoke.getField(activityThreadCls, "sPackageManager"), activityThreadCls, object);
        } catch (Exception e) {
            Log.e(TAG, "hook package manager failed " + e.getMessage());
        }
    }
}
