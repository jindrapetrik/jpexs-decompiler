/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class SourceGeneratorLocalData implements Serializable {

    public HashMap<String, Integer> registerVars;
    public Integer inFunction;
    public Boolean inMethod;
    public Integer forInLevel;

    //TODO: handle AVM2 separately
    public List<ABCException> exceptions = new ArrayList<>();
    public List<Integer> finallyCatches = new ArrayList<Integer>();
    public Map<Integer, Integer> finallyCounter = new HashMap<>();
    public int finallyRegister = -1;
    public String currentClass;
    public int activationReg = 0;
    public List<MethodBody> callStack = new ArrayList<>();
    public Map<MethodBody, List<Integer>> traitUsages = new HashMap<>();
    public String pkg = "";
    public List<GraphTargetItem> scopeStack = new ArrayList<GraphTargetItem>();
    public boolean documentClass;
    public ScriptInfo currentScript;

    public SourceGeneratorLocalData(HashMap<String, Integer> registerVars, Integer inFunction, Boolean inMethod, Integer forInLevel) {
        this.registerVars = registerVars;
        this.inFunction = inFunction;
        this.inMethod = inMethod;
        this.forInLevel = forInLevel;
    }
}
