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

import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import static com.jpexs.decompiler.graph.GraphTargetItem.PRECEDENCE_PRIMARY;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ActionItem extends GraphTargetItem implements Serializable {

    public ActionItem() {
        super(null, NOPRECEDENCE);
    }

    public ActionItem(GraphSourceItem instruction, int precedence) {
        super(instruction, precedence);
    }

    public abstract String toString(ConstantPool constants);

    @Override
    public String toString() {
        ConstantPool c = null;
        return toString(c);
    }

    protected boolean isEmptyString(GraphTargetItem target) {
        if (target instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) target).value instanceof String) {

                if (((DirectValueActionItem) target).value.equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected String stripQuotes(GraphTargetItem target, ConstantPool constants) {
        if (target instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) target).value instanceof String) {
                return (String) ((DirectValueActionItem) target).hilight((String) ((DirectValueActionItem) target).value);
            }
        }
        if (target == null) {
            return "";
        } else {
            return target.toString(constants);
        }
    }

    @Override
    public String toString(List<Object> localData) {
        if (localData.isEmpty()) {
            ConstantPool c = null;
            return toString(c);
        }
        return toString((ConstantPool) localData.get(0));
    }

    protected List<GraphSourceItem> toSourceCall(List<Object> localData, SourceGenerator gen, List<GraphTargetItem> list) {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ret.addAll(0, list.get(i).toSource(localData, gen));
        }
        ret.add(new ActionPush((Long) (long) list.size()));
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = toSource(localData, generator);
        if (hasReturnValue()) {
            ret.add(new ActionPop());
        }
        return ret;
    }
}
