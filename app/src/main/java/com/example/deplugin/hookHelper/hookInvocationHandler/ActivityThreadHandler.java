package com.example.deplugin.hookHelper.hookInvocationHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ServiceInfo;
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
import com.example.deplugin.utils.HostToPluginMapping;
import com.example.deplugin.utils.RefInvoke;
import com.example.deplugin.utils.Utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class ActivityThreadHandler implements Handler.Callback {
    private static final String TAG = Constants.TAG + "ATHandler";
    private static final int EXECUTE_TRANSACTION = 159;
    private static final int CREATE_SERVICE = 114;
    private Map<String, String> mPathToPluginNameMap = new HashMap<>();

    private Handler mBase;

    public ActivityThreadHandler(Handler handler) {
        mBase = handler;
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        int what = message.what;
        Object object = message.obj;
        switch (what) {
            case EXECUTE_TRANSACTION:
                try {
                    List<Object> mActivityCallbacks = RefInvoke.on(object, "getCallbacks").invoke();
                    Class<?> mLaunchActivityItemCls = RefInvoke.getClass("android.app.servertransaction.LaunchActivityItem");
                    for (Object obj : mActivityCallbacks) {
                        if (mLaunchActivityItemCls.isInstance(obj)) {
                            Intent intent = getIntent(mLaunchActivityItemCls, obj);
                            if (null == intent) {
                                break;
                            }
                            if (!replace(intent)) {
                                break;
                            }
                            String path = DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, "");
                            if (TextUtils.isEmpty(path)) {
                                Log.i(TAG, "dex path is empty,so do need replace class loader");
                                break;
                            }
                            replaceClassloader(path);
                            replacePkgName(mLaunchActivityItemCls, obj, mPathToPluginNameMap.get(path));
                            replace(intent);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getActivityToken failed " + e.getMessage());
                }
                break;
            case CREATE_SERVICE:
                handleCreateService(object);
                break;
            default:

        }
        mBase.handleMessage(message);
        return true;
    }

    /*----------------Activity hook----------------*/
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

    private void replaceClassloader(String path) throws Exception {
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

        String cachePluginName = mPathToPluginNameMap.get(path);
        if (!TextUtils.isEmpty(cachePluginName) && mPackages.get(cachePluginName) != null) {
            Log.i(TAG, path + " plugin name is " + cachePluginName + " has replaced");
            return;
        }

        ApplicationInfo applicationInfo = Utils.generateApplicationInfo(DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, ""));

        if (null != applicationInfo) {
            Object defaultCompatibilityInfo = RefInvoke.getStaticFieldValue(RefInvoke.getField("android.content.res.CompatibilityInfo", "DEFAULT_COMPATIBILITY_INFO"), RefInvoke.getClass("android.content.res.CompatibilityInfo"));
            Object loadedApk = RefInvoke.on(sCurrentActivityThread, "getPackageInfo", ApplicationInfo.class, RefInvoke.getClass("android.content.res.CompatibilityInfo"), int.class).invoke(applicationInfo, defaultCompatibilityInfo, Context.CONTEXT_INCLUDE_CODE);

            String pluginName = applicationInfo.packageName;

            if (!TextUtils.isEmpty(pluginName)) {
                Log.i(TAG, "plugin pkg name is " + pluginName);
                setClassloader(loadedApk, path);
                mPackages.put(pluginName, new WeakReference<>(loadedApk));
                mPathToPluginNameMap.put(path, pluginName);
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
        ComponentName componentName = intent.getComponent();
        if (null == componentName) {
            Log.i(TAG, "replace not allowed component is null");
            return false;
        }
        String hostActivityName = componentName.getClassName();
        if (TextUtils.isEmpty(hostActivityName)) {
            Log.i(TAG, "not found host activity,so no need replace");
            return false;
        }
        String pluginActivityName = HostToPluginMapping.getPluginActivity(hostActivityName);
        Log.i(TAG, "host activity name is " + hostActivityName + ",plugin activity name is " + pluginActivityName);
        componentName = new ComponentName(componentName.getPackageName(), pluginActivityName);
        intent.setComponent(componentName);
        return true;
    }

    /*----------------Service hook----------------*/
    public void handleCreateService(Object object) {
        try {
            ServiceInfo serviceInfo = (ServiceInfo) RefInvoke.getFieldValue(RefInvoke.getField(object.getClass(), "info"), object);
            String hostServiceName = serviceInfo.name;
            String pluginServiceName = HostToPluginMapping.getPluginService(hostServiceName);
            if (TextUtils.isEmpty(pluginServiceName)) {
                Log.i(TAG, "not found host service,so no need replace");
                return;
            }
            String path = DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, "");
            replaceClassloader(path);

            serviceInfo.name = pluginServiceName;
            serviceInfo.applicationInfo.packageName = mPathToPluginNameMap.get(path);
            Log.i(TAG, "replaced to plugin service success");
        } catch (Exception e) {
            Log.i(TAG, "handle create service failed");
        }
    }
}
