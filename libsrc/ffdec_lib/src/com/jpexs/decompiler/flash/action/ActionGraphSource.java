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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphPartChangeException;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ActionScript 1/2 graph source
 *
 * @author JPEXS
 */
public class ActionGraphSource extends GraphSource {

    /**
     * Actions
     */
    private final ActionList actions;

    /**
     * SWF version
     */
    public int version;

    /**
     * Register names - map of register number to register name
     */
    private final HashMap<Integer, String> registerNames;

    /**
     * Variables - map of variable name to variable item
     */
    private final HashMap<String, GraphTargetItem> variables;

    /**
     * Functions - map of function name to function item
     */
    private final HashMap<String, GraphTargetItem> functions;

    /**
     * Is inside doInitAction
     */
    private final boolean insideDoInitAction;

    /**
     * Path
     */
    private final String path;

    /**
     * Charset - SWFs version 5 and lower do not use UTF-8 charset
     */
    private String charset;

    /**
     * Position cache
     */
    private List<Long> posCache = null;

    /**
     * Gets actions
     *
     * @return Actions
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Constructs new ActionGraphSource
     *
     * @param path Path
     * @param insideDoInitAction Is inside doInitAction
     * @param actions Actions
     * @param version SWF version
     * @param registerNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param charset Charset
     */
    public ActionGraphSource(String path, boolean insideDoInitAction, List<Action> actions, int version, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, String charset, int startIp) {
        this.startIp = startIp;
        this.actions = actions instanceof ActionList ? (ActionList) actions : new ActionList(actions, charset);        
        this.version = version;
        this.registerNames = registerNames;
        this.variables = variables;
        this.functions = functions;
        this.insideDoInitAction = insideDoInitAction;
        this.path = path;
        this.charset = charset;
    }

    /**
     * Gets charset
     *
     * @return Charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Gets the important addresses
     *
     * @return Set of important addresses
     */
    @Override
    public Set<Long> getImportantAddresses() {
        return Action.getActionsAllRefs(actions);
    }

    /**
     * Converts instruction at the specified position to string
     *
     * @param pos Position of the instruction
     * @return Instruction as string
     */
    @Override
    public String insToString(int pos) {
        if (pos < actions.size()) {
            return actions.get(pos).getASMSource(actions, getImportantAddresses(), ScriptExportMode.PCODE);
        }
        return "";
    }

    /**
     * Gets the size of the graph source
     *
     * @return The size of the graph source
     */
    @Override
    public int size() {
        return actions.size();
    }

    /**
     * Gets the graph source item at the specified position
     *
     * @param pos Position of the graph source item
     * @return The graph source item at the specified position
     */
    @Override
    public GraphSourceItem get(int pos) {
        return actions.get(pos);
    }

    /**
     * Sets the graph source item at the specified position
     *
     * @param pos Position of the graph source item
     * @param t The graph source item
     */
    public void set(int pos, Action t) {
        actions.set(pos, t);
    }

    /**
     * Checks if the graph source is empty
     *
     * @return True if the graph source is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    /**
     * Translates the part of the graph source
     *
     * @param output Output
     * @param graph Graph
     * @param part Graph part
     * @param localData Local data
     * @param stack Translate stack
     * @param start Start position
     * @param end End position
     * @param staticOperation Unused
     * @param path Path
     * @throws InterruptedException On interrupt
     * @throws GraphPartChangeException On graph part change
     */
    @Override
    public void translatePart(List<GraphTargetItem> output, Graph graph, GraphPart part, BaseLocalData localData, TranslateStack stack, int start, int end, int staticOperation, String path) throws InterruptedException, GraphPartChangeException {
        Reference<GraphSourceItem> fi = new Reference<>(localData.lineStartInstruction);

        Action.actionsPartToTree(output, (ActionGraph) graph, localData.allSwitchParts, localData.secondPassData, this.insideDoInitAction, fi, registerNames, variables, functions, stack, actions, start, end, version, staticOperation, path, charset);
        localData.lineStartInstruction = fi.getVal();
    }

    /**
     * Rebuilds the position cache
     */
    private void rebuildCache() {
        posCache = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            posCache.add(pos2adr(i));
        }
    }

    /**
     * Converts position to address
     *
     * @param pos Position
     * @return Address
     */
    @Override
    public long pos2adr(int pos) {
        GraphSourceItem si = actions.get(pos);
        if (si instanceof Action) {
            return ((Action) si).getAddress();
        }
        return 0;
    }

    /**
     * Converts address to position
     *
     * @param adr Address
     * @return Position
     */
    @Override
    public int adr2pos(long adr) {
        if (posCache == null) {
            rebuildCache();
        }
        if (adr == 0) {
            return 0;
        }
        int ret = posCache.indexOf((Long) adr);
        if (ret == -1) {
            if (!posCache.isEmpty() && (adr > posCache.get(posCache.size() - 1))) {
                return size();
            }
            if (ret == -1) {
                Logger.getLogger(ActionGraphSource.class.getName()).log(Level.SEVERE, "address loc{1} not found in {0}", new Object[]{path, Helper.formatAddress(adr)});
            }
        }
        return ret;
    }

    /**
     * Converts address to position
     *
     * @param adr Address
     * @param nearest Nearest
     * @return Position
     */
    @Override
    public int adr2pos(long adr, boolean nearest) {
        if (posCache == null) {
            rebuildCache();
        }
        if (adr == 0) {
            return 0;
        }
        int ret = posCache.indexOf((Long) adr);
        if (ret == -1) {
            if (!posCache.isEmpty() && (adr > posCache.get(posCache.size() - 1))) {
                return size();
            }
            for (int i = 0; i < posCache.size(); i++) {
                Long a = posCache.get(i);
                if (a > adr) {
                    return i;
                }
            }
            return size();
        }
        return ret;
    }

    /**
     * Gets variables
     *
     * @return Map of variable name to variable item
     */
    public HashMap<String, GraphTargetItem> getVariables() {
        return variables;
    }

    public String getPath() {
        return path;
    }   
}
