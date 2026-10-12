/*
 * Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Names that can be used without a class qualifier in an AS2 class body.
 */
public class As2ClassMemberScope {

    private final List<String> classPath;
    private final Set<String> members;
    private final Set<String> shadowed;

    public As2ClassMemberScope(GraphTargetItem className, Set<String> members) {
        this(path(className), members, new HashSet<>());
    }

    private As2ClassMemberScope(List<String> classPath, Set<String> members, Set<String> shadowed) {
        this.classPath = classPath;
        this.members = members;
        this.shadowed = shadowed;
    }

    public As2ClassMemberScope forFunction(FunctionActionItem function) {
        Set<String> names = new HashSet<>(shadowed);
        names.addAll(function.paramNames);
        names.addAll(function.regNames.values());
        AbstractGraphTargetVisitor visitor = new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                if (item instanceof FunctionActionItem) {
                    names.add(((FunctionActionItem) item).functionName);
                    return false;
                }
                if (item instanceof DefineLocalActionItem) {
                    GraphTargetItem name = ((DefineLocalActionItem) item).name;
                    if (name instanceof DirectValueActionItem && ((DirectValueActionItem) name).isString()) {
                        names.add(((DirectValueActionItem) name).getAsString());
                    }
                }
                return true;
            }
        };
        for (GraphTargetItem action : function.actions) {
            if (visitor.visit(action)) {
                action.visitRecursively(visitor);
            }
        }
        return new As2ClassMemberScope(classPath, members, names);
    }

    public As2ClassMemberScope withShadowedName(String name) {
        Set<String> names = new HashSet<>(shadowed);
        names.add(name);
        return new As2ClassMemberScope(classPath, members, names);
    }

    public static boolean canShorten(GraphTargetItem object, GraphTargetItem member, LocalData localData) {
        As2ClassMemberScope scope = localData.as2ClassMemberScope;
        if (scope == null || !(member instanceof DirectValueActionItem)
                || !((DirectValueActionItem) member).isString()) {
            return false;
        }
        String name = ((DirectValueActionItem) member).getAsString();
        return IdentifiersDeobfuscation.isValidName(false, name)
                && scope.members.contains(name) && !scope.shadowed.contains(name)
                && scope.classPath != null && !scope.classPath.isEmpty()
                && !scope.shadowed.contains(scope.classPath.get(0))
                && scope.classPath.equals(path(object));
    }

    private static List<String> path(GraphTargetItem item) {
        List<String> result = new ArrayList<>();
        while (item instanceof GetMemberActionItem) {
            GetMemberActionItem member = (GetMemberActionItem) item;
            if (!(member.memberName instanceof DirectValueActionItem)
                    || !((DirectValueActionItem) member.memberName).isString()) {
                return null;
            }
            result.add(0, ((DirectValueActionItem) member.memberName).getAsString());
            item = member.object;
        }
        String root;
        if (item instanceof GetVariableActionItem) {
            GraphTargetItem name = ((GetVariableActionItem) item).name;
            if (!(name instanceof DirectValueActionItem) || !((DirectValueActionItem) name).isString()) {
                return null;
            }
            root = ((DirectValueActionItem) name).getAsString();
        } else if (item instanceof VariableActionItem) {
            root = ((VariableActionItem) item).getVariableName();
        } else {
            return null;
        }
        if (!"_global".equals(root)) {
            result.add(0, root);
        }
        return result;
    }
}
