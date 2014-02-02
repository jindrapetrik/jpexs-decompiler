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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.graph.ExportMode;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2AssemblerTest extends ActionStript2TestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        Configuration.autoDeobfuscate.set(false);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
    }

    @Test
    public void testModifiedConstantPools() {
        String actionsString = "ConstantPool \"ok\"\n"
                + "Jump loc001f\n"
                + "loc000d:Push \"ok\" false\n"
                + "SetVariable\n"
                + "Jump loc002f\n"
                + "loc001f:ConstantPool \"wrong\"\n"
                + "Jump loc000d\n"
                + "loc002f:";
        try {
            List<Action> actions = ASMParser.parse(0, 0, true, actionsString, swf.version, false);

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HilightedTextWriter writer = new HilightedTextWriter(false);
            Action.actionsToSource(doa, doa.getActions(swf.version), swf.version, "", writer);
            String actualResult = writer.toString();
            writer = new HilightedTextWriter(false);
            doa.getASMSource(swf.version, ExportMode.PCODE, writer, null);
            String decompiled = writer.toString();

            assertEquals(actualResult.trim(), "ok = false;");
            assertTrue(decompiled.contains("Push \"ok\" false"));
        } catch (IOException | ParseException | InterruptedException ex) {
            fail();
        }
    }
}
