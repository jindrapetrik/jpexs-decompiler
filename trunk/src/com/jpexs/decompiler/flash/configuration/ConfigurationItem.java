/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.configuration;

/**
 *
 * @author JPEXS
 */
public class ConfigurationItem<T> {

    private final String name;
    protected boolean hasValue;

    private T value;
    private T defaultValue;
    private boolean modified;

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
    }

    public void unset() {
        hasValue = false;
        modified = true;
        this.value = null;
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
}
