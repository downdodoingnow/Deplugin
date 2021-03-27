package com.example.deplugin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.deplugin.Constants;

public class DePluginSP {
    private static final String TAG = Constants.TAG + ".DePluginSP";
    private static final String SP_NAME = "DePlugin_SP";
    private static DePluginSP mDePluginSP;

    private SharedPreferences sp;
    private boolean mIsSetValueSync;

    public static DePluginSP getInstance(Context context) {
        if (null == mDePluginSP) {
            synchronized (DePluginSP.class) {
                if (null == mDePluginSP) {
                    mDePluginSP = new DePluginSP(context);
                }
            }
        }
        return mDePluginSP;
    }

    private DePluginSP(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public void setInt(String key, int value) {
        try {
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(key, value);
            save(editor);
        } catch (Exception e) {
            Log.i(TAG, "set int value failed " + e.getMessage());
        }
    }

    public void setString(String key, String value) {
        try {
            SharedPreferences.Editor editor = getEditor();
            editor.putString(key, value);
            save(editor);
        } catch (Exception e) {
            Log.i(TAG, "set string value failed " + e.getMessage());
        }
    }

    public void setBoolean(String key, boolean value) {
        try {
            SharedPreferences.Editor editor = getEditor();
            editor.putBoolean(key, value);
            save(editor);
        } catch (Exception e) {
            Log.i(TAG, "set string value failed " + e.getMessage());
        }
    }

    private SharedPreferences.Editor getEditor() {
        return sp.edit();
    }

    private void save(SharedPreferences.Editor editor) {
        if (mIsSetValueSync) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public void setSync(boolean sync) {
        mIsSetValueSync = sync;
    }
}
