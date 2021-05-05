package com.example.deplugin.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HostToPluginMapping {
    private static Map<String, String> mServicesMapping = new HashMap<>();
    private static Map<String, String> mActivitiesMapping = new HashMap<>();

    public static void putServiceMapping(String hostService, String pluginService) {
        mServicesMapping.put(hostService, pluginService);
    }

    public static void putActivityMapping(String hostActivity, String pluginActivity) {
        mActivitiesMapping.put(hostActivity, pluginActivity);
    }

    public static String getPluginService(String hostService) {
        return mServicesMapping.get(hostService);
    }

    public static String getHostService(String pluginService) {
        return getKey(mServicesMapping, pluginService);
    }


    public static String getPluginActivity(String hostActivity) {
        return mActivitiesMapping.get(hostActivity);
    }

    public static String getHostActivity(String pluginActivity) {
        return getKey(mActivitiesMapping, pluginActivity);
    }

    private static String getKey(Map<String, String> params, String pluginComponentName) {
        Set<Map.Entry<String, String>> set = params.entrySet();

        for (Map.Entry<String, String> entry : set) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (TextUtils.equals(value, pluginComponentName)) {
                return key;
            }
        }
        return "";
    }
}
