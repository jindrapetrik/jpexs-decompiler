/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.configuration;

import java.util.ArrayList;
import java.util.List;

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
