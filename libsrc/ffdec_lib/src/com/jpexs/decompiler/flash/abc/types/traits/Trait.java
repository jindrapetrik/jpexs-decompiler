/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Trait implements Cloneable, Serializable {

    public static final String METADATA_CTOR_DEFINITION = "__go_to_ctor_definition_help";

    public static final String METADATA_DEFINITION = "__go_to_definition_help";

    private static final int[] EMPTY_METADATA_ARRAY = new int[0];

    public int name_index;

    public int kindType;

    public int kindFlags;

    public int[] metadata = EMPTY_METADATA_ARRAY;

    public long fileOffset;

    public byte[] bytes;

    public static final int ATTR_Final = 0x1;

    public static final int ATTR_Override = 0x2;

    public static final int ATTR_Metadata = 0x4;

    public static final int TRAIT_SLOT = 0;

    public static final int TRAIT_METHOD = 1;

    public static final int TRAIT_GETTER = 2;

    public static final int TRAIT_SETTER = 3;

    public static final int TRAIT_CLASS = 4;

    public static final int TRAIT_FUNCTION = 5;

    public static final int TRAIT_CONST = 6;

    public abstract void delete(ABC abc, boolean d);

    public final List<Entry<String, Map<String, String>>> getMetaDataTable(ABC abc) {
        List<Entry<String, Map<String, String>>> ret = new ArrayList<>();
        for (int m : metadata) {
            if (m >= 0 && m < abc.metadata_info.size()) {
                String name = abc.constants.getString(abc.metadata_info.get(m).name_index);
                Map<String, String> data = new HashMap<>();
                for (int i = 0; i < abc.metadata_info.get(m).keys.length; i++) {
                    data.put(abc.constants.getString(abc.metadata_info.get(m).keys[i]),
                            abc.constants.getString(abc.metadata_info.get(m).values[i]));
                }
                ret.add(new SimpleEntry<>(name, data));
            }
        }
        return ret;
    }

    public final GraphTextWriter getMetaData(ABC abc, GraphTextWriter writer) {
        List<Entry<String, Map<String, String>>> md = getMetaDataTable(abc);
        for (Entry<String, Map<String, String>> en : md) {
            String name = en.getKey();
            if (METADATA_DEFINITION.equals(name) || METADATA_CTOR_DEFINITION.equals(name)) {
                continue;
            }
            writer.append("[").append(IdentifiersDeobfuscation.printIdentifier(true, name));
            if (!en.getValue().isEmpty()) {
                writer.append("(");
                boolean first = true;
                for (String key : en.getValue().keySet()) {
                    if (!first) {
                        writer.append(",");
                    }
                    first = false;
                    if (key != null && !key.isEmpty()) {
                        writer.append(IdentifiersDeobfuscation.printIdentifier(true, key)).append("=");
                    }
                    writer.append("\"");
                    String val = en.getValue().get(key);
                    writer.append(Helper.escapeActionScriptString(val));
                    writer.append("\"");
                }
                writer.append(")");
            }
            writer.append("]");
            writer.newLine();
        }
        return writer;
    }

    protected final DottedChain findCustomNs(int link_ns_index, ABC abc) {
        String nsname = "";
        if (link_ns_index <= 0) {
            return null;
        }
        Namespace ns = abc.constants.getNamespace(link_ns_index);
        if (ns.kind != Namespace.KIND_NAMESPACE) {
            return null;
        }
        String name = abc.constants.getString(ns.name_index);
        for (ABCContainerTag abcTag : abc.getAbcTags()) {
            DottedChain dc = abcTag.getABC().nsValueToName(name);
            nsname = dc.getLast();

            if (nsname == null) {
                continue;
            }
            if (!nsname.isEmpty()) {
                return dc;
            }
        }
        return null;
    }

    public final GraphTextWriter getModifiers(ABC abc, boolean isStatic, GraphTextWriter writer) {
        if ((kindFlags & ATTR_Override) > 0) {
            writer.appendNoHilight("override ");
        }
        Multiname m = getName(abc);
        if (m != null) {
            DottedChain dc = findCustomNs(m.namespace_index, abc);
            String nsname = dc != null ? dc.getLast() : null;

            Namespace ns = m.getNamespace(abc.constants);

            if (nsname != null) {
                String identifier = IdentifiersDeobfuscation.printIdentifier(true, nsname);
                if (identifier != null && !identifier.isEmpty()) {
                    writer.appendNoHilight(identifier).appendNoHilight(" ");
                }
            }
            if (ns != null) {
                String nsPrefix = ns.getPrefix(abc);
                if (nsPrefix != null && !nsPrefix.isEmpty()) {
                    writer.appendNoHilight(nsPrefix).appendNoHilight(" ");
                }
            }
        }
        if (isStatic) {
            if ((this instanceof TraitSlotConst) && ((TraitSlotConst) this).isNamespace()) {
                //static is automatic
            } else {
                writer.appendNoHilight("static ");
            }
        }
        if ((kindFlags & ATTR_Final) > 0) {
            if (!isStatic) {
                writer.appendNoHilight("final ");
            }
        }
        return writer;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public GraphTextWriter toString(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        writer.appendNoHilight(abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata));
        return writer;
    }

    public void convert(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
    }

    public GraphTextWriter toStringPackaged(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = ns.getName(abc.constants).toPrintableString(true);
            writer.appendNoHilight("package");
            if (!nsname.isEmpty()) {
                writer.appendNoHilight(" " + nsname); //assume not null name
            }
            writer.startBlock();
            toString(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            writer.endBlock();
            writer.newLine();
        }
        return writer;
    }

    public void convertPackaged(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = ns.getName(abc.constants).toPrintableString(true);
            convert(parent, convertData, path + nsname, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        }
    }

    public GraphTextWriter toStringHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        toString(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        return writer;
    }

    public void convertHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        convert(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
    }

    public final Multiname getName(ABC abc) {
        if (name_index == 0) {
            return null;
        } else {
            return abc.constants.getMultiname(name_index);
        }
    }

    public abstract int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException;

    public final ClassPath getPath(ABC abc) {
        Multiname name = getName(abc);
        Namespace ns = name.getNamespace(abc.constants);
        DottedChain packageName = ns == null ? DottedChain.EMPTY : ns.getName(abc.constants);
        String objectName = name.getName(abc.constants, null, true);
        return new ClassPath(packageName, objectName); //assume not null name
    }

    @Override
    public Trait clone() {
        try {
            Trait ret = (Trait) super.clone();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
