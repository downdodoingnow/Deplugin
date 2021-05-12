package com.example.deplugin;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.deplugin.hookHelper.AMSHookHelper;
import com.example.deplugin.puppet.receiver.StubReceiver;
import com.example.deplugin.utils.DePluginSP;
import com.example.deplugin.utils.PluginReceiverParseUtils;
import com.example.deplugin.utils.Utils;

import java.io.File;
import java.lang.reflect.Method;

public class MainActivity extends BaseActivity {
    private static final String TAG = Constants.TAG + "MainActivity";
    private static String path = "";

    private ServiceConnection cnn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
        }
    };

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
            path = DePluginSP.getInstance(DePluginApplication.getContext()).getString(Constants.COPY_FILE_PATH, "");
        } catch (Exception e) {
            Log.e(TAG, "init: " + e.getMessage());
        }
    }

    public void click(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.plugin:
                startActivity();
                break;
            case R.id.hook:
                AMSHookHelper.hookAMN();
                AMSHookHelper.hookH();
                AMSHookHelper.hookPkgManager();
                break;
            case R.id.test:
                try {
//                    Intent intent = new Intent();
//                    ComponentName componentName = new ComponentName(this, StubReceiver.class.getName());
//                    intent.setComponent(componentName);
//                    intent.setAction("android.intent.action.MyReceiver");
//                    sendBroadcast(intent);
                    Class<?> cls = Class.forName("com.example.mydemo.Utils");
                    Method method = cls.getDeclaredMethod("registerReceiver", Context.class);
                    method.setAccessible(true);
                    method.invoke(cls, this);

                    Intent intent = new Intent();
                    intent.setAction("com.andorid.test.ACTION");
                    sendBroadcast(intent);
                } catch (Exception e) {
                    Log.e(TAG, "click: " + e);
                }
                break;
            case R.id.service:
                serviceOperate("startService");
                break;
            case R.id.stop_service:
                serviceOperate("stopService");
                break;
            case R.id.bind_service:
                serviceOperate("bindService");
                break;
            case R.id.unbind_service:
                unbindService(cnn);
                break;
            case R.id.receiver:
                PluginReceiverParseUtils.parsePackage(path);
                break;
        }

    }

    private void serviceOperate(String name) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(getPackageName(), "com.example.mydemo.MyService");
        intent.setComponent(componentName);
        if (TextUtils.equals(name, "startService")) {
            startService(intent);
        } else if (TextUtils.equals(name, "stopService")) {
            stopService(intent);
        } else if (TextUtils.equals(name, "bindService")) {
            bindService(intent, cnn, Context.BIND_AUTO_CREATE);
        }
    }

    private void startActivity() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(getPackageName(), "com.example.mydemo.Main2Activity");
        intent.setComponent(componentName);

        startActivity(intent);
    }
}
