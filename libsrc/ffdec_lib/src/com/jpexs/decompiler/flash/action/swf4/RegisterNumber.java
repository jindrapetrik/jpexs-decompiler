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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.Serializable;

/**
 * Register number.
 *
 * @author JPEXS
 */
public class RegisterNumber implements Serializable {

    /**
     * Register number.
     */
    public final int number;

    /**
     * Register name.
     */
    public String name = null;

    /**
     * Hash code.
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + number;
        return hash;
    }

    /**
     * Equals.
     *
     * @param obj Object
     * @return True if equals
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegisterNumber other = (RegisterNumber) obj;
        if (number != other.number) {
            return false;
        }
        return true;
    }

    /**
     * Constructs a new register number.
     *
     * @param number Register number
     * @param name Register name
     */
    public RegisterNumber(int number, String name) {
        this.number = number;
        this.name = name;
    }

    /**
     * Constructs a new register number.
     *
     * @param number Register number
     */
    public RegisterNumber(int number) {
        this(number, null);
    }

    /**
     * To string.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        if (name == null || name.trim().isEmpty()) {
            return toStringNoName();
        }
        return name;
    }

    /**
     * To string without name.
     *
     * @return String representation
     */
    public String toStringNoName() {
        return "register" + number;
    }

    /**
     * Translate register number to name.
     *
     * @return Translated name
     */
    public String translate() {
        if (name == null || name.trim().isEmpty()) {
            return String.format(Configuration.registerNameFormat.get(), number);
        }
        return name;
    }
}
