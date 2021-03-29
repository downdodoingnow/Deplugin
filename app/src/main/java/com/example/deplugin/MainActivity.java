package com.example.deplugin;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.deplugin.hookHelper.AMSHookHelper;
import com.example.deplugin.puppet.activity.StandardStubActivity;
import com.example.deplugin.utils.DePluginSP;
import com.example.deplugin.utils.RefInvoke;
import com.example.deplugin.utils.Utils;

import java.io.File;

public class MainActivity extends BaseActivity {
    private static final String TAG = Constants.TAG + "MainActivity";
    private static String fileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadResource(DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, ""));
        setContentView(R.layout.activity_main);
        Utils.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        init();
        Log.i(TAG, "onCreate deplugin MainActivity");

        mergeDex();
    }

    private void mergeDex() {
        try {
            String path = DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, "");
            if (TextUtils.isEmpty(path)) {
                Log.i(TAG, "path is empty");
                return;
            }
            String dir = path.substring(0, path.lastIndexOf("/"));
            Log.i(TAG, "dir is " + dir);
            File apkFile = new File(path);
            File optDexFile = new File(dir + "/out.dex");
            if (!optDexFile.exists()) {
                boolean result = optDexFile.createNewFile();
                Log.i(TAG, "create dex dir result is " + result);
            }
            Utils.mergeDex(getClassLoader(), apkFile, optDexFile);
        } catch (Exception e) {
            Log.e(TAG, "mergeDex: " + e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: " + this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: " + this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart: " + this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: " + this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    private void init() {
        try {
            File file = getFileStreamPath(Constants.PLUGIN_NAME_ONE);
            fileName = file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "init: " + e.getMessage());
        }
    }

    public void click(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.plugin:
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(getPackageName(), "com.example.mydemo.Main2Activity");
                intent.setComponent(componentName);
                intent.putExtra(Constants.REPLACED_START_UP_INTENT, createStartUpIntent());

                startActivity(intent);
                break;
            case R.id.hook:
                AMSHookHelper.hookAMN();
                AMSHookHelper.hookH();
                AMSHookHelper.hookPkgManager();
                break;
            case R.id.test:
                try {
                    Class<?> cls = getClassLoader().loadClass("com.example.deplugin.Test");
                    Log.i(TAG, "click: " + cls);
                    RefInvoke.on(RefInvoke.createObject(cls, new Class[]{}, new Object[]{}), "print").invoke();
                } catch (Exception e) {
                    Log.e(TAG, "click: " + e);
                }
        }

    }

    private Intent createStartUpIntent() {
        Intent startUpIntent = new Intent();
        ComponentName componentName = new ComponentName(DePluginApplication.getContext(), StandardStubActivity.class.getName());
        startUpIntent.setComponent(componentName);
        startUpIntent.putExtra(Constants.DEX_PATH, DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, ""));
        return startUpIntent;
    }
}
