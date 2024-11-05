/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.amf.amf3;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * AMF3 traits.
 */
public class Traits {

    private String className;
    private boolean dynamic;
    private Set<String> sealedMemberNames;

    /**
     * Constructor.
     * @param className Class name
     * @param dynamic Dynamic
     * @param sealedMemberNames Sealed member names
     */
    public Traits(String className, boolean dynamic, Collection<? extends String> sealedMemberNames) {
        this.className = className;
        this.dynamic = dynamic;
        this.sealedMemberNames = new LinkedHashSet<>(sealedMemberNames);
    }

    /**
     * Gets class name.
     * @return Class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Checks if dynamic.
     * @return True if dynamic
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Gets sealed member names.
     * @return Sealed member names
     */
    public Set<String> getSealedMemberNames() {
        return new LinkedHashSet<>(sealedMemberNames);
    }

    /**
     * Sets class name.
     * @param className Class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Sets dynamic.
     * @param dynamic Dynamic
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * Sets sealed member names.
     * @param sealedMemberNames Sealed member names
     */
    public void setSealedMemberNames(Collection<? extends String> sealedMemberNames) {
        this.sealedMemberNames = new LinkedHashSet<>(sealedMemberNames);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.className);
        hash = 11 * hash + (this.dynamic ? 1 : 0);
        hash = 11 * hash + Objects.hashCode(this.sealedMemberNames);
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
        final Traits other = (Traits) obj;
        if (this.dynamic != other.dynamic) {
            return false;
        }
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        return Objects.equals(this.sealedMemberNames, other.sealedMemberNames);
    }   
}
