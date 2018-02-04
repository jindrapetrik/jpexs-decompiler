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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
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

    public List<Action> getActions() {
        return actions;
    }

    public ActionGraphSource(String path, boolean insideDoInitAction, List<Action> actions, int version, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        this.actions = actions instanceof ActionList ? (ActionList) actions : new ActionList(actions);
        this.version = version;
        this.registerNames = registerNames;
        this.variables = variables;
        this.functions = functions;
        this.insideDoInitAction = insideDoInitAction;
        this.path = path;
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
    public List<GraphTargetItem> translatePart(GraphPart part, BaseLocalData localData, TranslateStack stack, int start, int end, int staticOperation, String path) throws InterruptedException {
        Reference<GraphSourceItem> fi = new Reference<>(localData.lineStartInstruction);

        List<GraphTargetItem> r = Action.actionsPartToTree(this.insideDoInitAction, fi, registerNames, variables, functions, stack, actions, start, end, version, staticOperation, path);
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

    /* public int adr2posInside(long addr){
     long lastAddr=0;
     if(addr==0){
     return 0;
     }
     for(int i=0;i<size();i++){
     long curAdr=pos2adr(i);
     if(curAdr==addr){
     return i;
     }
     if(curAdr>addr){
     System.err.println("lastAddr="+lastAddr+" addr="+addr+" curAddr="+curAdr);
     int contPos=adr2pos(lastAddr);
     System.err.println("/insadr2po");
     GraphSourceItem src=get(contPos);
     if(src instanceof ActionContainer){
     ActionContainer cnt=(ActionContainer)src;
     return new ActionGraphSource(cnt.getActions(), version, registerNames, variables, functions).adr2pos(addr);
     }else{
     return -1;
     }
     }
     lastAddr=curAdr;
     }
     return -1;
     }*/
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
            //ret = adr2posInside(adr);
            if (ret == -1) {
                Logger.getLogger(ActionGraphSource.class.getName()).log(Level.SEVERE, "{0} - address loc{1} not found", new Object[]{path, Helper.formatAddress(adr)});
                /*System.err.println("Addr loc"+Helper.formatAddress(adr)+" not found");
                 int pos=0;
                 for(long l:posCache){
                 System.err.println("ip "+pos+" action "+get(pos).toString()+" loc"+Helper.formatAddress(l));
                 pos++;
                 }*/
            }
        }
        return ret;
        /*int pos = 0;
         long lastAddr = 0;
         for (Action a : actions) {
         lastAddr = a.getAddress();
         System.err.println("ip "+pos+" addr "+Helper.formatAddress(lastAddr));
         if (lastAddr == adr) {
         return pos;
         }

         pos++;
         }
         if (adr > lastAddr) {
         return actions.size();
         }
         if (adr == 0) {
         return 0;
         }
         //throw new RuntimeException("Address "+Helper.formatAddress(adr)+" not found");
         return -1;*/
    }

    @Override
    public long pos2adr(int pos) {
        GraphSourceItem si = actions.get(pos);
        if (si instanceof Action) {
            return ((Action) si).getAddress();//Action.ip2adr(actions, pos, version);
        }
        return 0;
    }
}
