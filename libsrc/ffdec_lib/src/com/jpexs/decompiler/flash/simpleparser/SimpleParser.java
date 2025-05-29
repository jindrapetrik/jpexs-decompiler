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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public interface SimpleParser {
    
    /**
     * Parses document.
     *
     * @param str The string to convert
     * @param definitionPosToReferences Definition position to references
     * @param referenceToDefinition Reference to definition
     * @param errors Errors
     * @throws SimpleParseException On parse error
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void parse(
            String str,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors
    ) throws SimpleParseException, IOException, InterruptedException;
}
