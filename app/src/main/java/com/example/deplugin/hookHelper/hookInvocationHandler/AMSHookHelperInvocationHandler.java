package com.example.deplugin.hookHelper.hookInvocationHandler;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.deplugin.Constants;
import com.example.deplugin.utils.HostToPluginMapping;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AMSHookHelperInvocationHandler implements InvocationHandler {
    private static final String TAG = Constants.TAG + "AMSHookHandler";

    private Object mBase;

    public AMSHookHelperInvocationHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        int index = -1;
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof Intent) {
                index = i;
                break;
            }
        }
        if (-1 == index) {
            Log.i(TAG, "not found intent in params");
            return method.invoke(mBase, objects);
        }
        String name = method.getName();
        if (TextUtils.equals(name, "startActivity")) {
            Log.i(TAG, "current is hooking startActivity");
            Intent realIntent = (Intent) objects[index];
            hookStarActivity(realIntent, objects, index);

        } else if (TextUtils.equals(name, "startService")
                || TextUtils.equals(name, "stopService")
                || TextUtils.equals(name, "bindService")) {
            Intent realIntent = (Intent) objects[index];
            hookServiceOperate(realIntent, name);
        }
        return method.invoke(mBase, objects);
    }

    private void hookServiceOperate(Intent realIntent, String name) {
        if (null != realIntent) {
            ComponentName pluginComponentName = realIntent.getComponent();
            if (null != pluginComponentName) {
                String pluginServiceName = pluginComponentName.getClassName();
                String hostServiceName = HostToPluginMapping.getHostService(pluginServiceName);
                if (!TextUtils.isEmpty(hostServiceName)) {
                    Log.i(TAG, "current is hooking " + name);
                    ComponentName componentName = new ComponentName(pluginComponentName.getPackageName(), hostServiceName);
                    realIntent.setComponent(componentName);
                }
            }
        }
    }

    private void hookStarActivity(Intent realIntent, Object[] objects, int index) {
        if (null != realIntent) {
            ComponentName pluginComponentName = realIntent.getComponent();
            if (null != pluginComponentName) {
                String pluginActivityName = pluginComponentName.getClassName();
                String hostActivityName = HostToPluginMapping.getHostActivity(pluginActivityName);
                if (!TextUtils.isEmpty(hostActivityName)) {
                    Log.i(TAG, "current is hooking startActivity");
                    ComponentName componentName = new ComponentName(pluginComponentName.getPackageName(), hostActivityName);
                    realIntent.setComponent(componentName);
                }
            }
        }
    }
}
