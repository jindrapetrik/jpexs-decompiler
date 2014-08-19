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
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
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
public class ActionScript2ModificationTest extends ActionStript2TestBase {
    
    @BeforeClass
    public void init() throws IOException, InterruptedException {
        Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
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
            }
            else {
                break;
            }
        }
        return actions;
    }
    
    public void testRemoveAction(String actionsString, String expectedResult, int[] actionsToRemove) {
        try {
            ActionList actions = ASMParser.parse(0, true, actionsString, swf.version, false);

            for (int i : actionsToRemove) {
                actions.removeAction(i);
            }
            
            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HilightedTextWriter writer = new HilightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, doa.getActions());
            String actualResult = normalizeLabels(writer.toString());

            actualResult = actualResult.replaceAll("[ \r\n]", "");
            expectedResult = expectedResult.replaceAll("[ \r\n]", "");
            
            Assert.assertEquals(actualResult, expectedResult);
        } catch (IOException | ParseException | InterruptedException ex) {
            fail();
        }
    }
    
    @Test
    public void testRemoveJumpAction() {
        String actionsString = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "Return\n" +
                "}\n" +
                "Push 2\n" + 
                "Jump label_1\n" + // remove this action
                "label_1:Push 3";
        String expectedResult = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "Return\n" +
                "}\n" +
                "Push 2 3";
        testRemoveAction(actionsString, expectedResult, new int[] {5});
    }

    @Test
    public void testRemoveActionFromContainer() {
        String actionsString = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" + // remove this action
                "Return\n" +
                "}\n" +
                "Push 2\n" + 
                "Jump label_1\n" +
                "label_1:Push 3";
        String expectedResult = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Return\n" +
                "}\n" +
                "Push 2\n" + 
                "Jump label_1\n" +
                "label_1:Push 3";
        testRemoveAction(actionsString, expectedResult, new int[] {2});
    }

    @Test
    public void testRemoveLastActionFromContainer() {
        String actionsString = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "GetVariable\n" + // remove this action
                "}\n" +
                "Push 2\n" + 
                "Jump label_1\n" +
                "label_1:Push 3";
        String expectedResult = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "}\n" +
                "Push 2\n" + 
                "Jump label_1\n" +
                "label_1:Push 3";
        testRemoveAction(actionsString, expectedResult, new int[] {3});
    }

    @Test
    public void testRemoveIfTargetAction() {
        String actionsString = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "GetVariable\n" +
                "}\n" +
                "Push 2\n" + 
                "If label_1\n" +
                "Push 3\n" +
                "label_1:Push 4\n" + // remove this action
                "Push 5"; // after removing the previous action the if action should jump here
        String expectedResult = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "GetVariable\n" +
                "}\n" +
                "Push 2\n" + 
                "If label_1\n" +
                "Push 3\n" +
                "label_1:Push 5";
        testRemoveAction(actionsString, expectedResult, new int[] {7});
    }

    @Test
    public void testRemoveIfTargetLastAction() {
        String actionsString = 
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "GetVariable\n" +
                "}\n" +
                "Push 2\n" + 
                "If label_1\n" +
                "Push 3\n" +
                "label_1:Push 4"; // remove this action
        String expectedResult =
                "ConstantPool\n" + 
                "DefineFunction \"test\" 1 \"p1\" {\n" +
                "Push 1\n" +
                "GetVariable\n" +
                "}\n" +
                "Push 2\n" + 
                "If label_1\n" +
                "Push 3\n" +
                "label_1:";
        testRemoveAction(actionsString, expectedResult, new int[] {7});
    }
}
