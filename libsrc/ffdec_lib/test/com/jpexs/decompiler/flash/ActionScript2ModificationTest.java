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

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionList;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.Assert;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2ModificationTest extends ActionScript2TestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
        Configuration.showAllAddresses.set(false);
        Configuration.pluginPath.set(null);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
    }

    private String normalizeLabels(String actions) {
        int labelCnt = 1;
        while (true) {
            Pattern pattern = Pattern.compile("^([a-z][0-9a-z]+):", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(actions);
            if (matcher.find()) {
                String str = matcher.group(1);
                actions = actions.replaceAll(str, "label_" + labelCnt++);
            } else {
                break;
            }
        }
        return actions;
    }

    public void testRemoveActionNormal(String actionsString, String expectedResult, int[] actionsToRemove) {
        try {
            ActionList actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset());

            for (int i : actionsToRemove) {
                actions.removeAction(i);
            }

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            writer.finishHilights();
            String actualResult = normalizeLabels(writer.toString());

            actualResult = cleanPCode(actualResult);
            expectedResult = cleanPCode(expectedResult);

            Assert.assertEquals(actualResult, expectedResult);
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    public void testRemoveActionFast(String actionsString, String expectedResult, int[] actionsToRemove) {
        try {
            ActionList actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset());
            FastActionList fastActions = new FastActionList(actions);

            for (int i : actionsToRemove) {
                fastActions.removeItem(i, 1);
            }

            actions = fastActions.toActionList();

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            writer.finishHilights();
            String actualResult = normalizeLabels(writer.toString());

            actualResult = cleanPCode(actualResult);
            expectedResult = cleanPCode(expectedResult);

            Assert.assertEquals(actualResult, expectedResult);
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    public void testRemoveAction(String actionsString, String expectedResult, int[] actionsToRemove) {
        testRemoveActionNormal(actionsString, expectedResult, actionsToRemove);
        testRemoveActionFast(actionsString, expectedResult, actionsToRemove);
    }

    public void testAddActionNormal(String actionsString, String expectedResult, Action action, int index) {
        try {
            ActionList actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset());

            actions.addAction(index, action);

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            writer.finishHilights();
            String actualResult = normalizeLabels(writer.toString());

            actualResult = cleanPCode(actualResult);
            expectedResult = cleanPCode(expectedResult);

            Assert.assertEquals(actualResult, expectedResult);
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    public void testAddActionFast(String actionsString, String expectedResult, Action action, int index) {
        try {
            ActionList actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset());
            FastActionList fastActions = new FastActionList(actions);

            fastActions.insertItemBefore(fastActions.get(index), action);
            actions = fastActions.toActionList();

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            writer.finishHilights();
            String actualResult = normalizeLabels(writer.toString());

            actualResult = cleanPCode(actualResult);
            expectedResult = cleanPCode(expectedResult);

            Assert.assertEquals(actualResult, expectedResult);
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testRemoveJumpAction() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "Return\n"
                + "}\n"
                + "Push 2\n"
                + "Jump label_1\n"
                + // remove this action
                "label_1:Push 3";
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "Return\n"
                + "}\n"
                + "Push 2, 3";
        testRemoveAction(actionsString, expectedResult, new int[]{5});
    }

    @Test
    public void testRemoveActionFromContainer() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n" // remove this action
                + "Return\n"
                + "}\n"
                + "Push 2\n"
                + "Jump label_1\n"
                + "label_1:Push 3";
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Return\n"
                + "}\n"
                + "Push 2\n"
                + "Jump label_1\n"
                + "label_1:Push 3";
        testRemoveAction(actionsString, expectedResult, new int[]{2});
    }

    @Test
    public void testRemoveLastActionFromContainer() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n" // remove this action
                + "}\n"
                + "Push 2\n"
                + "Jump label_1\n"
                + "label_1:Push 3";
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "}\n"
                + "Push 2\n"
                + "Jump label_1\n"
                + "label_1:Push 3";
        testRemoveAction(actionsString, expectedResult, new int[]{3});
    }

    @Test
    public void testRemoveIfTargetAction() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4\n" // remove this action
                + "Push 5"; // after removing the previous action the if action should jump here
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 5";
        testRemoveAction(actionsString, expectedResult, new int[]{7});
    }

    @Test
    public void testRemoveIfTargetLastAction() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4"; // remove this action
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:";
        testRemoveAction(actionsString, expectedResult, new int[]{7});
    }

    @Test
    public void testAddActionFirst() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        String expectedResult
                = "GetMember\n"
                + "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        testAddActionNormal(actionsString, expectedResult, new ActionGetMember(), 0);
        testAddActionFast(actionsString, expectedResult, new ActionGetMember(), 0);
    }

    @Test
    public void testAddAction1() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        String expectedResult
                = "ConstantPool\n"
                + "GetMember\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        testAddActionNormal(actionsString, expectedResult, new ActionGetMember(), 1);
        testAddActionFast(actionsString, expectedResult, new ActionGetMember(), 1);
    }

    @Test
    public void testAddActionToContainer() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "GetMember\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        testAddActionNormal(actionsString, expectedResult, new ActionGetMember(), 2);
        testAddActionFast(actionsString, expectedResult, new ActionGetMember(), 2);
    }

    @Test
    public void testAddActionIf() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "GetMember\n"
                + "label_1:Push 4";
        testAddActionNormal(actionsString, expectedResult, new ActionGetMember(), 7);
        testAddActionFast(actionsString, expectedResult, new ActionGetMember(), 7);
    }

    @Test
    public void testAddActionAfterContainer() {
        String actionsString
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        String expectedResult
                = "ConstantPool\n"
                + "DefineFunction \"test\", 1, \"p1\" {\n"
                + "Push 1\n"
                + "GetVariable\n"
                + "}\n"
                + "GetMember\n"
                + "Push 2\n"
                + "If label_1\n"
                + "Push 3\n"
                + "label_1:Push 4";
        testAddActionNormal(actionsString, expectedResult, new ActionGetMember(), 4);
        testAddActionFast(actionsString, expectedResult, new ActionGetMember(), 4);
    }

    @Test
    public void testAddToJumpTarget() {
        String actionsString
                = "ConstantPool\n"
                + "If label_1\n"
                + "GetMember\n"
                + "label_1:Jump label_2\n" // address 9
                + "label_2:Jump label_3\n"
                + "label_3:Jump labelend\n"
                + "labelend:End"; // address 24
        String expectedResult
                = "ConstantPool\n"
                + "If label_1\n"
                + "GetMember\n"
                + "Jump label_4\n"
                + "label_1:Jump label_2\n"
                + "label_2:Jump label_3\n"
                + "label_3:Jump label_4\n"
                + "label_4:";
        ActionJump jump = new ActionJump(0, Utf8Helper.charsetName);
        jump.setAddress(9);
        jump.setJumpOffset(24 - 9 - 5);
        testAddActionNormal(actionsString, expectedResult, jump, 3);
        testAddActionFast(actionsString, expectedResult, jump, 3);
    }
}
