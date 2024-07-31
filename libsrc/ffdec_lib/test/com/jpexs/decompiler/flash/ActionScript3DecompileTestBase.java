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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author JPEXS
 */
public abstract class ActionScript3DecompileTestBase extends ActionScriptTestBase {

    private final Map<String, SWF> swfMap = new HashMap<>();

    @BeforeClass
    public void initConfiguration() throws IOException, InterruptedException {
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);

        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        Configuration.showMethodBodyId.set(false);
        Configuration.displayDupInstructions.set(true);
    }

    protected void addSwf(String identifier, String path) throws FileNotFoundException, IOException, InterruptedException {
        swfMap.put(identifier, new SWF(new BufferedInputStream(new FileInputStream(path)), false));
    }

    public SWF getSwf(String identifier) {
        return swfMap.get(identifier);
    }

    protected void decompileMethod(String swfIdentifier, String methodName, String expectedResult, boolean isStatic) {
        String className = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

        int clsIndex = -1;
        int scriptIndex = -1;

        ABC abc = null;
        SWF swf = getSwf(swfIdentifier);
        List<ABC> abcs = new ArrayList<>();
        for (ABCContainerTag abcTag : swf.getAbcList()) {
            abcs.add(abcTag.getABC());
        }
        ScriptPack scriptPack = null;
        for (ABC a : abcs) {
            scriptPack = a.findScriptPackByPath("tests." + className, abcs);
            if (scriptPack != null) {
                break;
            }
        }
        assertNotNull(scriptPack);
        abc = scriptPack.abc;
        scriptIndex = scriptPack.scriptIndex;

        clsIndex = abc.findClassByName(new DottedChain(new String[]{"tests", className}));

        assertTrue(clsIndex > -1);
        assertTrue(scriptIndex > -1);

        int bodyIndex = abc.findMethodBodyByName(clsIndex, "run");

        assertTrue(bodyIndex > -1);
        HighlightedTextWriter writer;
        try {
            Configuration.autoDeobfuscate.set(methodName.toLowerCase().contains("obfus"));

            List<MethodBody> callStack = new ArrayList<>();
            callStack.add(abc.bodies.get(bodyIndex));
            abc.bodies.get(bodyIndex).convert(callStack, swf.getAbcIndex(), new ConvertData(), "run", ScriptExportMode.AS, isStatic, abc.bodies.get(bodyIndex).method_info, scriptIndex, clsIndex, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), abc.instance_info.get(clsIndex).instance_traits, true, new HashSet<>());
            writer = new HighlightedTextWriter(new CodeFormatting(), false);
            abc.bodies.get(bodyIndex).toString(callStack, swf.getAbcIndex(), "run", ScriptExportMode.AS, abc, null, writer, new ArrayList<>(), new HashSet<>());
        } catch (InterruptedException ex) {
            fail();
            return;
        }
        Configuration.autoDeobfuscate.set(false);
        writer.finishHilights();
        String actualResult = cleanPCode(writer.toString());
        expectedResult = cleanPCode(expectedResult);
        assertEquals(actualResult, expectedResult);
    }

}
