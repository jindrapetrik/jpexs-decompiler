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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;

public class FullMultinameAVM2Item extends AVM2Item {

    public int multinameIndex;
    public GraphTargetItem name;
    public GraphTargetItem namespace;

    public FullMultinameAVM2Item(AVM2Instruction instruction, int multinameIndex, GraphTargetItem name) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = null;
    }

    public FullMultinameAVM2Item(AVM2Instruction instruction, int multinameIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = null;
        this.namespace = null;
    }

    public FullMultinameAVM2Item(AVM2Instruction instruction, int multinameIndex, GraphTargetItem name, GraphTargetItem namespace) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = namespace;
    }

    public boolean isRuntime() {
        return (name != null) || (namespace != null);
    }

    public boolean isXML(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String cname;
        if (name != null) {
            cname = name.toString(false, LocalData.create(constants, localRegNames, fullyQualifiedNames));
        } else {
            cname = (constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames));
        }
        String cns = "";
        if (namespace != null) {
            cns = namespace.toString(false, LocalData.create(constants, localRegNames, fullyQualifiedNames));
        } else {
            Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
            if ((ns != null) && (ns.name_index != 0)) {
                cns = ns.getName(constants);
            }
        }
        return cname.equals("XML") && cns.equals("");
    }

    @Override
    protected HilightedTextWriter appendTo(HilightedTextWriter writer, LocalData localData) {
        if (namespace != null) {
            namespace.toString(writer, localData);
            writer.append("::");
        } else {
            /*Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
             if ((ns != null)&&(ns.name_index!=0)) {
             ret =  hilight(ns.getName(constants) + "::")+ret;
             }*/
        }
        if (name != null) {
            writer.append("[");
            name.toString(writer, localData);
            writer.append("]");
        } else {
            ConstantPool constants = localData.constantsAvm2;
            List<String> fullyQualifiedNames = localData.fullyQualifiedNames;
            writer.append(constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames));
        }
        return writer;
    }

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
        if (tiNameSpace != tiNameSpace2) {
            return false;
        }
        return true;
    }
}
