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
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.HashMap;

/**
 *
 * @author JPEXS
 */
public class ActionLocalData extends BaseLocalData {

    public final HashMap<Integer, String> regNames;

    public final HashMap<String, GraphTargetItem> variables;

    public final HashMap<String, GraphTargetItem> functions;

    public GraphSourceItem lineStartAction;

    public boolean insideDoInitAction;

    public ActionLocalData(boolean insideDoInitAction) {
        regNames = new HashMap<>();
        variables = new HashMap<>();
        functions = new HashMap<>();
        this.insideDoInitAction = insideDoInitAction;
    }

    public ActionLocalData(boolean insideDoInitAction, HashMap<Integer, String> regNames) {
        this.regNames = regNames;
        variables = new HashMap<>();
        functions = new HashMap<>();
        this.insideDoInitAction = insideDoInitAction;
    }

    public ActionLocalData(boolean insideDoInitAction, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        this.regNames = regNames;
        this.variables = variables;
        this.functions = functions;
        this.insideDoInitAction = insideDoInitAction;
    }
}
