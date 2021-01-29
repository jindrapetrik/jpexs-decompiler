/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 * @param <T>
 */
public class ConfigurationItem<T> {

    private final String name;

    protected boolean hasValue;

    private T value;

    private T defaultValue;

    private boolean modified;

    private List<ConfigurationItemChangeListener<T>> listeners;

    public ConfigurationItem(String name) {
        this.name = name;
    }

    public ConfigurationItem(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public ConfigurationItem(String name, T defaultValue, T value) {
        this.name = name;
        hasValue = true;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T get() {
        if (!hasValue) {
            return defaultValue;
        }
        return value;
    }

    public T get(T defaultValue) {
        if (!hasValue) {
            return defaultValue;
        }
        return value;
    }

    public void set(T value) {
        hasValue = true;
        modified = true;
        this.value = value;
        fireConfigurationItemChanged(value);
    }

    public void unset() {
        hasValue = false;
        modified = true;
        this.value = null;
        fireConfigurationItemChanged(defaultValue);
    }

    public boolean hasValue() {
        return hasValue;
    }

    public boolean isModified() {
        return modified;
    }

    public static ConfigurationItem<?> getItem(Field field) {
        try {
            field.setAccessible(true);
            ConfigurationItem<?> item = (ConfigurationItem<?>) field.get(null);
            return item;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ConfigurationItem.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error(ex);
        }
    }

    public static Class<?> getConfigurationFieldType(Field field) {
        Type type = ((ParameterizedType) (field.getGenericType())).getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
    }

    public static String getName(Field field) {
        ConfigurationName annotation = field.getAnnotation(ConfigurationName.class);
        String name = annotation == null ? field.getName() : annotation.value();
        return name;
    }

    public static boolean isInternal(Field field) {
        ConfigurationInternal cint = field.getAnnotation(ConfigurationInternal.class);
        return cint != null;
    }

    @Override
    public String toString() {
        return name;
    }

    private void fireConfigurationItemChanged(T newValue) {
        if (listeners != null) {
            for (ConfigurationItemChangeListener<T> listener : listeners) {
                listener.configurationItemChanged(newValue);
            }
        }
    }

    public void addListener(ConfigurationItemChangeListener<T> l) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }

        listeners.add(l);
    }

    public void removeListener(ConfigurationItemChangeListener<T> l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }
}
