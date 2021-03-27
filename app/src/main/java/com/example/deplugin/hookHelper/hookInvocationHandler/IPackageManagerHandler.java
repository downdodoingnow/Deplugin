package com.example.deplugin.hookHelper.hookInvocationHandler;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import com.example.deplugin.Constants;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class IPackageManagerHandler implements InvocationHandler {
    private static final String TAG = Constants.TAG + "pkgMHandler";
    private Object mBase;

    public IPackageManagerHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (TextUtils.equals("getPackageInfo", method.getName())) {
            Log.i(TAG, "current method is getPackageInfo");
            return new PackageInfo();
        }
        return method.invoke(mBase, objects);
    }
}
