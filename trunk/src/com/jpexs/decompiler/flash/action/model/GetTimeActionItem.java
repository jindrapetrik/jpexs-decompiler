/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.action.swf4.ActionGetTime;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.List;
import java.util.Random;

public class GetTimeActionItem extends ActionItem {

    public GetTimeActionItem(GraphSourceItem instruction) {
        super(instruction, PRECEDENCE_PRIMARY);
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("getTimer()");
    }

    @Override
    public boolean isCompileTime() {
        return true;
    }

    @Override
    public Object getResult() {
        return (Double) (double) new Random().nextInt(10000) + 1000;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, new ActionGetTime());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
