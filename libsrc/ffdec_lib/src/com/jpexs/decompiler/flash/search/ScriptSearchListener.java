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
package com.jpexs.decompiler.flash.search;

/**
 * Script search listener.
 *
 * @author JPEXS
 */
public interface ScriptSearchListener {

    /**
     * Called when a script is decompiled.
     *
     * @param pos Position of the script
     * @param total Total number of scripts
     * @param name Name of the script
     */
    public void onDecompile(int pos, int total, String name);

    /**
     * Called when a script is searched.
     *
     * @param pos Position of the script
     * @param total Total number of scripts
     * @param name Name of the script
     */
    public void onSearch(int pos, int total, String name);
}
