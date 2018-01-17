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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface GraphSourceItemContainer {

    public long getHeaderSize();

    public List<Long> getContainerSizes();

    public void setContainerSize(int index, long size);

    public String getASMSourceBetween(int pos);

    public boolean parseDivision(long size, FlasmLexer lexer);

    public HashMap<Integer, String> getRegNames();

    public void translateContainer(List<List<GraphTargetItem>> contents, GraphSourceItem lineStartItem, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions);

    public String getName();
}
