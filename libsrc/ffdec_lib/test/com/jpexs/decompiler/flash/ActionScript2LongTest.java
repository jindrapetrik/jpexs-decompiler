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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2LongTest extends ActionScript2TestBase {
    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
        Configuration.showAllAddresses.set(false);
        Configuration.pluginPath.set(null);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2_long/as2_long.swf")), false);
    }
    
    @Test
    public void testLongScript() {      
        StringBuilder sb = new StringBuilder();
        StringBuilderTextWriter writer = new StringBuilderTextWriter(new CodeFormatting(), sb);

        DoActionTag doa = getFirstActionTag();
        try {
            Action.actionsToSource(new HashMap<>(),doa,  doa.getActions(), "", writer, "UTF-8");
        } catch (InterruptedException ex) {
            fail();
        }
        
        String result = sb.toString();
        if (result.contains("/*")) {
            fail();
        }
        if (!result.contains("\"9999\"")) {
            fail();
        }
    }
}
