package com.christian34.easyprefix.utils;

import java.lang.reflect.Field;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
@SuppressWarnings("rawtypes")
public class Reflection {

    public static Class getClass(String packageName, String className) {
        try {
            return Class.forName(packageName + "." + className);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getField(Class clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch(NoSuchFieldException e) {
            return null;
        }
    }

}