package com.example.deplugin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.deplugin.Constants;
import com.example.deplugin.DePluginApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

public class Utils {
    private static final String TAG = Constants.TAG + "utils";

    public static boolean extractAssets(Context context, String filePath, String pluginName) {
        AssetManager assetManager = context.getAssets();

        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {

            Log.i(TAG, "save apk file path is " + filePath);

            fileOutputStream = new FileOutputStream(filePath);
            inputStream = assetManager.open(pluginName);

            byte[] bytes = new byte[1024];
            int length = -1;
            while ((length = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, length);
            }
            fileOutputStream.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "copy file failed " + e.getMessage());
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
                if (null != fileOutputStream) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                Log.i(TAG, "extractAssets: " + e.getMessage());
            }
        }
        return false;
    }

    public static void requestPermissions(Activity activity, String[] permissions) {
        try {
            PackageManager pm = activity.getPackageManager();
            String pkgName = activity.getPackageName();
            String[] needRequst = new String[permissions.length];

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (PackageManager.PERMISSION_DENIED == pm.checkPermission(permission, pkgName)) {
                    needRequst[i] = permission;
                }
            }

            ActivityCompat.requestPermissions(activity, needRequst, 1);
        } catch (Exception e) {
            Log.i(TAG, "requestPermissions: " + e.getMessage());
        }
    }

    public static void copyApk(Context context, String pluginName) {
        DePluginSP sp = DePluginSP.getInstance(context);
        String filePath = sp.getString(Constants.COPY_FILE_PATH, "");
        if (TextUtils.isEmpty(filePath)) {
            File saveApkFile = context.getFileStreamPath(pluginName);
            if (null == saveApkFile) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    saveApkFile = context.getDataDir();
                    filePath = saveApkFile.getAbsolutePath() + pluginName;
                } else {
                    filePath = "data/user/0/" + context.getPackageName() + "/" + pluginName;
                }
            } else {
                filePath = saveApkFile.getAbsolutePath();
            }
            boolean result = extractAssets(context, filePath, pluginName);
            if (result) {
                sp.setString(Constants.COPY_FILE_PATH, filePath);
            }
            Log.i(TAG, "copy " + result);
        } else {
            File file = new File(filePath);
            if (file.exists()) {
                Log.i(TAG, "had copy apk before,so no need copy again");
            } else {
                Log.i(TAG, "althogh save apk file path success,but file not exists");
                extractAssets(context, filePath, pluginName);
            }
        }
    }

    public static ApplicationInfo generateApplicationInfo(String pluginPath) {
        try {
            Class<?> packageParserCls = RefInvoke.getClass("android.content.pm.PackageParser");
            Object packageParserObj = RefInvoke.createObject(packageParserCls, new Class[]{}, new Class[]{});

            File file = new File(pluginPath);
            Log.i(TAG, "plugin path is " + pluginPath);
            if (!file.exists()) {
                Log.i(TAG, "file non-exist");
                return null;
            }
            Object packageObj = RefInvoke.on(packageParserObj, "parseMonolithicPackage", File.class, int.class).invoke(file, 0);
            if (null == packageObj) {
                Log.i(TAG, "get PackageParse$Package obj failed");
                return null;
            }

            Class<?> packageUserStateCls = RefInvoke.getClass("android.content.pm.PackageUserState");
            ApplicationInfo applicationInfo = RefInvoke.on(packageParserObj, "generateApplicationInfo", packageObj.getClass(), int
                    .class, packageUserStateCls).invoke(packageObj, 0, RefInvoke.createObject(packageUserStateCls, new Class[]{}, new Class[]{}));
            Log.i(TAG, pluginPath + " package name is " + applicationInfo.packageName);

            int uid = getUid();
            if (-1 == uid) {
                Log.i(TAG, "get uid failed");
                return null;
            }
            applicationInfo.sourceDir = pluginPath;
            applicationInfo.publicSourceDir = pluginPath;
            applicationInfo.uid = uid;
            return applicationInfo;
        } catch (Exception e) {
            Log.i(TAG, "generateApplicationzInfo failed " + e.getMessage());
        }
        return null;
    }

    public static int getUid() {
        try {
            Context context = DePluginApplication.getContext();
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return applicationInfo.uid;
        } catch (Exception e) {
            Log.e(TAG, "getUid failed " + e);
        }
        return -1;
    }

    public static void mergeDex(ClassLoader classLoader, File apkFile, File optDexfile) {
        try {
            Object pathListObj = RefInvoke.getFieldValue(RefInvoke.getField(BaseDexClassLoader.class, "pathList"), classLoader);
            if (null == pathListObj) {
                Log.i(TAG, "get path list failed");
                return;
            }
            Field dexElementsField = RefInvoke.getField(pathListObj.getClass(), "dexElements");
            Object[] elements = (Object[]) RefInvoke.getFieldValue(dexElementsField, pathListObj);
            if (null == elements) {
                Log.i(TAG, "get elements failed");
                return;
            }
            int length = elements.length;
            Class<?> elementCls = elements.getClass().getComponentType();
            Object[] newElemets = (Object[]) Array.newInstance(elementCls, length + 1);

            Object elementObj = RefInvoke.createObject(elementCls, new Class[]{DexFile.class, File.class}, new Object[]{DexFile.loadDex(apkFile.getCanonicalPath(), optDexfile.getAbsolutePath(), 0), apkFile});
            newElemets[0] = elementObj;
            System.arraycopy(elements, 0, newElemets, 1, length);
            RefInvoke.setFieldValue(dexElementsField, pathListObj, newElemets);
            Log.i(TAG, "merge dex success");
        } catch (Exception e) {
            Log.e(TAG, "mergeDex failed " + e);
        }
    }
}
