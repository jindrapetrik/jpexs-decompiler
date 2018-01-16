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
package com.jpexs.decompiler.flash.abc;

public class ABCVersion implements Comparable<ABCVersion> {

    public int major = 46;
    public int minor = 16;

    public ABCVersion() {

    }

    public ABCVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    @Override
    public int compareTo(ABCVersion o) {
        if (major != o.major) {
            return major - o.major;
        }
        return minor - o.minor;
    }

    @Override
    public String toString() {
        return "" + major + "." + minor;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.major;
        hash = 53 * hash + this.minor;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ABCVersion other = (ABCVersion) obj;
        return true;
    }

}
