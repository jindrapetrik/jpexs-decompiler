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
package com.jpexs.decompiler.flash.abc.avm2.treemodel;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;

public class FullMultinameTreeItem extends TreeItem {

    public int multinameIndex;
    public GraphTargetItem name;
    public GraphTargetItem namespace;

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, GraphTargetItem name) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = null;
    }

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = null;
        this.namespace = null;
    }

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, GraphTargetItem name, GraphTargetItem namespace) {
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
            cname = name.toString(constants, localRegNames, fullyQualifiedNames);
        } else {
            cname = (constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames));
        }
        String cns = "";
        if (namespace != null) {
            cns = namespace.toString(constants, localRegNames, fullyQualifiedNames);
        } else {
            Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
            if ((ns != null) && (ns.name_index != 0)) {
                cns = ns.getName(constants);
            }
        }
        return cname.equals("XML") && cns.equals("");
    }

    @Override
    public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String ret = "";
        if (name != null) {
            ret = "[" + name.toString(constants, localRegNames, fullyQualifiedNames) + "]";
        } else {
            ret = hilight(constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames));
        }
        if (namespace != null) {
            ret = namespace.toString(constants, localRegNames, fullyQualifiedNames) + "::" + ret;
        } else {
            /*Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
             if ((ns != null)&&(ns.name_index!=0)) {
             ret =  hilight(ns.getName(constants) + "::")+ret;
             }*/
        }
        return ret;
    }

    public boolean compareSame(FullMultinameTreeItem other) {
        if (multinameIndex != other.multinameIndex) {
            return false;
        }
        GraphTargetItem tiName = name;
        if (name != null) {
            name = name.getThroughDuplicate();
        }
        while (tiName instanceof LocalRegTreeItem) {
            if (((LocalRegTreeItem) tiName).computedValue != null) {
                tiName = ((LocalRegTreeItem) tiName).computedValue.getThroughNotCompilable().getThroughDuplicate();
            } else {
                break;
            }
        }

        GraphTargetItem tiName2 = other.name;
        if (tiName2 != null) {
            tiName2 = tiName2.getThroughDuplicate();
        }
        while (tiName2 instanceof LocalRegTreeItem) {
            if (((LocalRegTreeItem) tiName2).computedValue != null) {
                tiName2 = ((LocalRegTreeItem) tiName2).computedValue.getThroughNotCompilable().getThroughDuplicate();
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
        while (tiNameSpace instanceof LocalRegTreeItem) {
            if (((LocalRegTreeItem) tiNameSpace).computedValue != null) {
                tiNameSpace = ((LocalRegTreeItem) tiNameSpace).computedValue.getThroughNotCompilable().getThroughDuplicate();
            }
        }

        GraphTargetItem tiNameSpace2 = other.namespace;
        if (tiNameSpace2 != null) {
            tiNameSpace2 = tiNameSpace2.getThroughDuplicate();
        }
        while (tiNameSpace2 instanceof LocalRegTreeItem) {
            if (((LocalRegTreeItem) tiNameSpace2).computedValue != null) {
                tiNameSpace2 = ((LocalRegTreeItem) tiNameSpace2).computedValue.getThroughNotCompilable().getThroughDuplicate();
            }
        }
        if (tiNameSpace != tiNameSpace2) {
            return false;
        }
        return true;
    }
}
