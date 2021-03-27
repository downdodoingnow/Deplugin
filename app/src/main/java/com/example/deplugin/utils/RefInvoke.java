package com.example.deplugin.utils;

import android.util.Log;

import com.example.deplugin.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class RefInvoke {
    private static final String TAG = Constants.TAG + "Ref";

    private static ConcurrentHashMap<String, Class> mCachedClasses = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Method> mCachedMethods = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Field> mCachedFields = new ConcurrentHashMap<>();

    public static Reflect on(Object object, String methodName, Class<?>... paramTypes) throws Exception {
        return new Reflect(object, getMethod(object.getClass(), methodName, paramTypes));
    }

    public static Reflect on(Class<?> cls, String methodName, Class<?>... paramTypes) throws Exception {
        return new Reflect(null, getMethod(cls, methodName, paramTypes));
    }

    public static Object createObject(String className, Class[] paramsTypes, Object[] paramsValues) throws Exception {
        Class<?> cls = getClass(className);
        return createObject(cls, paramsTypes, paramsValues);
    }

    public static Object createObject(Class<?> cls, Class[] paramsTypes, Object[] paramsValues) throws Exception {
        if (null == cls) {
            Log.i(TAG, "not allowed cls is null");
            return null;
        }
        Constructor<?> constructor = cls.getConstructor(paramsTypes);
        if (null == constructor) {
            Log.i(TAG, "undefined constructor");
            return null;
        }
        constructor.setAccessible(true);
        return constructor.newInstance(paramsValues);
    }

    public static Method getMethod(String clsName, String methodName, Class[] paramsType) throws Exception {
        return getMethod(getClass(clsName), methodName, paramsType);
    }

    public static Method getMethod(Class<?> cls, String methodName, Class[] paramsType) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(cls.getName()).append(" ").append(methodName).append("(");
        for (int i = 0; i < paramsType.length; i++) {
            builder.append(paramsType[i].getName()).append(",");
        }
        builder.append(")");
        String key = builder.toString();

        Method method = mCachedMethods.get(key);
        if (null == method) {
            method = findMethod(cls, methodName, paramsType);
            if (null != method) {
                method.setAccessible(true);
                Log.i(TAG, "cached method key is " + key);
                mCachedMethods.put(key, method);
            } else {
                Log.i(TAG, "not find " + methodName);
            }
        }
        return method;
    }

    private static Method findMethod(Class<?> cls, String methodName, Class[] paramsType) {
        Method method = null;
        try {
            method = cls.getDeclaredMethod(methodName, paramsType);
        } catch (Exception e) {
            Log.i(TAG, "get Declared Method failed,so use get method");
            try {
                method = cls.getMethod(methodName, paramsType);
            } catch (Exception e1) {
                Log.i(TAG, "get Method failed " + e1);
            }
        }
        return method;
    }

    public static Field getField(String clsName, String fieldName) throws Exception {
        return getField(getClass(clsName), fieldName);
    }

    public static Field getField(Class<?> cls, String fieldName) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(cls.getName()).append(" ").append(fieldName);

        String key = builder.toString();

        Field field = mCachedFields.get(key);
        if (null == field) {
            field = findField(cls, fieldName);
            if (null != field) {
                field.setAccessible(true);
                Log.i(TAG, "cached field key is " + key);
                mCachedFields.put(key, field);
            } else {
                Log.i(TAG, "failed get field " + fieldName);
            }
        }
        return field;
    }

    private static Field findField(Class<?> cls, String fieldName) {
        Field field = null;
        try {
            field = cls.getDeclaredField(fieldName);
        } catch (Exception e) {
            Log.i(TAG, "get Declared Field failed,so use get field");
            try {
                field = cls.getField(fieldName);
            } catch (Exception e1) {
                Log.i(TAG, "get field failed " + e1);
            }
        }
        return field;
    }

    public static Class getClass(String className) throws Exception {
        Class<?> cls = mCachedClasses.get(className);
        if (null == cls) {
            cls = Class.forName(className);
            mCachedClasses.put(className, cls);
        }
        return cls;
    }

    public static void setStaticFieldValue(Field field, Class<?> cls, Object value) throws Exception {
        field.set(cls, value);
    }

    public static Object getStaticFieldValue(Field field, Class<?> cls) throws Exception {
        return field.get(cls);
    }

    public static void setFieldValue(Field field, Object obj, Object value) throws Exception {
        field.set(obj, value);
    }

    public static Object getFieldValue(Field field, Object obj) throws Exception {
        return field.get(obj);
    }

    public static class Reflect {
        private Object mObject;
        private Method mMethod;

        public Reflect(Object object, Method method) {
            this.mObject = object;
            this.mMethod = method;
        }

        public <T> T invoke(Object... args) throws Exception {
            if (null == mMethod) {
                return null;
            }
            if (!mMethod.isAccessible()) {
                mMethod.setAccessible(true);
            }
            if (mMethod.getReturnType() == void.class) {
                mMethod.invoke(mObject, args);
                return null;
            } else {
                return (T) mMethod.invoke(mObject, args);
            }

        }
    }
}
