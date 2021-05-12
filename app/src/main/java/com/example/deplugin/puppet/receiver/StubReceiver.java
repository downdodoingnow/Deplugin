package com.example.deplugin.puppet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.deplugin.Constants;
import com.example.deplugin.utils.PluginReceiverParseUtils;

import java.util.List;

public class StubReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.TAG + "stub.receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "action is " + action);
        List<Object> receiverObjs = PluginReceiverParseUtils.getReceiverObjByAction(action);
        if (null != receiverObjs) {
            for(Object obj : receiverObjs){
                ((BroadcastReceiver) obj).onReceive(context, intent);
            }
            Log.i(TAG, "translate receiver succ");
        } else {
            Log.i(TAG, "not exist plugin receiver");
        }
    }
}
