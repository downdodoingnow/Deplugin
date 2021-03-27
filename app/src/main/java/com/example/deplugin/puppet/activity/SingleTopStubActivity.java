package com.example.deplugin.puppet.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.deplugin.Constants;
import com.example.deplugin.R;

public class SingleTopStubActivity extends Activity {

    private static final String TAG = Constants.TAG + "single";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_top_stub);
        Log.i(TAG, "onCreate: " );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: " );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }
}
