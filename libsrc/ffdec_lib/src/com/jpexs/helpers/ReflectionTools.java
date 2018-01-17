/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.helpers;

import com.jpexs.decompiler.flash.types.FieldChangeObserver;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class ReflectionTools {

    private static final Logger logger = Logger.getLogger(ReflectionTools.class.getName());

    public static <E> Map<E, String> getConstNamesMap(Class<?> classWithConsts, Class<E> constsType, String matchRegexp) {
        Map<E, String> ret = new HashMap<>();
        Field[] fs = classWithConsts.getDeclaredFields();
        Pattern p = Pattern.compile(matchRegexp);
        for (Field f : fs) {
            Matcher m = p.matcher(f.getName());
            if (m.matches()) {
                try {
                    String name = m.groupCount() > 0 ? m.group(1) : m.group(0);
                    @SuppressWarnings("unchecked")
                    E val = (E) f.get(constsType);

                    StringBuilder identName = new StringBuilder();
                    boolean cap = false;
                    for (int i = 0; i < name.length(); i++) {
                        char c = name.charAt(i);
                        if (c == '_') {
                            cap = true;
                            continue;
                        }
                        if (cap) {
                            identName.append(c);
                            cap = false;
                        } else {
                            identName.append(Character.toLowerCase(c));
                        }
                    }
                    ret.put(val, identName.toString());
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    //ignore
                }
            }
        }
        return ret;
    }

    public static Object getValue(Object obj, Field field) throws IllegalArgumentException, IllegalAccessException {
        Object value = field.get(obj);
        return value;
    }

    public static Object getValue(Object obj, Field field, int index) throws IllegalArgumentException, IllegalAccessException {
        if (index == -1) {
            return getValue(obj, field);
        }

        if (getFieldSubSize(obj, field) <= index) {
            return null;
        }
        Object value = field.get(obj);
        if (List.class.isAssignableFrom(field.getType())) {
            return ((List) value).get(index);
        }

        if (field.getType().isArray()) {
            return Array.get(value, index);
        }

        return value;
    }

    public static boolean needsIndex(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            return true;
        } else if (field.getType().isArray()) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static int getFieldSubSize(Object obj, Field field) {
        Object val;
        try {
            val = field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return 0;
        }
        if (List.class.isAssignableFrom(field.getType())) {
            return ((List) val).size();
        } else if (field.getType().isArray()) {
            return Array.getLength(val);
        }
        return 0;
    }

    private static void notifyFieldChanged(Object obj, Field field) {
        if (obj != null && obj instanceof FieldChangeObserver) {
            ((FieldChangeObserver) obj).fieldChanged(field);
        }
    }

    public static void setValue(Object obj, Field field, Object newValue) throws IllegalArgumentException, IllegalAccessException {
        field.set(obj, newValue);
        notifyFieldChanged(obj, field);
    }

    @SuppressWarnings("unchecked")
    public static void setValue(Object obj, Field field, int index, Object newValue) throws IllegalArgumentException, IllegalAccessException {
        if (index == -1) {
            setValue(obj, field, newValue);
            return;
        }

        Object value = field.get(obj);
        if (needsIndex(field) && index >= getFieldSubSize(obj, field)) { //outofbounds, ignore
            return;
        }
        if (List.class.isAssignableFrom(field.getType())) {
            ((List) value).set(index, newValue);
        } else if (field.getType().isArray()) {
            Array.set(value, index, newValue);
        } else {
            field.set(obj, newValue);
            notifyFieldChanged(obj, field);
        }
    }

    public static boolean canInstantiate(Class cls) {
        if (cls.isInterface()) {
            return false;
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }
        return true;
    }

    public static boolean canInstantiateDefaultConstructor(Class<?> cls) {
        if (!canInstantiate(cls)) {
            return false;
        }

        try {
            cls.getConstructor();
        } catch (NoSuchMethodException | SecurityException ex) {
            return false;
        }

        return true;
    }

    public static boolean canAddToField(Object object, Field field) {
        if (List.class.isAssignableFrom(field.getType())) {

            ParameterizedType listType = (ParameterizedType) field.getGenericType();
            Class<?> parameterClass = (Class<?>) listType.getActualTypeArguments()[0];
            return canInstantiate(parameterClass);
        }

        if (field.getType().isArray()) {
            Object arrValue;
            try {
                arrValue = field.get(object);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
                return false;
            }
            Class componentClass = arrValue.getClass().getComponentType();
            if (componentClass.isPrimitive()) {
                return true;
            }
            return canInstantiate(componentClass);
        }
        return false;

    }

    public static Object newInstanceOf(Class cls) throws InstantiationException, IllegalAccessException {
        if (cls == Integer.class || cls == int.class) {
            return 0;
        } else if (cls == Float.class || cls == float.class) {
            return 0.0f;
        } else if (cls == Double.class || cls == double.class) {
            return (double) 0;
        } else if (cls == Long.class || cls == long.class) {
            return 0L;
        }
        if (cls.isInterface()) {
            return null;
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            return null;
        }
        return cls.newInstance();
    }

    @SuppressWarnings("unchecked")
    public static boolean addToList(Object object, Field field, int index, Class<?> cls) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return false;
        }
        List list;
        try {
            list = (List) field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> parameterClass = (Class<?>) listType.getActualTypeArguments()[0];
        try {
            Object val = newInstanceOf(cls == null ? parameterClass : cls);
            if (val == null) {
                return false;
            }
            list.add(index, val);
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static Class<?> getFieldSubType(Object object, Field field) {
        if (field.getType().isArray()) {
            Object arrValue;
            try {
                arrValue = field.get(object);
                return arrValue.getClass().getComponentType();
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                return null;
            }
        }
        if (List.class.isAssignableFrom(field.getType())) {
            ParameterizedType listType = (ParameterizedType) field.getGenericType();
            return (Class<?>) listType.getActualTypeArguments()[0];
        }
        return null;
    }

    public static boolean addToArray(Object object, Field field, int index, boolean notnull, Class<?> cls) {
        if (!field.getType().isArray()) {
            return false;
        }
        Object arrValue;
        try {
            arrValue = field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        Class componentClass = arrValue.getClass().getComponentType();
        Object val = null;
        if (!componentClass.isPrimitive()) {
            try {
                val = newInstanceOf(cls == null ? componentClass : cls);
            } catch (InstantiationException | IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
                return false;
            }
            if (val == null) {
                return false;
            }
        }

        int originalSize = Array.getLength(arrValue);
        Object copy = Array.newInstance(componentClass, originalSize + 1);

        //Copy items before
        for (int i = 0; i < index; i++) {
            Array.set(copy, i, Array.get(arrValue, i));
        }
        if (val != null) {
            Array.set(copy, index, val);
        }
        //Copy items after
        for (int i = index; i < originalSize; i++) {
            Array.set(copy, i + 1, Array.get(arrValue, i));
        }
        try {
            field.set(object, copy);
            notifyFieldChanged(object, field);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static boolean addToField(Object object, Field field, int index, boolean notnull, Class<?> cls) {
        if (List.class.isAssignableFrom(field.getType())) {
            return addToList(object, field, index, cls);
        }

        if (field.getType().isArray()) {
            return addToArray(object, field, index, notnull, cls);
        }
        return false;
    }

    public static boolean removeFromField(Object object, Field field, int index) {
        if (List.class.isAssignableFrom(field.getType())) {
            return removeFromList(object, field, index);
        }

        if (field.getType().isArray()) {
            return removeFromArray(object, field, index);
        }
        return false;
    }

    public static boolean removeFromList(Object object, Field field, int index) {
        List list;
        try {
            list = (List) field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        if (index < 0 || index >= list.size()) {
            return false;
        }
        list.remove(index);
        return true;
    }

    public static boolean removeFromArray(Object object, Field field, int index) {
        Object arrValue;
        try {
            arrValue = field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        Class componentClass = arrValue.getClass().getComponentType();
        int originalSize = Array.getLength(arrValue);
        Object copy = Array.newInstance(componentClass, originalSize - 1);
        int pos = 0;
        //copy all before index
        for (int i = 0; i < index; i++) {
            Array.set(copy, pos, Array.get(arrValue, i));
            pos++;
        }
        //copy all after index
        for (int i = index + 1; i < originalSize; i++) {
            Array.set(copy, pos, Array.get(arrValue, i));
            pos++;
        }
        try {
            field.set(object, copy);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static List<Field> getSwfFields(Class cls) {
        List<Field> result = new ArrayList<>();
        Field[] fields = cls.getFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            Internal inter = f.getAnnotation(Internal.class);
            if (inter != null) {
                continue;
            }

            result.add(f);
        }

        fields = cls.getDeclaredFields();
        // Add private fields marked with SWFField annotation
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            if (!Modifier.isPrivate(f.getModifiers())) {
                continue;
            }

            SWFField swfField = f.getAnnotation(SWFField.class);
            if (swfField != null) {
                result.add(f);
            }
        }

        return result;
    }
}
