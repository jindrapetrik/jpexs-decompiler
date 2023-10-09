/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 *
 * @author JPEXS
 */
public class ActionGraphSource extends GraphSource {

    private final ActionList actions;

    public int version;

    private final HashMap<Integer, String> registerNames;

    private final HashMap<String, GraphTargetItem> variables;

    private final HashMap<String, GraphTargetItem> functions;

    private final boolean insideDoInitAction;

    private final String path;

    private String charset;

    public List<Action> getActions() {
        return actions;
    }

    public ActionGraphSource(String path, boolean insideDoInitAction, List<Action> actions, int version, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, String charset) {
        this.actions = actions instanceof ActionList ? (ActionList) actions : new ActionList(actions, charset);
        this.version = version;
        this.registerNames = registerNames;
        this.variables = variables;
        this.functions = functions;
        this.insideDoInitAction = insideDoInitAction;
        this.path = path;
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public Set<Long> getImportantAddresses() {
        return Action.getActionsAllRefs(actions);
    }

    @Override
    public String insToString(int pos) {
        if (pos < actions.size()) {
            return actions.get(pos).getASMSource(actions, getImportantAddresses(), ScriptExportMode.PCODE);
        }
        return "";
    }

    @Override
    public int size() {
        return actions.size();
    }

    @Override
    public GraphSourceItem get(int pos) {
        return actions.get(pos);
    }

    public void set(int pos, Action t) {
        actions.set(pos, t);
    }

    @Override
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    @Override
    public List<GraphTargetItem> translatePart(Graph graph, GraphPart part, BaseLocalData localData, TranslateStack stack, int start, int end, int staticOperation, String path) throws InterruptedException, GraphPartChangeException {
        Reference<GraphSourceItem> fi = new Reference<>(localData.lineStartInstruction);

        List<GraphTargetItem> r = Action.actionsPartToTree((ActionGraph) graph, localData.allSwitchParts, localData.secondPassData, this.insideDoInitAction, fi, registerNames, variables, functions, stack, actions, start, end, version, staticOperation, path, charset);
        localData.lineStartInstruction = fi.getVal();
        return r;
    }

    private List<Long> posCache = null;

    private void rebuildCache() {
        posCache = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            posCache.add(pos2adr(i));
        }
    }

    @Override
    public long pos2adr(int pos) {
        GraphSourceItem si = actions.get(pos);
        if (si instanceof Action) {
            return ((Action) si).getAddress();
        }
        return 0;
    }

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
                Logger.getLogger(ActionGraphSource.class.getName()).log(Level.SEVERE, "{0} - address loc{1} not found", new Object[]{path, Helper.formatAddress(adr)});
            }
        }
        return ret;
    }

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

    public HashMap<String, GraphTargetItem> getVariables() {
        return variables;
    }

}
