package com.example.deplugin.utils;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.deplugin.Constants;
import com.example.deplugin.DePluginApplication;
import com.example.deplugin.classLoader.DeHostDexClassloader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class PluginReceiverParseUtils {
    private static final String TAG = Constants.TAG + "PR";
    private static final Map<String, List<Object>> mActionToReceiverMap = new HashMap<>();

    public static void parsePackage(String apkPath) {
        try {
            File file = new File(apkPath);
            if (!file.exists()) {
                Log.i(TAG, "parse plugin receiver apk not exist");
                return;
            }
            Log.i(TAG, "parse package path is " + apkPath);
            Class<?> cls = Class.forName("android.content.pm.PackageParser");
            Object packageParserObj = RefInvoke.createObject(cls, null, null);
            if (null == packageParserObj) {
                Log.i(TAG, "parse package create packageParser object failed");
                return;
            }

            Object packageObj = RefInvoke.on(packageParserObj, "parsePackage", new Class[]{File.class, int.class})
                    .invoke(file, PackageManager.GET_RECEIVERS);
            if (null == packageObj) {
                Log.i(TAG, "parse package get packageObj failed");
                return;
            }

            List<Object> receivers = (List<Object>) RefInvoke.getFieldValue(RefInvoke.getField(packageObj.getClass(), "receivers"), packageObj);
            if (null == receivers) {
                Log.i(TAG, "parse package get receivers failed");
                return;
            }
            for (Object receiver : receivers) {
                parseAction(receiver, apkPath);
            }
        } catch (Exception e) {
            Log.i(TAG, "parse package failed " + e);
        }
    }

    private static void parseAction(Object receiver, String path) {
        try {
            Class<?> cls = RefInvoke.getClass("android.content.pm.PackageParser$Component");
            ArrayList<IntentFilter> intents = (ArrayList<IntentFilter>) RefInvoke.getFieldValue(RefInvoke.getField(cls, "intents"), receiver);
            if (null == intents || 0 == intents.size()) {
                return;
            }
            String clsName = (String) RefInvoke.getFieldValue(RefInvoke.getField(cls, "className"), receiver);
            Log.i(TAG, "parseAction current receiver name is " + clsName);
            Object receiverObj = creatReceiverObj(clsName, path);
            if (null == receiverObj) {
                Log.i(TAG, "parseAction create receiver obj failed");
                return;
            }

            for (IntentFilter intentFilter : intents) {
                Class<?> intentFilterCls = RefInvoke.getClass("android.content.IntentFilter");
                Log.i(TAG, "field: " + RefInvoke.getField(intentFilterCls, "mActions"));
                List<String> actions = (List<String>) RefInvoke.getFieldValue(RefInvoke.getField(intentFilterCls, "mActions"), intentFilter);

                for (String action : actions) {
                    registerActionToReceiver(action, receiverObj);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseAction failed " + e);
        }
    }

    private static Object creatReceiverObj(String clsName, String path) {
        try {
            DexClassLoader dexClassLoader = DeHostDexClassloader.getInstance().getDexClassLoader(DePluginApplication.getContext(), path);
            Class<?> cls = dexClassLoader.loadClass(clsName);
            return RefInvoke.createObject(cls, null, null);
        } catch (Exception e) {
            Log.i(TAG, "createReceiverObj failed " + e.getCause());
        }
        return null;
    }


    private static void registerActionToReceiver(String action, Object receiverObj) {
        List<Object> receiverObjs = mActionToReceiverMap.get(action);
        if(null == receiverObjs){
            receiverObjs = new ArrayList<>();
            mActionToReceiverMap.put(action, receiverObjs);
        }
        receiverObjs.add(receiverObj);
    }

    public static List<Object> getReceiverObjByAction(String action) {
        if (0 == mActionToReceiverMap.size()) {
            Log.i(TAG, "not exist any action to receiver obj");
            return null;
        }
        return mActionToReceiverMap.get(action);
    }

}
