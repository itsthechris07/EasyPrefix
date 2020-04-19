package com.christian34.easyprefix.utils;

import java.lang.reflect.Field;

public class Reflection {

    public static Class getClass(String packageName, String className) {
        try {
            return Class.forName(packageName + "." + className);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Field getField(Class clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch(NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

}