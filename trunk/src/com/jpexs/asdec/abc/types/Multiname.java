/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;


public class Multiname {

    public static final int QNAME = 7;
    public static final int QNAMEA = 13;
    public static final int MULTINAME = 9;
    public static final int MULTINAMEA = 14;
    public static final int RTQNAME = 15;
    public static final int RTQNAMEA = 16;
    public static final int MULTINAMEL = 27;
    public static final int RTQNAMEL = 17;
    public static final int RTQNAMELA = 18;
    private static final int multinameKinds[] = new int[]{QNAME, QNAMEA, MULTINAME, MULTINAMEA, RTQNAME, RTQNAMEA, MULTINAMEL, RTQNAMEL, RTQNAMELA};
    private static final String multinameKindNames[] = new String[]{"Qname", "QnameA", "Multiname", "MultinameA", "RTQname", "RTQnameA", "MultinameL", "RTQnameL", "RTQnameLA"};


    public int kind = -1;
    public int name_index = -1;
    public int namespace_index = -1;
    public int namespace_set_index = -1;


    public Multiname(int kind, int name_index, int namespace_index, int namespace_set_index) {
        this.kind = kind;
        this.name_index = name_index;
        this.namespace_index = namespace_index;
        this.namespace_set_index = namespace_set_index;
    }

    public boolean isAttribute() {
        if (kind == QNAMEA) return true;
        if (kind == MULTINAMEA) return true;
        if (kind == RTQNAMEA) return true;
        if (kind == RTQNAMELA) return true;
        return false;
    }

    public boolean isRuntime() {
        if (kind == RTQNAME) return true;
        if (kind == RTQNAMEA) return true;
        if (kind == MULTINAMEL) return true;
        return false;
    }

    public boolean needsName() {
        if (kind == RTQNAMEL) return true;
        if (kind == RTQNAMELA) return true;
        if (kind == MULTINAMEL) return true;
        return false;
    }

    public boolean needsNs() {
        if (kind == RTQNAME) return true;
        if (kind == RTQNAMEA) return true;
        if (kind == RTQNAMEL) return true;
        if (kind == RTQNAMELA) return true;
        return false;
    }

    public String getKindStr() {
        String kindStr = "?";
        for (int k = 0; k < multinameKinds.length; k++) {
            if (multinameKinds[k] == kind) {
                kindStr = multinameKindNames[k];
                break;
            }
        }
        return kindStr;
    }

    @Override
    public String toString() {
        String kindStr = getKindStr();
        return "kind=" + kindStr + " name_index=" + name_index + " namespace_index=" + namespace_index + " namespace_set_index=" + namespace_set_index;

    }

    public String toString(ConstantPool constants) {
        String kindStr = "?";
        for (int k = 0; k < multinameKinds.length; k++) {
            if (multinameKinds[k] == kind) {
                kindStr = multinameKindNames[k] + " ";
                break;
            }
        }
        String nameStr = "";
        if (name_index > 0) {
            nameStr = constants.constant_string[name_index];
        }
        if (name_index == 0) {
            nameStr = "*";
        }
        String namespaceStr = "";
        if (namespace_index > 0) {
            namespaceStr = constants.constant_namespace[namespace_index].toString(constants);
        }
        if (!namespaceStr.equals(""))
            namespaceStr = namespaceStr + ".";
        if (namespace_index == 0) {
            namespaceStr = "*.";
        }
        String namespaceSetStr = "";
        if (namespace_set_index > 0) {
            namespaceSetStr = " <NS:" + constants.constant_namespace_set[namespace_set_index].toString(constants) + ">";
        }
        //kindStr+" "+
        return namespaceStr + nameStr + namespaceSetStr;

    }

    public String getName(ConstantPool constants) {
        if (name_index == -1) {
            return "";
        }
        if (name_index == 0) {
            return "*";
        } else {
            return (isAttribute() ? "@" : "") + constants.constant_string[name_index];
        }
    }

    public Namespace getNamespace(ConstantPool constants) {
        if ((namespace_index == 0) || (namespace_index == -1)) {
            return null;
        } else {
            return constants.constant_namespace[namespace_index];
        }
    }

    public NamespaceSet getNamespaceSet(ConstantPool constants) {
        if (namespace_set_index == 0) {
            return null;
        } else {
            return constants.constant_namespace_set[namespace_set_index];
        }
    }
}