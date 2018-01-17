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
package com.jpexs.decompiler.flash.amf.amf3;

import java.util.Collection;
import java.util.Set;

public class Traits {

    private String className;
    private boolean dynamic;
    private Set<String> sealedMemberNames;

    public Traits(String className, boolean dynamic, Collection<? extends String> sealedMemberNames) {
        this.className = className;
        this.dynamic = dynamic;
        this.sealedMemberNames = new ListSet<>(sealedMemberNames);
    }

    public String getClassName() {
        return className;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public Set<String> getSealedMemberNames() {
        return new ListSet<>(sealedMemberNames);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void setSealedMemberNames(Collection<? extends String> sealedMemberNames) {
        this.sealedMemberNames = new ListSet<>(sealedMemberNames);
    }

}
