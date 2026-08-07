/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.as3decompile;

import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.IOException;
import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constructor assignments that read activation slots (named params/locals)
 * must stay in the constructor and must not become field initializers.
 */
public class ActionScript3CtorFieldInitTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("ctorfield", "testdata/as3_ctor_field_init/bin/as3_ctor_field_init.swf");
    }

    private String decompile(String classPath) {
        Configuration.decompilationTimeoutFile.set(5 * 60);
        Configuration.decompilationTimeoutSingleMethod.set(60);

        ScriptPack scriptPack = null;
        ABC abc = null;
        SWF swf = getSwf("ctorfield");
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                abc = ((DoABC2Tag) t).getABC();
                scriptPack = abc.findScriptPackByPath(classPath, Arrays.asList(abc));
                if (scriptPack != null) {
                    break;
                }
            }
        }
        assertNotNull(scriptPack);

        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        try {
            scriptPack.toSource(swf.getAbcIndex(), writer, abc.script_info.get(scriptPack.scriptIndex).traits.traits, new ConvertData(), ScriptExportMode.AS, false, false, false);
        } catch (InterruptedException ex) {
            fail(ex.getMessage());
        }
        writer.finishHilights();
        return cleanPCode(writer.toString());
    }

    @Test
    public void testActivationSlotAssignsStayInConstructor() {
        String actual = decompile("tests_classes.TestCtorActivationFieldInit");

        String expected = "package tests_classes\n"
                + "{\n"
                + "   public class TestCtorActivationFieldInit\n"
                + "   {\n"
                + "       \n"
                + "      protected var mValue:Object;\n"
                + "       \n"
                + "      protected var mFlag:Boolean;\n"
                + "       \n"
                + "      protected var mInverted:Boolean;\n"
                + "       \n"
                + "      private var mLiteral:int = 7;\n"
                + "       \n"
                + "      public function TestCtorActivationFieldInit(value:Object, flag:Boolean = false)\n"
                + "      {\n"
                + "         mValue = value;\n"
                + "         mFlag = flag;\n"
                + "         mInverted = !flag;\n"
                + "         super();\n"
                + "         run(function():Object\n"
                + "         {\n"
                + "            return value;\n"
                + "         });\n"
                + "      }\n"
                + "       \n"
                + "      private function run(f:Function) : void\n"
                + "      {\n"
                + "         f();\n"
                + "      }\n"
                + "   }\n"
                + "}";

        assertEquals(actual, cleanPCode(expected));
        assertFalse(actual.contains("mValue:Object = value"));
        assertFalse(actual.contains("mFlag:Boolean = flag"));
        assertFalse(actual.contains("mInverted:Boolean = !flag"));
        assertTrue(actual.contains("mLiteral:int = 7"));
    }

    @Test
    public void testLocalRegAssignsStayInConstructor() {
        String actual = decompile("tests_classes.TestCtorFieldInit");

        assertFalse(actual.contains("mId:int = id"));
        assertFalse(actual.contains("mFlag:Boolean = flag"));
        assertFalse(actual.contains("mInverted:Boolean = !flag"));
        assertFalse(actual.contains("mName:String = name"));
        assertTrue(actual.contains("mLiteral:int = 42"));
        assertTrue(actual.contains("mId = id"));
        assertTrue(actual.contains("mInverted = !flag"));
    }
}
