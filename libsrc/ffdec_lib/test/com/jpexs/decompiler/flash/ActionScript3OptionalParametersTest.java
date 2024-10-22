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
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.DottedChain;
import java.io.IOException;
import java.util.ArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3OptionalParametersTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("standard", "testdata/as3_new/bin/as3_new.flex.swf");
    }

    @Test
    public void testOptionalParameters() {
        String methodName = "testOptionalParameters";
        String className = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

        int clsIndex = -1;
        DoABC2Tag tag = null;
        ABC abc = null;
        for (Tag t : getSwf("standard").getTags()) {
            if (t instanceof DoABC2Tag) {
                tag = (DoABC2Tag) t;
                abc = tag.getABC();
                clsIndex = abc.findClassByName(new DottedChain(new String[]{"tests", className}));
                if (clsIndex > -1) {
                    break;
                }
            }
        }
        assertTrue(clsIndex > -1);

        int methodInfo = abc.findMethodInfoByName(clsIndex, "run");
        int bodyIndex = abc.findMethodBodyByName(clsIndex, "run");
        assertTrue(methodInfo > -1);
        assertTrue(bodyIndex > -1);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        abc.method_info.get(methodInfo).getParamStr(writer, abc.constants, abc.bodies.get(bodyIndex), abc, new ArrayList<>());
        writer.finishHilights();
        String actualResult = writer.toString().replaceAll("[ \r\n]", "");
        String expectedResult = "p1:Event=null,p2:Number=1,p3:Number=-1,p4:Number=-1.1,p5:Number=-1.1,p6:String=\"a\"";
        expectedResult = expectedResult.replaceAll("[ \r\n]", "");
        assertEquals(actualResult, expectedResult);
    }
}
