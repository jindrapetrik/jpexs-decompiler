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
package com.jpexs.decompiler.flash.abc;

/**
 * Represents ABC version
 */
public class ABCVersion implements Comparable<ABCVersion> {

    /**
     * Major version
     */
    public int major = 46;

    /**
     * Minor version
     */
    public int minor = 16;

    /**
     * Constructs new ABCVersion
     */
    public ABCVersion() {

    }

    /**
     * Constructs new ABCVersion
     *
     * @param major Major version
     * @param minor Minor version
     */
    public ABCVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    /**
     * Compares ABCVersion with another ABCVersion
     *
     * @param o the object to be compared.
     * @return Negative number if this version is lower, 0 if versions are
     * equal, positive number if this version is higher
     */
    @Override
    public int compareTo(ABCVersion o) {
        if (major != o.major) {
            return major - o.major;
        }
        return minor - o.minor;
    }

    /**
     * Returns string representation of ABCVersion
     *
     * @return String representation of ABCVersion
     */
    @Override
    public String toString() {
        return "" + major + "." + minor;
    }

    /**
     * Returns hash code of ABCVersion
     *
     * @return Hash code of ABCVersion
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.major;
        hash = 53 * hash + this.minor;
        return hash;
    }

    /**
     * Equals method
     *
     * @param obj Object to compare
     * @return True if objects are equal
     */
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
