/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class NewFunctionAVM2Item extends AVM2Item {

    public String functionName;

    public String path;

    public boolean isStatic;

    public int scriptIndex;

    public int classIndex;

    public ABC abc;

    public List<DottedChain> fullyQualifiedNames;

    public int methodIndex;

    public ScopeStack scopeStack;

    public NewFunctionAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, String functionName, String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, List<DottedChain> fullyQualifiedNames, int methodIndex, ScopeStack scopeStack) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.functionName = functionName;
        this.path = path;
        this.isStatic = isStatic;
        this.scriptIndex = scriptIndex;
        this.classIndex = classIndex;
        this.abc = abc;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.methodIndex = methodIndex;
        this.scopeStack = scopeStack;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (localData.seenMethods.contains(methodIndex)) {
            return writer.append("§§method(").append(methodIndex).append(")");
        }
        //if (methodIndex == 9141)
        MethodBody body = abc.findBody(methodIndex);
        writer.append("function");
        writer.startMethod(methodIndex);
        writer.append((!functionName.isEmpty() ? " " + functionName : ""));
        writer.appendNoHilight("(");
        abc.method_info.get(methodIndex).getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
        writer.appendNoHilight("):");
        if (Configuration.showMethodBodyId.get()) {
            writer.appendNoHilight("// method body index: ");
            writer.appendNoHilight(abc.findBodyIndex(methodIndex));
            writer.appendNoHilight(" method index: ");
            writer.appendNoHilight(methodIndex);
            writer.newLine();
        }
        abc.method_info.get(methodIndex).getReturnTypeStr(writer, abc.constants, fullyQualifiedNames);
        writer.startBlock();
        if (body != null) {
            body.convert(new ConvertData(), path + "/inner", ScriptExportMode.AS, isStatic, methodIndex, scriptIndex, classIndex, abc, null, new ScopeStack(), 0, new NulWriter(), fullyQualifiedNames, null, false, new HashSet<>(localData.seenMethods));
            body.toString(path + "/inner", ScriptExportMode.AS, abc, null, writer, fullyQualifiedNames, new HashSet<>(localData.seenMethods));
        }
        writer.endBlock();
        writer.endMethod();
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.FUNCTION);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.functionName);
        hash = 37 * hash + this.methodIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NewFunctionAVM2Item other = (NewFunctionAVM2Item) obj;
        if (this.methodIndex != other.methodIndex) {
            return false;
        }
        if (!Objects.equals(this.functionName, other.functionName)) {
            return false;
        }
        return true;
    }

}
