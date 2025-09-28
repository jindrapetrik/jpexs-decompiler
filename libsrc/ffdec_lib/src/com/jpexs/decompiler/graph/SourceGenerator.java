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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SwapItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.List;

/**
 * Source generator interface. A low-level code generator.
 *
 * @author JPEXS
 */
public interface SourceGenerator {

    /**
     * Generates source code for PushItem.
     *
     * @param localData Local data
     * @param item PushItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, PushItem item) throws CompilationException;

    /**
     * Generates source code for PopItem.
     *
     * @param localData Local data
     * @param item PopItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, PopItem item) throws CompilationException;

    /**
     * Generates source code for TrueItem.
     *
     * @param localData Local data
     * @param item TrueItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TrueItem item) throws CompilationException;

    /**
     * Generates source code for FalseItem.
     *
     * @param localData Local data
     * @param item FalseItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, FalseItem item) throws CompilationException;

    /**
     * Generates source code for AndItem.
     *
     * @param localData Local data
     * @param item AndItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, AndItem item) throws CompilationException;

    /**
     * Generates source code for OrItem.
     *
     * @param localData Local data
     * @param item OrItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, OrItem item) throws CompilationException;

    /**
     * Generates source code for IfItem.
     *
     * @param localData Local data
     * @param item IfItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, IfItem item) throws CompilationException;

    /**
     * Generates source code for TernarOpItem.
     *
     * @param localData Local data
     * @param item TernarOpItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TernarOpItem item) throws CompilationException;

    /**
     * Generates source code for WhileItem.
     *
     * @param localData Local data
     * @param item WhileItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WhileItem item) throws CompilationException;

    /**
     * Generates source code for DoWhileItem.
     *
     * @param localData Local data
     * @param item DoWhileItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DoWhileItem item) throws CompilationException;

    /**
     * Generates source code for ForItem.
     *
     * @param localData Local data
     * @param item ForItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForItem item) throws CompilationException;

    /**
     * Generates source code for SwitchItem.
     *
     * @param localData Local data
     * @param item SwitchItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, SwitchItem item) throws CompilationException;

    /**
     * Generates source code for NotItem.
     *
     * @param localData Local data
     * @param item NotItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, NotItem item) throws CompilationException;

    /**
     * Generates source code for DuplicateItem.
     *
     * @param localData Local data
     * @param item DuplicateItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DuplicateItem item) throws CompilationException;

    /**
     * Generates source code for BreakItem.
     *
     * @param localData Local data
     * @param item BreakItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, BreakItem item) throws CompilationException;

    /**
     * Generates source code for ContinueItem.
     *
     * @param localData Local data
     * @param item ContinueItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ContinueItem item) throws CompilationException;

    /**
     * Generates source code for commands.
     *
     * @param localData Local data
     * @param commands List of GraphTargetItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException;

    /**
     * Generates source code for CommaExpressionItem.
     *
     * @param localData Local data
     * @param item CommaExpressionItem
     * @param withReturnValue If true, the return value is used
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, CommaExpressionItem item, boolean withReturnValue) throws CompilationException;

    /**
     * Generates source code for TypeItem.
     *
     * @param localData Local data
     * @param item TypeItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TypeItem item) throws CompilationException;
    
    /**
     * Generates source code for SwapItem.
     *
     * @param localData Local data
     * @param item SwapItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, SwapItem item) throws CompilationException;   

    /**
     * Generates DiscardValue action.
     *
     * @param localData Local data
     * @param item GraphTargetItem
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generateDiscardValue(SourceGeneratorLocalData localData, GraphTargetItem item) throws CompilationException;
}
