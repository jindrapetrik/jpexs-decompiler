/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.ecma.Undefined;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents ActionScript array.
 *
 * @author JPEXS
 */
public class ActionScriptArray extends ActionScriptObject {

    /**
     * Array values
     */
    protected List<Object> values = new ArrayList<>();

    //TODO: implement some methods?

    /**
     * Constructor.
     */
    public ActionScriptArray() {
    }

    /**
     * Enumerates all members of this object
     *
     * @return List of member names
     */
    @Override
    public List<String> enumerate() {
        List<String> ret = super.enumerate();
        for (int i = 0; i < values.size(); i++) {
            ret.add("" + i);
        }
        return ret;
    }

    /**
     * Gets member of this object
     *
     * @param path Member path
     * @return Member value
     */
    @Override
    public Object getMember(String path) {
        if (path.matches("[1-9][0-9]*|0")) {
            return getValueAtIndex(Integer.parseInt(path));
        }
        return super.getMember(path);
    }

    /**
     * Sets member of this object
     *
     * @param path Member path
     * @param value Value to set
     */
    @Override
    public void setMember(String path, Object value) {
        if (path.matches("[1-9][0-9]*|0")) {
            setValueAtIndex(Integer.parseInt(path), value);
            return;
        }
        super.setMember(path, value);
    }

    /**
     * Sets value at index
     *
     * @param index Index
     * @param value Value
     */
    public void setValueAtIndex(int index, Object value) {
        if (index < 0) {
            return;
        }
        if (index >= values.size()) {
            int delta = 1 + index - values.size();
            for (int i = 0; i < delta - 1; i++) {
                values.add(Undefined.INSTANCE);
            }
            values.add(value);
        } else {
            values.set(index, value);
        }
        trim();
    }

    /**
     * Trims array by removing trailing undefined values
     */
    public void trim() {
        for (int i = values.size() - 1; i >= 0; i--) {
            if (values.get(i) == Undefined.INSTANCE) {
                values.remove(i);
            } else {
                break;
            }
        }
    }

    /**
     * Gets value at index
     *
     * @param index Index
     * @return Value
     */
    public Object getValueAtIndex(int index) {
        if (index < 0) {
            return Undefined.INSTANCE; //throw error?
        }
        if (index >= values.size()) {
            return Undefined.INSTANCE;
        }
        return values.get(index);
    }
}
