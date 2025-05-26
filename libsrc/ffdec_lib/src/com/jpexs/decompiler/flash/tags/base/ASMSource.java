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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionTreeOperation;
import com.jpexs.decompiler.flash.action.ConstantPoolTooBigException;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.HasSwfAndTag;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.ByteArrayRange;
import java.util.List;

/**
 * Object containing ActionScript 1/2 bytecode.
 *
 * @author JPEXS
 */
public interface ASMSource extends Exportable, HasSwfAndTag {

    /**
     * Converts actions to ASM source.
     *
     * @param exportMode PCode or hex?
     * @param writer Writer
     * @param actions Actions
     * @return ASM source
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter getASMSource(ScriptExportMode exportMode, GraphTextWriter writer, ActionList actions) throws InterruptedException;

    /**
     * Converts actions to ActionScript source.
     *
     * @param writer Writer
     * @param actions Actions
     * @return ASM source
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter getActionScriptSource(GraphTextWriter writer, ActionList actions) throws InterruptedException;

    /**
     * Converts actions to ActionScript source with executing operation on the
     * tree.
     *
     * @param writer Writer
     * @param actions List of actions
     * @param treeOperations List of operations to execute on the tree
     * @return ASM source
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter getActionScriptSource(GraphTextWriter writer, ActionList actions, List<ActionTreeOperation> treeOperations) throws InterruptedException;

    /**
     * Whether this object contains ASM source.
     *
     * @return True when contains
     */
    public boolean containsSource();

    /**
     * Returns actions associated with this object.
     *
     * @return List of actions
     * @throws InterruptedException On interrupt
     */
    public ActionList getActions() throws InterruptedException;

    /**
     * Sets actions associated with this object.
     *
     * @param actions Action list
     */
    public void setActions(List<Action> actions);

    /**
     * Gets actions as tree.
     * @return List of graph target items
     */
    public List<GraphTargetItem> getActionsToTree();

    /**
     * Sets modified flag.
     */
    public void setModified();

    /**
     * Gets action bytes.
     * @return Action bytes
     */
    public ByteArrayRange getActionBytes();

    /**
     * Sets action bytes.
     * @param actionBytes Action bytes
     */
    public void setActionBytes(byte[] actionBytes);

    /**
     * Sets constant pools.
     * @param constantPools List of constant pools
     * @throws ConstantPoolTooBigException When constant pool is too big
     */
    public void setConstantPools(List<List<String>> constantPools) throws ConstantPoolTooBigException;

    /**
     * Gets action bytes as hex.
     * @param writer Writer
     * @return Writer
     */
    public GraphTextWriter getActionBytesAsHex(GraphTextWriter writer);

    /**
     * Adds disassembly listener.
     * @param listener Listener
     */
    public void addDisassemblyListener(DisassemblyListener listener);

    /**
     * Removes disassembly listener.
     * @param listener Listener
     */
    public void removeDisassemblyListener(DisassemblyListener listener);

    /**
     * Gets action source prefix.
     * @param writer Writer
     * @return Writer
     */
    public GraphTextWriter getActionSourcePrefix(GraphTextWriter writer);

    /**
     * Gets action source suffix.
     * @param writer Writer
     * @return Writer
     */
    public GraphTextWriter getActionSourceSuffix(GraphTextWriter writer);

    /**
     * Gets prefix line count.
     * @return Line count
     */
    public int getPrefixLineCount();

    /**
     * Removes prefix and suffix from source.
     * @param source Source
     * @return Source without prefix and suffix
     */
    public String removePrefixAndSuffix(String source);

    /**
     * Gets source tag.
     * @return Source tag
     */
    public Tag getSourceTag();

    @Override
    public void setSourceTag(Tag t);

    /**
     * Gets script name.
     * @return Script name
     */
    public String getScriptName();

    /**
     * Sets script name.
     * @param scriptName Script name
     */
    public void setScriptName(String scriptName);

    /**
     * Gets exported script name.
     * @return Exported script name
     */
    public String getExportedScriptName();

    /**
     * Sets exported script name.
     * @param scriptName Exported script name
     */
    public void setExportedScriptName(String scriptName);
}
