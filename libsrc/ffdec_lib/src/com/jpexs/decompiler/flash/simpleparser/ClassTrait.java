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
package com.jpexs.decompiler.flash.simpleparser;

/**
 * Class trait.
 *
 * @author JPEXS
 */
public class ClassTrait extends Variable {

    public final Path classFullName;
    public final String customNs;
    public final Path simpleName;

    /**
     * Constructor
     *
     * @param classFullName Full class name including package
     * @param name Name
     * @param customNs Custom namespace
     * @param position Position
     * @param isStatic True = static
     * @param type Type
     * @param callType Call type
     * @param subType Sub type
     * @param callSubType Call sub type
     */
    public ClassTrait(Path classFullName, Path name, String customNs, int position, boolean isStatic, Path type, Path callType, Variable subType, Variable callSubType) {
        super(true, isStatic ? classFullName.add(name) : new Path("this").add((customNs != null ? customNs + "::" : "") + name), position, isStatic, type, callType, subType, callSubType);
        this.classFullName = classFullName;
        this.customNs = customNs;
        this.simpleName = name;
    }

    @Override
    public String toString() {
        return (isStatic ? "static " : "") + "trait " + name + " of class " + classFullName + " at " + position;
    }

    public Path getFullIdentifier() {
        return classFullName.add((customNs != null ? customNs + "::" : "") + simpleName);
    }
}
