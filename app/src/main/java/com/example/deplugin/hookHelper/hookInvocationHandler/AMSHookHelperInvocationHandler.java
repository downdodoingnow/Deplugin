package com.example.deplugin.hookHelper.hookInvocationHandler;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.deplugin.Constants;
import com.example.deplugin.DePluginApplication;
import com.example.deplugin.puppet.activity.StandardStubActivity;

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
        if (TextUtils.equals(method.getName(), "startActivity")) {
            Log.i(TAG, "replace start up activity");

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

            Intent realIntent = (Intent) objects[index];

            //代替插件Activity的宿主Activity
            Intent replacedStartUpIntent = realIntent.getParcelableExtra(Constants.REPLACED_START_UP_INTENT);
            if (null != replacedStartUpIntent) {
                Log.i(TAG, "origin intent is " + realIntent);
                realIntent.putExtra(Constants.REPLACED_START_UP_INTENT,"");
                replacedStartUpIntent.putExtra(Constants.START_UP_INTENT, realIntent);
                objects[index] = replacedStartUpIntent;
                Log.i(TAG, "replaced start up intent is " + replacedStartUpIntent);
            } else {
                Log.i(TAG, "replaced intent activity is null");
            }
        }
        return method.invoke(mBase, objects);
    }
}
