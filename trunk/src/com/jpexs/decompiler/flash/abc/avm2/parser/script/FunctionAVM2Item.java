/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FunctionAVM2Item extends AVM2Item {

    public String calculatedFunctionName;
    public String functionName;
    public List<String> paramNames;
    public List<GraphTargetItem> body;
    public List<NameAVM2Item> subvariables;
    public List<GraphTargetItem> paramTypes;
    public List<GraphTargetItem> paramValues;
    public GraphTargetItem retType;

    public FunctionAVM2Item(String functionName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<NameAVM2Item> subvariables, GraphTargetItem retType) {
        super(null, NOPRECEDENCE);
        this.paramNames = paramNames;
        this.body = body;
        this.functionName = functionName;
        this.subvariables = subvariables;
        this.paramTypes = paramTypes;
        this.paramValues = paramValues;
        this.retType = retType;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer; //todo?
    }

    @Override
    public GraphTargetItem returnType() {
        return retType;
    }
    
    

}
