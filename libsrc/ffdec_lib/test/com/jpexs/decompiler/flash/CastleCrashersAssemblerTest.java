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
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.profiles.CastleCrashersSwfProfile;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CastleCrashersAssemblerTest {

    private static final String FUNCTION = "DefineFunction2 \"myf\", 0, 1, false, false, true, false, true, false, true, false, false {\n";

    private ActionList parse(String source) throws Exception {
        return ASMParser.parse(0, true, source, 6, true, "UTF-8", new CastleCrashersSwfProfile());
    }

    private ActionList roundTrip(ActionList actions) throws Exception {
        // Minimal version 6 SWF, so the Castle Crashers profile is active.
        byte[] swfBytes = {70, 87, 83, 6, 16, 0, 0, 0, 8, 0, 0, 12, 1, 0, 0, 0};
        try (ByteArrayInputStream input = new ByteArrayInputStream(swfBytes)) {
            SWF swf = new SWF(input, false, "UTF-8", new CastleCrashersSwfProfile());
            DoActionTag tag = new DoActionTag(swf);
            tag.setActions(actions);
            byte[] bytes = tag.getActionBytes().getRangeData();
            ActionList result = ActionListReader.readActionList(tag, new ArrayList<>(),
                    new SWFInputStream(swf, bytes), 6, 0, bytes.length, "", 0);
            assertEquals(Action.actionsToBytes(result, true, 6), Action.actionsToBytes(actions, true, 6));
            return result;
        }
    }

    @Test
    public void testPushSetMemberFunctionRoundTrip() throws Exception {
        ActionList actions = parse("ConstantPool \"A\"\n" + FUNCTION
                + "Push \"A\"\nTrace\nPush \"_root\"\nGetVariable\n"
                + "CC_PushSetMember \"b_do_init\", true\n}\n");
        assertEquals(((ActionDefineFunction2) actions.get(1)).codeSize, 33);
        ActionList result = roundTrip(actions);
        for (Action action : result) {
            assertFalse(action instanceof ActionJump, "Unexpected jump after reloading the function");
        }
    }

    @DataProvider(name = "instructions")
    public Object[][] instructions() {
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[]{"CC_PushGetVariable \"_root\""});
        result.add(new Object[]{"CC_PushGetVariable \"žluťoučký\""});
        for (String value : new String[]{"\"žluťoučký\"", "null", "undefined", "register1", "true", "false", "1", "1.5", "1.5f", "constant255", "constant256"}) {
            result.add(new Object[]{"CC_PushGetMember " + value});
            result.add(new Object[]{"CC_PushSetMember \"b_do_init\", " + value});
            result.add(new Object[]{"CC_PushGetVariable2 " + value + ", \"_root\""});
            result.add(new Object[]{"CC_PushDefineLocal \"žluťoučký\", " + value});
            result.add(new Object[]{"Push " + value});
        }
        result.add(new Object[]{"CC_PushSetMember constant256, constant255"});
        return result.toArray(new Object[0][]);
    }

    @Test(dataProvider = "instructions")
    public void testInstructionLengths(String instruction) throws Exception {
        Action action = parse(instruction + "\n").get(0);
        assertEquals(action.getTotalActionLength(), action.getBytes(6).length, instruction);
        assertEquals(action.getBytesLength(), action.getBytes(6).length, instruction);
    }

    @Test(dataProvider = "instructions")
    public void testBranchesAndContainersRoundTrip(String instruction) throws Exception {
        ActionList actions = parse(FUNCTION
                + "Push true\nIf end\n" + instruction + "\nend:\nPush \"done\"\nTrace\n}\n"
                + "Push true\nIf after\nPush \"skip\"\nTrace\nJump after\nafter:\nPush \"after\"\nTrace\n");
        roundTrip(actions);
    }
}
