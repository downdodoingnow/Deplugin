package com.example.deplugin.classLoader;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class DeHostDexClassloader {
    private static DeHostDexClassloader mInstance;
    private Map<String, DexClassLoader> mClassloaders = new HashMap<>();

    private DeHostDexClassloader() {

    }

    public static DeHostDexClassloader getInstance() {
        if (null == mInstance) {
            synchronized (DeHostDexClassloader.class) {
                if (null == mInstance) {
                    mInstance = new DeHostDexClassloader();
                }
            }
        }
        return mInstance;
    }

    public Class<?> loadClass(DexClassLoader classLoader, String clsName) throws Exception {
        return classLoader.loadClass(clsName);
    }

    public DexClassLoader getDexClassLoader(Context context, String dexPath) {
        DexClassLoader dexClassLoader = mClassloaders.get(dexPath);
        if (null == dexClassLoader) {
            dexClassLoader = new DexClassLoader(dexPath, null, null, context.getClassLoader());
            mClassloaders.put(dexPath, dexClassLoader);
        }
        return dexClassLoader;
    }
}
