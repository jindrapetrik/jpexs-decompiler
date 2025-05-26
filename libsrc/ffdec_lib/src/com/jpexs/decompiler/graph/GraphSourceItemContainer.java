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

import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import java.util.HashMap;
import java.util.List;

/**
 * Container for source items.
 *
 * @author JPEXS
 */
public interface GraphSourceItemContainer {

    /**
     * Gets the size of the header.
     *
     * @return The size of the header.
     */
    public long getHeaderSize();

    /**
     * Gets the sizes of the container parts.
     *
     * @return List of sizes of the container parts.
     */
    public List<Long> getContainerSizes();

    /**
     * Sets the size of the container part.
     *
     * @param index Index of the container part.
     * @param size Size of the container part.
     */
    public void setContainerSize(int index, long size);

    /**
     * Gets the ASM source between the specified position.
     *
     * @param pos Index of container part
     * @return ASM source between the specified position
     */
    public String getASMSourceBetween(int pos);

    /**
     * Parses the division
     *
     * @param size Size up to this point
     * @param lexer Lexer
     * @return True if the division was parsed successfully
     */
    public boolean parseDivision(long size, FlasmLexer lexer);

    /**
     * Gets the names of the registers.
     *
     * @return The names of the registers. Map of register index and register
     * name.
     */
    public HashMap<Integer, String> getRegNames();

    /**
     * Translates the container to high level code.
     *
     * @param contents List of contents of the container parts
     * @param lineStartItem Start source item of the line
     * @param stack Stack
     * @param output Output
     * @param regNames Register names
     * @param variables Variables
     * @param functions Functions
     */
    public void translateContainer(List<List<GraphTargetItem>> contents, GraphSourceItem lineStartItem, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions);

    /**
     * Gets the name of the container.
     *
     * @return The name of the container
     */
    public String getName();
}
