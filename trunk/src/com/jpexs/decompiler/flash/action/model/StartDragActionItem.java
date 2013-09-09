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

import com.jpexs.decompiler.flash.action.swf4.ActionStartDrag;
import com.jpexs.decompiler.flash.ecma.*;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.List;

public class StartDragActionItem extends ActionItem {

    public GraphTargetItem target;
    public GraphTargetItem lockCenter;
    public GraphTargetItem constrain;
    public GraphTargetItem y2;
    public GraphTargetItem x2;
    public GraphTargetItem y1;
    public GraphTargetItem x1;

    public StartDragActionItem(GraphSourceItem instruction, GraphTargetItem target, GraphTargetItem lockCenter, GraphTargetItem constrain, GraphTargetItem x1, GraphTargetItem y1, GraphTargetItem x2, GraphTargetItem y2) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
        this.lockCenter = lockCenter;
        this.constrain = constrain;
        this.y2 = y2;
        this.x2 = x2;
        this.y1 = y1;
        this.x1 = x1;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        boolean hasConstrains = true;
        if (constrain instanceof DirectValueActionItem) {
            if (Double.compare(EcmaScript.toNumber(constrain.getResult()), 0) == 0) {
                hasConstrains = false;
            }
        }
        return hilight("startDrag(", highlight) + target.toString(highlight, constants) + hilight(",", highlight) + lockCenter.toString(highlight, constants) + (hasConstrains ? hilight(",", highlight) + x1.toString(highlight, constants) + hilight(",", highlight) + y1.toString(highlight, constants) + hilight(",", highlight) + x2.toString(highlight, constants) + hilight(",", highlight) + y2.toString(highlight, constants) : "") + hilight(")", highlight);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        ret.addAll(constrain.getNeededSources());
        ret.addAll(x1.getNeededSources());
        ret.addAll(x2.getNeededSources());
        ret.addAll(y1.getNeededSources());
        ret.addAll(y2.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        boolean hasConstrains = true;
        if (constrain instanceof DirectValueActionItem) {
            if (Double.compare(EcmaScript.toNumber(constrain.getResult()), 0) == 0) {
                hasConstrains = false;
            }
        }
        if (hasConstrains) {
            return toSourceMerge(localData, generator, x1, y1, x2, y2, constrain, lockCenter, target, new ActionStartDrag());
        } else {
            return toSourceMerge(localData, generator, constrain, lockCenter, target, new ActionStartDrag());
        }
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
