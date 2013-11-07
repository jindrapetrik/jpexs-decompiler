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
        SWF swf = new SWF(new FileInputStream("testdata/as3/as3.swf"),false);
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
                    HilightedTextWriter src = abc.findBody(((TraitMethodGetterSetter) t).method_info).toString("", ExportMode.SOURCE, false, -1/*FIX?*/, classId, abc, null,abc.constants, abc.method_info, new Stack<GraphTargetItem>(), false, new ArrayList<String>(), abc.instance_info[classId].instance_traits);
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
