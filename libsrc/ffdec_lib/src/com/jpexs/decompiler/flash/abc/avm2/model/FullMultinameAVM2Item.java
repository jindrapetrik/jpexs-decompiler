/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Fully resolved multiname.
 *
 * @author JPEXS
 */
public class FullMultinameAVM2Item extends AVM2Item {

    /**
     * Multiname index
     */
    public int multinameIndex;

    /**
     * Name
     */
    public GraphTargetItem name;

    /**
     * Namespace
     */
    public GraphTargetItem namespace;

    /**
     * Is property
     */
    public boolean property;

    /**
     * Resolved multiname name
     */
    public String resolvedMultinameName;

    /**
     * Constructor.
     *
     * @param property Is property
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param multinameIndex Multiname index
     * @param resolvedMultinameName Resolved multiname name
     * @param name Name
     */
    public FullMultinameAVM2Item(boolean property, GraphSourceItem instruction, GraphSourceItem lineStartIns, int multinameIndex, String resolvedMultinameName, GraphTargetItem name) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = null;
        this.property = property;
        this.resolvedMultinameName = resolvedMultinameName;
    }

    /**
     * Constructor.
     * @param property Is property
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param multinameIndex Multiname index
     * @param resolvedMultinameName Resolved multiname name
     */
    public FullMultinameAVM2Item(boolean property, GraphSourceItem instruction, GraphSourceItem lineStartIns, int multinameIndex, String resolvedMultinameName) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.resolvedMultinameName = resolvedMultinameName;
        this.name = null;
        this.namespace = null;
        this.property = property;
    }

    /**
     * Constructor.
     * @param property Is property
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param multinameIndex Multiname index
     * @param resolvedMultinameName Resolved multiname name
     * @param name Name
     * @param namespace Namespace
     */
    public FullMultinameAVM2Item(boolean property, GraphSourceItem instruction, GraphSourceItem lineStartIns, int multinameIndex, String resolvedMultinameName, GraphTargetItem name, GraphTargetItem namespace) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = namespace;
        this.property = property;
        this.resolvedMultinameName = resolvedMultinameName;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        if (name != null) {
            visitor.visit(name);
        }
        if (namespace != null) {
            visitor.visit(namespace);
        }
    }

    /**
     * Is runtime multiname.
     * @return Is runtime multiname
     */
    public boolean isRuntime() {
        return (name != null) || (namespace != null);
    }

    /**
     * Is top level.
     * @param tname Top level name
     * @param abc ABC
     * @param localRegNames Local register names
     * @param fullyQualifiedNames Fully qualified names
     * @param seenMethods Seen methods
     * @return Is top level
     * @throws InterruptedException On interrupt
     */
    public boolean isTopLevel(String tname, ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, Set<Integer> seenMethods) throws InterruptedException {
        String cname;
        if (name != null) {
            cname = name.toString(LocalData.create(new ArrayList<>(), null, abc, localRegNames, fullyQualifiedNames, seenMethods, ScriptExportMode.AS, -1));
        } else {
            cname = (abc.constants.getMultiname(multinameIndex).getName(abc.constants, fullyQualifiedNames, true, true));
        }
        String cns = "";
        if (namespace != null) {
            cns = namespace.toString(LocalData.create(new ArrayList<>(), null, abc, localRegNames, fullyQualifiedNames, seenMethods, ScriptExportMode.AS, -1));
        } else {
            Namespace ns = abc.constants.getMultiname(multinameIndex).getNamespace(abc.constants);
            if ((ns != null) && (ns.name_index != 0)) {
                cns = ns.getName(abc.constants).toPrintableString(true);
            }
        }
        return cname.equals(tname) && cns.isEmpty();
    }

    /**
     * Is XML.
     * @param abc ABC
     * @param localRegNames Local register names
     * @param fullyQualifiedNames Fully qualified names
     * @param seenMethods Seen methods
     * @return Is XML
     * @throws InterruptedException On interrupt
     */
    public boolean isXML(ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, Set<Integer> seenMethods) throws InterruptedException {
        return isTopLevel("XML", abc, localRegNames, fullyQualifiedNames, seenMethods);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (namespace != null) {
            namespace.toString(writer, localData);
            writer.append("::");
        } else {
            /*Namespace ns = constants.getMultiname(multinameIndex).getNamespace(constants);
             if ((ns != null)&&(ns.name_index!=0)) {
             ret =  hilight(ns.getName(constants) + "::")+ret;
             }*/
        }
        if (name != null) {
            writer.append("[");
            if (name instanceof IntegerValueAVM2Item) {
                name.toString(writer, localData);
            } else {
                name.toString(writer, localData);
            }
            writer.append("]");
        } else {
            AVM2ConstantPool constants = localData.constantsAvm2;
            List<DottedChain> fullyQualifiedNames = property ? new ArrayList<>() : localData.fullyQualifiedNames;
            if (multinameIndex > 0 && multinameIndex < constants.getMultinameCount()) {
                String simpleName = constants.getMultiname(multinameIndex).getName(constants, fullyQualifiedNames, true, false);
                if ("*".equals(simpleName)) {
                    writer.append("*");
                } else {
                    writer.append(constants.getMultiname(multinameIndex).getNameWithCustomNamespace(localData.abc, fullyQualifiedNames, false, true));
                }
            } else {
                writer.append("§§multiname(").append(multinameIndex).append(")");
            }
        }
        return writer;
    }

    /**
     * Compare same.
     * @param other Other
     * @return Is same
     */
    public boolean compareSame(FullMultinameAVM2Item other) {
        if (multinameIndex != other.multinameIndex) {
            return false;
        }
        GraphTargetItem tiName = name;
        if (name != null) {
            name = name.getThroughDuplicate();
        }
        while (tiName instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiName).computedValue != null) {
                tiName = ((LocalRegAVM2Item) tiName).computedValue.getThroughNotCompilable().getThroughDuplicate();
            } else {
                break;
            }
        }

        GraphTargetItem tiName2 = other.name;
        if (tiName2 != null) {
            tiName2 = tiName2.getThroughDuplicate();
        }
        while (tiName2 instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiName2).computedValue != null) {
                tiName2 = ((LocalRegAVM2Item) tiName2).computedValue.getThroughNotCompilable().getThroughDuplicate();
            } else {
                break;
            }
        }
        if (tiName != tiName2) {
            return false;
        }

        GraphTargetItem tiNameSpace = namespace;
        if (tiNameSpace != null) {
            tiNameSpace = tiNameSpace.getThroughDuplicate();
        }
        while (tiNameSpace instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiNameSpace).computedValue != null) {
                tiNameSpace = ((LocalRegAVM2Item) tiNameSpace).computedValue.getThroughNotCompilable().getThroughDuplicate();
            }
        }

        GraphTargetItem tiNameSpace2 = other.namespace;
        if (tiNameSpace2 != null) {
            tiNameSpace2 = tiNameSpace2.getThroughDuplicate();
        }
        while (tiNameSpace2 instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiNameSpace2).computedValue != null) {
                tiNameSpace2 = ((LocalRegAVM2Item) tiNameSpace2).computedValue.getThroughNotCompilable().getThroughDuplicate();
            }
        }
        return (tiNameSpace == tiNameSpace2);
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.multinameIndex;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.namespace);
        hash = 29 * hash + (this.property ? 1 : 0);
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
        final FullMultinameAVM2Item other = (FullMultinameAVM2Item) obj;
        if (this.multinameIndex != other.multinameIndex) {
            return false;
        }
        if (this.property != other.property) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.namespace, other.namespace)) {
            return false;
        }
        return true;
    }

}
