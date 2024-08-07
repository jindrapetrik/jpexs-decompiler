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
package com.jpexs.decompiler.flash.action;

/**
 * Constant pool too big exception.
 *
 * @author JPEXS
 */
public class ConstantPoolTooBigException extends Exception {

    /**
     * Index of new item
     */
    public int index;

    /**
     * Size of constant pool
     */
    public int size;

    /**
     * Constructs a new ConstantPoolTooBigException with the specified index and
     * size.
     *
     * @param index Index of new item
     * @param size Size of constant pool
     */
    public ConstantPoolTooBigException(int index, int size) {
        super("Constant pool too big. index=" + index + ", size=" + size);
        this.index = index;
        this.size = size;
    }
}
