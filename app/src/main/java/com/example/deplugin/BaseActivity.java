package com.example.deplugin;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.example.deplugin.utils.RefInvoke;

public class BaseActivity extends Activity {
    private static final String TAG = Constants.TAG + "BaseActivity";

    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;

    public void loadResource(String dexPath) {
        try {
            //mAssetManager = (AssetManager) RefInvoke.createObject(AssetManager.class, new Class[]{}, new Object[]{});
            mAssetManager = getAssets();
            int result = RefInvoke.on(mAssetManager, "addAssetPath", String.class).invoke(dexPath);
            Log.i(TAG, "add asset path result is " + result);

            Resources superRes = super.getResources();
            mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());

            mTheme = mResources.newTheme();
            mTheme.setTo(super.getTheme());
        } catch (Exception e) {
            Log.e(TAG, "load Resource failed " + e);
        }
    }

    @Override
    public AssetManager getAssets() {
        return null == mAssetManager ? super.getAssets() : mAssetManager;
    }

    @Override
    public Resources getResources() {
        return null == mResources ? super.getResources() : mResources;
    }

    @Override
    public Resources.Theme getTheme() {
        return null == mTheme ? super.getTheme() : mTheme;
    }
}
