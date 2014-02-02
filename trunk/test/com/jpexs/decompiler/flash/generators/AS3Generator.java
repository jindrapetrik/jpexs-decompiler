/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.generators;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoABCDefineTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * Generates stub for ActionScript3Test
 *
 * @author JPEXS
 */
public class AS3Generator {

    public static void main(String[] args) throws Exception {
        Configuration.autoDeobfuscate.set(false);
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3/as3.swf")),false);
        DoABCDefineTag tag = null;
        for (Tag t : swf.tags) {
            if (t instanceof DoABCDefineTag) {
                tag = (DoABCDefineTag) t;
                break;
            }
        }
        ABC abc = tag.getABC();
        int classId = abc.findClassByName("classes.Test");
        StringBuilder s = new StringBuilder();
        for (Trait t : abc.instance_info[classId].instance_traits.traits) {
            if (t instanceof TraitMethodGetterSetter) {
                String name = t.getName(abc).getName(abc.constants, new ArrayList<String>());
                if (name.startsWith("test")) {
                    s.append("@Test\r\npublic void ");
                    s.append(name);
                    s.append("(){\r\ndecompileMethod(\"");
                    s.append(name);
                    s.append("\", ");
                    HilightedTextWriter src = new HilightedTextWriter(false);
                    abc.findBody(((TraitMethodGetterSetter) t).method_info).toString("", ExportMode.SOURCE, false, -1/*FIX?*/, classId, abc, null,abc.constants, abc.method_info, new Stack<GraphTargetItem>(), false, src, new ArrayList<String>(), abc.instance_info[classId].instance_traits);
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
