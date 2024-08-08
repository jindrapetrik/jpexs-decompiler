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
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.graph.DottedChain;
import java.util.Objects;

/**
 * Dependency of a script.
 */
public class Dependency {

    /**
     * Id
     */
    private DottedChain id;

    /**
     * Type
     */
    private DependencyType type;

    /**
     * Constructor.
     *
     * @param id ID of the dependency
     * @param type Type of the dependency
     */
    public Dependency(DottedChain id, DependencyType type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Gets ID of the dependency.
     *
     * @return ID of the dependency
     */
    public DottedChain getId() {
        return id;
    }

    /**
     * Gets type of the dependency.
     *
     * @return Type of the dependency
     */
    public DependencyType getType() {
        return type;
    }

    @Override
    public String toString() {
        return id.toString() + " (" + type + ")";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + Objects.hashCode(this.type);
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
        final Dependency other = (Dependency) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

}
