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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class ClassAVM2Item extends AVM2Item implements Block {

    public List<GraphTargetItem> functions;
    public List<GraphTargetItem> staticFunctions;
    public GraphTargetItem extendsOp;
    public List<GraphTargetItem> implementsOp;
    public GraphTargetItem className;
    public GraphTargetItem constructor;
    public List<GraphTargetItem> vars;
    public List<GraphTargetItem> staticVars;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (functions != null) {
            ret.add(functions);
        }
        if (staticFunctions != null) {
            ret.add(staticFunctions);
        }
        return ret;
    }

    public ClassAVM2Item(GraphTargetItem className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, GraphTargetItem constructor, List<GraphTargetItem> functions, List<GraphTargetItem> vars, List<GraphTargetItem> staticFunctions, List<GraphTargetItem> staticVars) {
        super(null, NOPRECEDENCE);
        this.className = className;
        this.functions = functions;
        this.vars = vars;
        this.extendsOp = extendsOp;
        this.implementsOp = implementsOp;
        this.staticFunctions = staticFunctions;
        this.staticVars = staticVars;
        this.constructor = constructor;

        List<GraphTargetItem> allFunc = new ArrayList<>(functions);
        if (constructor != null) {
            allFunc.add(constructor);
        }        
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
