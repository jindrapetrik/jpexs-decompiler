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

import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.InitArrayActionItem;
import com.jpexs.decompiler.flash.action.model.InitObjectActionItem;
import com.jpexs.decompiler.flash.ecma.ArrayType;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import java.util.ArrayList;
import java.util.List;

/**
 * AVM1 dialect.
 *
 * @author JPEXS
 */
public class ActionGraphTargetDialect extends GraphTargetDialect {

    /**
     * AVM1 dialect instance.
     */
    public static final GraphTargetDialect INSTANCE = new ActionGraphTargetDialect();

    private ActionGraphTargetDialect() {

    }

    @Override
    public String getName() {
        return "Action";
    }

    @Override
    public GraphTargetItem valToItem(Object r) {
        if (r == null) {
            return null;
        }
        if (r instanceof Boolean) {
            if ((Boolean) r) {
                return new TrueItem(this, null, null);
            } else {
                return new FalseItem(this, null, null);
            }
        }
        if (r instanceof String) {
            return new DirectValueActionItem((String) r);
        }
        if (r instanceof Long) {
            return new DirectValueActionItem((Long) r);
        }
        if (r instanceof Integer) {
            return new DirectValueActionItem((Long) (long) (Integer) r);
        }
        if (r instanceof Short) {
            return new DirectValueActionItem((Long) (long) (Short) r);
        }
        if (r instanceof Byte) {
            return new DirectValueActionItem((Long) (long) (Byte) r);
        }
        if (r instanceof Double) {
            return new DirectValueActionItem((Double) r);
        }
        if (r instanceof Null) {
            return new DirectValueActionItem(Null.INSTANCE);
        }
        if (r instanceof Undefined) {
            return new DirectValueActionItem(Undefined.INSTANCE);
        }
        if (r instanceof ArrayType) {
            List<GraphTargetItem> vals = new ArrayList<>();
            ArrayType at = (ArrayType) r;
            for (Object v : at.values) {
                vals.add(valToItem(v));
            }
            return new InitArrayActionItem(null, null, vals);
        }
        if (r instanceof ObjectType) {
            List<GraphTargetItem> names = new ArrayList<>();
            List<GraphTargetItem> vals = new ArrayList<>();
            ObjectType ot = (ObjectType) r;
            for (String k : ot.getAttributeNames()) {
                names.add(valToItem(k));
                vals.add(valToItem(ot.getAttribute(k)));
            }
            return new InitObjectActionItem(null, null, names, vals);
        }
        return null;
    }

    @Override
    public boolean doesAllowMultilevelBreaks() {
        return false;
    }
}
