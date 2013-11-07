/*
 * Copyright (C) 2013 JPEXS
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2DeobfuscatorTest extends ActionStript2TestBase {

    @BeforeClass
    public void init() throws IOException {
        Configuration.autoDeobfuscate.set(true);
        swf = new SWF(new FileInputStream("testdata/as2/as2.swf"), false);
    }

    @Test
    public void testRemoveJumpsToTheNextAction() {
        String actionsString = "ConstantPool \"a\" \"b\" \"c\"\n" +
            "Push false register1\n" +
            "StoreRegister 2\n" +
            "Pop\n" +
            "Push register2\n" +
            "StoreRegister 0\n" +
            "Push \"a\"\n" +
            "StrictEquals\n" +
            "If loc005a\n" +
            "Push register0 \"b\"\n" +
            "StrictEquals\n" +
            "If loc0068\n" +
            "Jump loc0048;\n" +
            "loc0048:Push register0 \"c\"\n" +
            "StrictEquals\n" +
            "If loc0076\n" +
            "Jump loc0084\n" +
            "loc005a:Push 1\n" +
            "Trace\n" +
            "Jump loc0084\n" +
            "loc0068:Push 2\n" +
            "Trace\n" +
            "Jump loc0084\n" +
            "loc0076:Push 3\n" +
            "Trace\n" +
            "Jump loc0084\n" +
            "loc0084:";
        try {
            List<Action> actions = ASMParser.parse(0, 0, true, actionsString, swf.version, false);

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HilightedTextWriter writer = new HilightedTextWriter(false);
            Action.actionsToSource(doa, doa.getActions(swf.version), swf.version, "", writer);
            String actualResult = writer.toString();

            assertTrue(actualResult.contains("case \"c\":"));
        } catch (IOException | ParseException | InterruptedException ex) {
            fail();
        }
    }
}
