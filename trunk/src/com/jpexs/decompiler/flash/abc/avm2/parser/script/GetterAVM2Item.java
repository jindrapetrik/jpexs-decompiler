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

import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GetterAVM2Item extends MethodAVM2Item {

    public GetterAVM2Item(String customNamespace, boolean needsActivation, boolean hasRest, int line, boolean override, boolean isFinal, boolean isStatic, int namespace, String methodName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<AssignableAVM2Item> subvariables, GraphTargetItem retType) {
        super(customNamespace, needsActivation, hasRest, line, override, isFinal, isStatic, namespace, methodName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }

}
