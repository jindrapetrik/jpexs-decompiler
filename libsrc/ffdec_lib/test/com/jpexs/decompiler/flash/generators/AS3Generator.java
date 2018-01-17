/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.generators;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.ScopeStack;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Generates stub for ActionScript3Test
 *
 * @author JPEXS
 */
public class AS3Generator {

    public static void main(String[] args) throws Exception {
        Configuration.autoDeobfuscate.set(false);
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3/as3.swf")), false);
        DoABC2Tag tag = null;
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                tag = (DoABC2Tag) t;
                break;
            }
        }
        if (tag == null) {
            System.err.println("No ABC found");
            return;
        }
        ABC abc = tag.getABC();
        int classId = abc.findClassByName("classes.Test");
        StringBuilder s = new StringBuilder();
        for (Trait t : abc.instance_info.get(classId).instance_traits.traits) {
            if (t instanceof TraitMethodGetterSetter) {
                String name = t.getName(abc).getName(abc.constants, null, true, true);
                if (name.startsWith("test")) {
                    s.append("@Test\r\npublic void ");
                    s.append(name);
                    s.append("(){\r\ndecompileMethod(\"");
                    s.append(name);
                    s.append("\", ");
                    HighlightedTextWriter src = new HighlightedTextWriter(new CodeFormatting(), false);
                    MethodBody b = abc.findBody(((TraitMethodGetterSetter) t).method_info);
                    List<Traits> ts = new ArrayList<>();
                    ts.add(abc.instance_info.get(classId).instance_traits);
                    b.convert(new ConvertData(), "", ScriptExportMode.AS, false, -1/*FIX?*/, -1/*FIX?*/, classId, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), ts, true);
                    b.toString("", ScriptExportMode.AS, abc, null, src, new ArrayList<>());
                    String[] srcs = src.toString().split("[\r\n]+");
                    for (int i = 0; i < srcs.length; i++) {
                        String ss = srcs[i];
                        s.append("\"");
                        s.append(ss.trim().replace("\\", "\\\\").replace("\"", "\\\""));
                        s.append("\\r\\n\"");
                        if (i < srcs.length - 1) {
                            s.append("+");
                        }
                        s.append("\r\n");
                    }
                    s.append(", false);");
                    s.append("}");
                }
            }
            try (PrintWriter pw = new PrintWriter("as3_teststub.java")) {
                pw.println(s.toString());
            }
        }
    }
}
