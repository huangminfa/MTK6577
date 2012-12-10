package com.mediatek.ngin3d.android;

import android.content.Context;

import java.lang.reflect.Field;

class StyleableResolver {

    protected Class<?> mStyleClass;
    protected Object mStyleObject;

    public StyleableResolver(Context context) {
        ClassLoader cl = context.getClassLoader();
        String packageName = context.getPackageName();
        try {
            mStyleClass = Class.forName(packageName + ".R$styleable", true, cl);
            mStyleObject = mStyleClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public int[] resolveIntArray(String attr) {
        int[] intArray = null;
        try {
            Field declaredField = mStyleClass.getDeclaredField(attr);
            intArray = (int[])declaredField.get(mStyleObject);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return intArray;
    }

    public int resolveInt(String attr) {
        int value = 0;
        try {
            Field declaredField = mStyleClass.getDeclaredField(attr);
            value = declaredField.getInt(mStyleObject);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }
}
