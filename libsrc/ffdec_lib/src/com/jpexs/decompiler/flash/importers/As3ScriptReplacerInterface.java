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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import java.io.IOException;
import java.util.List;

/**
 * Interface for replacing ActionScript 3 scripts.
 */
public interface As3ScriptReplacerInterface {

    /**
     * Checks if this replacer is available.
     * @return True if available, false otherwise
     */
    public boolean isAvailable();

    /**
     * Initializes the replacement.
     * @param pack Script pack
     * @param dependencies List of dependencies
     */
    public void initReplacement(ScriptPack pack, List<SWF> dependencies);

    /**
     * Replaces the script.
     * @param pack Script pack
     * @param text Script text
     * @param dependencies List of dependencies
     * @throws As3ScriptReplaceException If the script cannot be replaced
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void replaceScript(ScriptPack pack, String text, List<SWF> dependencies) throws As3ScriptReplaceException, IOException, InterruptedException;

    /**
     * Deinitializes the replacement.
     * @param pack Script pack
     */
    public void deinitReplacement(ScriptPack pack);
}
