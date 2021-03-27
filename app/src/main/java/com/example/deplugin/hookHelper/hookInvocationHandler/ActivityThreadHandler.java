package com.example.deplugin.hookHelper.hookInvocationHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.deplugin.Constants;
import com.example.deplugin.DePluginApplication;
import com.example.deplugin.classLoader.DeHostDexClassloader;
import com.example.deplugin.utils.DePluginSP;
import com.example.deplugin.utils.RefInvoke;
import com.example.deplugin.utils.Utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import dalvik.system.DexClassLoader;

public class ActivityThreadHandler implements Handler.Callback {
    private static final String TAG = Constants.TAG + "ATHandler";
    private static final int EXECUTE_TRANSACTION = 159;
    private Handler mBase;

    public ActivityThreadHandler(Handler handler) {
        mBase = handler;
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        int what = message.what;
        switch (what) {
            case EXECUTE_TRANSACTION:
                Object object = message.obj;
                try {
                    List<Object> mActivityCallbacks = RefInvoke.on(object, "getCallbacks").invoke();
                    Class<?> mLaunchActivityItemCls = RefInvoke.getClass("android.app.servertransaction.LaunchActivityItem");
                    for (Object obj : mActivityCallbacks) {
                        if (mLaunchActivityItemCls.isInstance(obj)) {
                            Intent intent = getIntent(mLaunchActivityItemCls, obj);
                            if (null == intent) {
                                break;
                            }
                            String path = intent.getStringExtra(Constants.DEX_PATH);
                            if (TextUtils.isEmpty(path)) {
                                Log.i(TAG, "dex path is empty,so do need replace class loader");
                                break;
                            }
                            //replaceClassloader(mLaunchActivityItemCls, obj, path);
                            replace(intent);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getActivityToken failed " + e.getMessage());
                }
                break;
            default:

        }
        mBase.handleMessage(message);
        return true;
    }

    private Intent getIntent(Class<?> mLaunchActivityItemCls, Object obj) {
        try {
            Field field = RefInvoke.getField(mLaunchActivityItemCls, "mIntent");
            if (null == field) {
                Log.i(TAG, "can not get start up intent field");
                return null;
            }
            Intent intent = (Intent) field.get(obj);
            Log.i(TAG, "activity thread intent is " + intent);
            return intent;
        } catch (Exception e) {
            Log.e(TAG, "getIntent failed " + e.getMessage());
        }
        return null;
    }

    private void replaceClassloader(Class<?> mLaunchActivityItemCls, Object obj, String path) throws Exception {
        Object sCurrentActivityThread = RefInvoke.getStaticFieldValue(RefInvoke.getField("android.app.ActivityThread", "sCurrentActivityThread"), RefInvoke.getClass("android.app.ActivityThread"));
        Field mPackagesField = RefInvoke.getField(sCurrentActivityThread.getClass(), "mPackages");
        if (null == mPackagesField) {
            Log.i(TAG, "get mPackages field failed");
            return;
        }
        ArrayMap mPackages = (ArrayMap) mPackagesField.get(sCurrentActivityThread);
        if (null == mPackages) {
            Log.i(TAG, "can not get mPackages");
            return;
        }
        ApplicationInfo applicationInfo = Utils.generateApplicationInfo(DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, ""));
        if (null != applicationInfo) {
            Object defaultCompatibilityInfo = RefInvoke.getStaticFieldValue(RefInvoke.getField("android.content.res.CompatibilityInfo", "DEFAULT_COMPATIBILITY_INFO"), RefInvoke.getClass("android.content.res.CompatibilityInfo"));
            Object loadedApk = RefInvoke.on(sCurrentActivityThread, "getPackageInfo", ApplicationInfo.class, RefInvoke.getClass("android.content.res.CompatibilityInfo"), int.class).invoke(applicationInfo, defaultCompatibilityInfo, Context.CONTEXT_INCLUDE_CODE);

            String pluginPkgName = applicationInfo.packageName;

            if (!TextUtils.isEmpty(pluginPkgName)) {
                Log.i(TAG, "plugin pkg name is " + pluginPkgName);
                replacePkgName(mLaunchActivityItemCls, obj, pluginPkgName);
                setClassloader(loadedApk, path);
                mPackages.put(pluginPkgName, new WeakReference<>(loadedApk));
            } else {
                Log.i(TAG, "get plugin pkg name failed");
            }
        } else {
            Log.i(TAG, "can not get application info");
        }
    }

    private void setClassloader(Object loadedApk, String path) throws Exception {
        Log.i(TAG, "dex path is " + path);
        DexClassLoader dexClassLoader = DeHostDexClassloader.getInstance().getDexClassLoader(DePluginApplication.getContext(), path);
        RefInvoke.setFieldValue(RefInvoke.getField(loadedApk.getClass(), "mClassLoader"), loadedApk, dexClassLoader);
    }

    private void replacePkgName(Class<?> mLaunchActivityItemCls, Object obj, String pkgName) throws Exception {
        ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldValue(RefInvoke.getField(mLaunchActivityItemCls, "mInfo"), obj);
        activityInfo.applicationInfo.packageName = pkgName;
    }

    private boolean replace(Intent intent) throws Exception {
        Intent realIntent = intent.getParcelableExtra(Constants.START_UP_INTENT);
        if (null == realIntent) {
            return false;
        }
        Log.i(TAG, "origin start up Intent is " + realIntent);
        ComponentName componentName = realIntent.getComponent();
        if (null != componentName) {
            intent.setComponent(componentName);
            return true;
        } else {
            Log.i(TAG, "real start up intent has not component,please set");
        }
        return false;
    }
}
