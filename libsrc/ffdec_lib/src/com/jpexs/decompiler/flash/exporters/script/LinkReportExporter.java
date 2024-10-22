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
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class LinkReportExporter - generates Linker reports similar to Flex
 * -link-report, but for SWF files.
 *
 * @author JPEXS
 */
public class LinkReportExporter {

    private String newLineChar = "\n";
    private String indentStr = "  ";

    /**
     * Constructs reporter with LF as newline, two spaces as indent.
     */
    public LinkReportExporter() {

    }

    /**
     * Constructs reporter with custom newline char, two spaces as indent.
     *
     * @param newLineChar Newline char
     */
    public LinkReportExporter(String newLineChar) {
        this.newLineChar = newLineChar;
    }

    /**
     * Constructs reporter with custom newline char and indent string.
     *
     * @param newLineChar Newline char
     * @param indentStr Indent string
     */
    public LinkReportExporter(String newLineChar, String indentStr) {
        this.newLineChar = newLineChar;
        this.indentStr = indentStr;

    }

    private String indent(int cnt) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            sb.append(indentStr);
        }
        return sb.toString();
    }

    /**
     * Generates report.
     * @param swf SWF file
     * @param as3scripts List of scripts
     * @param evl Event listener
     * @return Report
     * @throws InterruptedException On interrupt
     */
    public String generateReport(SWF swf, List<ScriptPack> as3scripts, EventListener evl) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        Set<String> extDeps = new HashSet<>();
        sb.append("<report>").append(newLineChar);
        sb.append(indent(1)).append("<scripts>").append(newLineChar);
        List<ScriptPack> revList = new ArrayList<>(as3scripts);
        Collections.reverse(revList);

        List<DottedChain> existingObjects = new ArrayList<>();
        for (ScriptPack sp : as3scripts) {
            existingObjects.add(sp.getClassPath().packageStr.add(sp.getClassPath().className, sp.getClassPath().namespaceSuffix));
        }

        for (ScriptPack sp : revList) {
            String scriptName = "script" + sp.scriptIndex;
            sb.append(indent(2)).append("<script name=\"").append(scriptName).append("\">").append(newLineChar);
            //TODO: additional attributes - mod="1469951734131" size="525" optimizedsize="508
            ScriptInfo script = sp.abc.script_info.get(sp.scriptIndex);
            for (int traitIndex : sp.traitIndices) {
                Trait trait = script.traits.traits.get(traitIndex);
                sb.append(reportTrait(sp.scriptIndex, extDeps, existingObjects, swf, sp.abc, trait));
            }
            //how about script_init method(?)
            sb.append(indent(2)).append("</script>").append(newLineChar);
        }
        sb.append(indent(1)).append("</scripts>").append(newLineChar);
        sb.append("</report>").append(newLineChar);
        return sb.toString();
    }

    private String multiNameToId(ABC abc, Multiname multiName) {
        Namespace ns = multiName.getNamespace(abc.constants);
        NamespaceSet nss = multiName.getNamespaceSet(abc.constants);
        if (nss != null && nss.namespaces.length == 1) {
            ns = abc.constants.getNamespace(nss.namespaces[0]);
        }
        String pkgName = ns == null ? "" : ns.getName(abc.constants).toRawString();
        String clsName = multiName.getName(abc.constants, new ArrayList<>(), true, true);
        return pkgName.isEmpty() ? clsName : pkgName + ":" + clsName;
    }

    private String dottedChainToId(DottedChain dc) {
        if (dc.getWithoutLast().isEmpty()) {
            return dc.getLast();
        }
        return dc.getWithoutLast().toRawString() + ":" + dc.getLast();
    }

    private String reportTrait(int scriptIndex, Set<String> externalDefs, List<DottedChain> existingObjects, SWF swf, ABC abc, Trait t) throws InterruptedException {
        //TODO: handle externalDefs - <external-defs> <ext id="..." /> </external-defs>
        StringBuilder sb = new StringBuilder();
        if (t instanceof TraitClass) {
            TraitClass tc = (TraitClass) t;
            sb.append(indent(3)).append("<def id=\"").append(multiNameToId(abc, tc.getName(abc))).append("\" />").append(newLineChar);
            ClassInfo ci = abc.class_info.get(tc.class_info);
            InstanceInfo ii = abc.instance_info.get(tc.class_info);

            Set<String> allDeps = new HashSet<>();

            String superPre;
            if (ii.super_index != 0) {
                superPre = multiNameToId(abc, abc.constants.getMultiname(ii.super_index));
            } else {
                superPre = "Object";
            }
            allDeps.add(superPre);
            sb.append(indent(3)).append("<pre id=\"").append(superPre).append("\" />").append(newLineChar);

            for (int iface : ii.interfaces) {
                String ifacePre = multiNameToId(abc, abc.constants.getMultiname(iface));
                allDeps.add(ifacePre);
                sb.append(indent(3)).append("<pre id=\"").append(ifacePre).append("\" />").append(newLineChar);
            }

            for (Trait ct : ci.static_traits.traits) {
                reportTrait(scriptIndex, externalDefs, existingObjects, swf, abc, ct);
            }
            for (Trait it : ii.instance_traits.traits) {
                reportTrait(scriptIndex, externalDefs, existingObjects, swf, abc, it);
            }
            List<Dependency> dependencies = new ArrayList<>();
            sb.append(indent(3)).append("<dep id=\"AS3\" />").append(newLineChar); //Automatic

            tc.getDependencies(swf.getAbcIndex(), scriptIndex, -1, false, null, abc, dependencies, new DottedChain(new String[]{"FAKE!PACKAGE"}), new ArrayList<>(), new ArrayList<>(), new Reference<>(null));
            for (Dependency dependency : dependencies) {
                DottedChain dc = dependency.getId();
                if (!"*".equals(dc.getLast())) {
                    //some toplevel "imports" can be only method calls
                    if (dc.getWithoutLast().isEmpty() && !existingObjects.contains(dc)) {
                        continue;
                    }
                    String reportDepId = dottedChainToId(dc);
                    if (!allDeps.contains(reportDepId)) {
                        sb.append(indent(3)).append("<dep id=\"").append(reportDepId).append("\" />").append(newLineChar);
                        allDeps.add(reportDepId);
                    }
                }
            }
        }
        return sb.toString();
    }

}
