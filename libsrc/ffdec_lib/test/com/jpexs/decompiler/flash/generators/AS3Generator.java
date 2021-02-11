/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.generators;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
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
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * Generates stub for ActionScript3Test
 *
 * @author JPEXS
 */
public class AS3Generator {

    private static void useFile(String testClassName, String[][] swfAndIdentifierList, boolean multipleProviders) throws FileNotFoundException, IOException, InterruptedException {
        StringBuilder s = new StringBuilder();
        File f = new File(swfAndIdentifierList[0][0]);
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream(f)), false);
        DoABC2Tag tag = null;
        List<ScriptPack> scriptPacks = swf.getAS3Packs();
        Map<String, ScriptPack> sortedPacks = new TreeMap<>();
        for (ScriptPack pack : scriptPacks) {
            sortedPacks.put(pack.getClassPath().toRawString(), pack);
        }
        s.append("package com.jpexs.decompiler.flash.as3decompile;\r\n");
        s.append("\r\n");
        s.append("import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;\r\n");
        s.append("import java.io.IOException;\r\n");
        s.append("import org.testng.annotations.BeforeClass;\r\n");
        if (multipleProviders) {
            s.append("import org.testng.annotations.DataProvider;\r\n");
        }
        s.append("import org.testng.annotations.Test;\r\n");

        s.append("/**\r\n");
        s.append(" *\r\n");
        s.append(" * @author JPEXS\r\n");
        s.append(" */\r\n");
        s.append("public class ").append(testClassName).append(" extends ActionScript3DecompileTestBase {\r\n");

        s.append("@BeforeClass\r\n");
        s.append("public void init() throws IOException, InterruptedException {\r\n");
        for (int i = 0; i < swfAndIdentifierList.length; i++) {
            s.append("addSwf(\"").append(swfAndIdentifierList[i][1]).append("\", \"").append(swfAndIdentifierList[i][0].replace("\\", "\\\\")).append("\");\r\n");
        }
        s.append("}\r\n");

        if (multipleProviders) {
            s.append("@DataProvider\r\n");
            s.append("public Object[][] swfNamesProvider() {\r\n");
            s.append("return new Object[][]{\r\n");

            for (int i = 0; i < swfAndIdentifierList.length; i++) {
                s.append("{\"");
                s.append(swfAndIdentifierList[i][1]);
                s.append("\"}");
                if (i < swfAndIdentifierList.length - 1) {
                    s.append(",");
                }
                s.append("\r\n");
            }
            s.append("};\r\n");
            s.append("}\r\n");
        }

        for (String packClassName : sortedPacks.keySet()) {
            ScriptPack pack = sortedPacks.get(packClassName);
            ABC abc = pack.abc;
            if (pack.getClassPath().packageStr.toRawString().equals("tests")) {
                abc.findClassByName(pack.getClassPath().toRawString());

                int classId = abc.findClassByName(pack.getClassPath().toRawString());

                for (Trait t : abc.instance_info.get(classId).instance_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        String name = t.getName(abc).getName(abc.constants, null, true, true);
                        String clsName = pack.getClassPath().className;
                        String lower = clsName.substring(0, 1).toLowerCase() + clsName.substring(1);
                        String identifier = swfAndIdentifierList[0][1];
                        String testMethodName = lower; //lower.replaceAll("^test", "test" + idUpper);
                        if (lower.equals("testOptionalParameters")) { //SPECIAL: ignored
                            continue;
                        }
                        if (name.equals("run")) {
                            if (multipleProviders) {
                                s.append("@Test(dataProvider = \"swfNamesProvider\")\r\n");
                            } else {
                                s.append("@Test\r\n");
                            }
                            s.append("public void ");
                            s.append(testMethodName);
                            if (multipleProviders) {
                                s.append("(String swfUsed){\r\ndecompileMethod(swfUsed");
                            } else {
                                s.append("(){\r\ndecompileMethod(\"");
                                s.append(identifier);
                                s.append("\"");
                            }
                            s.append(",\"");
                            s.append(lower);
                            s.append("\", ");
                            HighlightedTextWriter src = new HighlightedTextWriter(new CodeFormatting(), false);
                            MethodBody b = abc.findBody(((TraitMethodGetterSetter) t).method_info);
                            List<Traits> ts = new ArrayList<>();
                            ts.add(abc.instance_info.get(classId).instance_traits);
                            
                            Configuration.autoDeobfuscate.set(clsName.toLowerCase().contains("obfus"));
                            
                            b.convert(new ConvertData(), "", ScriptExportMode.AS, false, ((TraitMethodGetterSetter) t).method_info, pack.scriptIndex, classId, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), ts, true, new HashSet<>());
                            b.toString("", ScriptExportMode.AS, abc, null, src, new ArrayList<>(), new HashSet<>());
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
                }
            }
        }

        s.append("}\r\n");
        String testPath = "test/com/jpexs/decompiler/flash/as3decompile/";
        Helper.writeFile(testPath + testClassName + ".java", s.toString().getBytes("UTF-8"));
    }

    public static void main(String[] args) throws Exception {
        Configuration.autoDeobfuscate.set(false);
        Configuration.showMethodBodyId.set(false);
        Configuration.simplifyExpressions.set(false);


        useFile("ActionScript3ClassicDecompileTest", new String[][]{{"testdata/as3_new/bin/as3_new.flex.swf", "classic"}}, false);
        useFile("ActionScript3ClassicAirDecompileTest", new String[][]{{"testdata/as3_new/bin/as3_new.air.swf", "classic_air"}}, false);

        useFile("ActionScript3CrossCompileDecompileTest", new String[][]{
            {"testdata/as3_cross_compile/bin/as3_cross_compile.flex.swf", "flex"},
            {"testdata/as3_cross_compile/bin/as3_cross_compile.air.swf", "air"}
        }, true);
        useFile("ActionScript3CrossCompileSwfToolsDecompileTest", new String[][]{
            {"testdata/as3_cross_compile/bin/as3_cross_compile.swftools.swf", "swftools"},}, false);
        useFile("ActionScript3AssembledDecompileTest", new String[][]{{"testdata/as3_assembled/bin/as3_assembled.swf", "assembled"}}, false);

        System.exit(0);
    }
}
