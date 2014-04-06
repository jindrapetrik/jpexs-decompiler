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

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class MethodAVM2Item extends FunctionAVM2Item {

    private boolean isStatic;
    private boolean isFinal;
    private boolean override;

    public MethodAVM2Item(boolean needsActivation, boolean hasRest, int line, boolean override, boolean isFinal, boolean isStatic, int namespace, String methodName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<NameAVM2Item> subvariables, GraphTargetItem retType) {
        super(needsActivation,namespace, hasRest, line, methodName, paramTypes, paramNames, paramValues, body, subvariables, retType);
        this.isStatic = isStatic;
        this.override = override;
        this.isFinal = isFinal;
    }

    public boolean isOverride() {
        return override;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer; //todo?
    }

}
