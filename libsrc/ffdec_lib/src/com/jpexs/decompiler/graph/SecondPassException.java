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
package com.jpexs.decompiler.graph;

/**
 * Second pass exception.
 *
 * @author JPEXS
 */
public class SecondPassException extends RuntimeException {

    /**
     * Second pass data.
     */
    private final SecondPassData data;

    /**
     * Constructs a new SecondPassException.
     *
     * @param data Second pass data
     */
    public SecondPassException(SecondPassData data) {
        this.data = data;
    }

    /**
     * Gets the second pass data.
     *
     * @return Second pass data
     */
    public SecondPassData getData() {
        return data;
    }

}
