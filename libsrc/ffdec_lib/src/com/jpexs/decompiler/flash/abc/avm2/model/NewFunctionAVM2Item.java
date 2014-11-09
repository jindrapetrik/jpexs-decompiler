/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

public class NewFunctionAVM2Item extends AVM2Item {

    public String functionName;
    public String path;
    public boolean isStatic;
    public int scriptIndex;
    public int classIndex;
    public ABC abc;
    public List<String> fullyQualifiedNames;
    public AVM2ConstantPool constants;
    public List<MethodInfo> methodInfo;
    public int methodIndex;

    public NewFunctionAVM2Item(AVM2Instruction instruction, String functionName, String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, List<String> fullyQualifiedNames, AVM2ConstantPool constants, List<MethodInfo> methodInfo, int methodIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.functionName = functionName;
        this.path = path;
        this.isStatic = isStatic;
        this.scriptIndex = scriptIndex;
        this.classIndex = classIndex;
        this.abc = abc;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.constants = constants;
        this.methodInfo = methodInfo;
        this.methodIndex = methodIndex;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        MethodBody body = abc.findBody(methodIndex);
        writer.append("function");
        writer.startMethod(methodIndex);
        writer.append((!functionName.isEmpty() ? " " + functionName : ""));
        writer.appendNoHilight("(");
        methodInfo.get(methodIndex).getParamStr(writer, constants, body, abc, fullyQualifiedNames);
        writer.appendNoHilight("):");
        if (Configuration.showMethodBodyId.get()) {
            writer.appendNoHilight("// method body id: ");
            writer.appendNoHilight(abc.findBodyIndex(methodIndex));
            writer.newLine();
        }
        methodInfo.get(methodIndex).getReturnTypeStr(writer, constants, fullyQualifiedNames);
        writer.startBlock();
        if (body != null) {
            if (writer instanceof NulWriter) {
                body.convert(path + "/inner", ScriptExportMode.AS, isStatic, scriptIndex, classIndex, abc, null, constants, methodInfo, new ScopeStack(), false, writer, fullyQualifiedNames, null, false);
            } else {
                body.toString(path + "/inner", ScriptExportMode.AS, abc, null, constants, methodInfo, writer, fullyQualifiedNames);
            }
        }
        writer.endBlock();
        writer.endMethod();
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("Function");
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
