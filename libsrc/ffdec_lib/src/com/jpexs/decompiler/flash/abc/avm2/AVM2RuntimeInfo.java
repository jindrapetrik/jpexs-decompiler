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
package com.jpexs.decompiler.flash.abc.avm2;

/**
 * Info of AVM2 runtime.
 *
 * @author JPEXS
 */
public class AVM2RuntimeInfo {

    /**
     * AVM2 runtime.
     */
    public AVM2Runtime runtime;

    /**
     * AVM2 version.
     */
    public int version;

    /**
     * Debug flag.
     */
    public boolean debug;

    /**
     * Constructs AVM2 runtime info.
     *
     * @param runtime AVM2 runtime
     * @param version AVM2 version
     * @param debug Debug flag
     */
    public AVM2RuntimeInfo(AVM2Runtime runtime, int version, boolean debug) {
        this.runtime = runtime;
        this.version = version;
        this.debug = debug;
    }
}
