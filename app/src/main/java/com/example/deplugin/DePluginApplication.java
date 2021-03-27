package com.example.deplugin;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.deplugin.utils.Utils;

public class DePluginApplication extends Application {
    private static Context mContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        Log.i(Constants.TAG, "onCreate application");
        Utils.copyApk(this, Constants.PLUGIN_NAME_ONE);
    }

    public static Context getContext() {
        return mContext;
    }
}
