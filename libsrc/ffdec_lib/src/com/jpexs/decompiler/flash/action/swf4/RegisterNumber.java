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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class RegisterNumber implements Serializable {

    public final int number;

    public String name = null;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + number;
        return hash;
    }

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

    public RegisterNumber(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public RegisterNumber(int number) {
        this(number, null);
    }

    @Override
    public String toString() {
        if (name == null || name.trim().isEmpty()) {
            return toStringNoName();
        }
        return name;
    }

    public String toStringNoName() {
        return "register" + number;
    }

    public String translate() {
        if (name == null || name.trim().isEmpty()) {
            return String.format(Configuration.registerNameFormat.get(), number);
        }
        return name;
    }
}
