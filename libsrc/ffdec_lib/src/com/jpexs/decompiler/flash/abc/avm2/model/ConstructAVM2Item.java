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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.List;
import java.util.Objects;

/**
 * Construct object.
 *
 * @author JPEXS
 */
public class ConstructAVM2Item extends AVM2Item {

    /**
     * Object
     */
    public GraphTargetItem object;

    /**
     * Arguments
     */
    public List<GraphTargetItem> args;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param object Object
     * @param args Arguments
     */
    public ConstructAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, List<GraphTargetItem> args) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.object = object;
        this.args = args;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(object);
        visitor.visitAll(args);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (object instanceof NewFunctionAVM2Item) {
            writer.append("new ");
            return object.toString(writer, localData);
        }
        writer.append("new ");

        boolean objectIsCall = (object instanceof CallAVM2Item)
                || (object instanceof CallPropertyAVM2Item)
                || (object instanceof CallMethodAVM2Item)
                || (object instanceof CallStaticAVM2Item)
                || (object instanceof CallSuperAVM2Item);

        boolean isGetDefinition = false;
        if (object instanceof GetPropertyAVM2Item) {
            GetPropertyAVM2Item getProperty = (GetPropertyAVM2Item) object;
            if (getProperty.propertyName instanceof FullMultinameAVM2Item) {
                FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProperty.propertyName;
                if (fm.multinameIndex > 0) {
                    Multiname m = localData.abc.constants.getMultiname(fm.multinameIndex);
                    if (m.kind == Multiname.MULTINAMEL) {
                        if (m.getNamespaceSet(localData.abc.constants).namespaces.length == 1) {
                            isGetDefinition = true;
                            writer.append("(");
                            if (localData.fullyQualifiedNames.contains(DottedChain.parseNoSuffix("flash.utils.getDefinitionByName"))) {
                                writer.append("flash.utils.getDefinitionByName");
                            } else {
                                writer.append("getDefinitionByName");
                            }
                            writer.append("(");
                            String nname = m.getSingleNamespace(localData.abc.constants).getName(localData.abc.constants).toRawString();
                            if (!nname.isEmpty()) {
                                writer.append("\"");
                                writer.append(Helper.escapeActionScriptString(nname));                        
                                writer.append("\"+\".\"+");                    
                            }
                            fm.name.appendTo(writer, localData);
                            writer.append("))");
                        }
                    }
                }
            }
        }
        
        if (!isGetDefinition) {
            if (object.getPrecedence() > getPrecedence() || objectIsCall) {
                writer.append("(");
            }
            object.toString(writer, localData);
            if (object.getPrecedence() > getPrecedence() || objectIsCall) {
                writer.append(")");
            }
        }
        writer.spaceBeforeCallParenthesis(args.size());
        if (object instanceof InitVectorAVM2Item) {
            return writer;
        }
        writer.append("(");
        for (int a = 0; a < args.size(); a++) {
            if (a > 0) {
                writer.allowWrapHere().append(",");
            }
            args.get(a).toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public GraphTargetItem returnType() {
        return object.returnType();
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.object);
        hash = 59 * hash + Objects.hashCode(this.args);
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
        final ConstructAVM2Item other = (ConstructAVM2Item) obj;
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (!Objects.equals(this.args, other.args)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}
