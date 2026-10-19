/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License, version 3 or later.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.special.ActionUnknown;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.ArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ActionUnknownTest {

    private ActionList read(String hex) throws Exception {
        SWF swf = new SWF();
        swf.version = 4;
        try (SWFInputStream input = new SWFInputStream(swf, new ByteArrayRange(hex).getRangeData())) {
            return ActionListReader.readActionList(null, new ArrayList<>(), input, swf.version, 0, -1, "", 0);
        }
    }

    private String pcode(ActionList actions) {
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToString(new ArrayList<>(), 0, actions, 4, ScriptExportMode.PCODE, writer);
        return writer.toString().replace("\r\n", "\n").trim();
    }

    private ActionList parse(String source) throws Exception {
        return ASMParser.parse(0, true, source, 4, true, Utf8Helper.charsetName, null);
    }

    @Test
    public void testUnknownPayloadRoundTrip() throws Exception {
        String hex = "96030000410026a50300abcdef9603000042002600";
        ActionList actions = read(hex);
        assertTrue(actions.get(2) instanceof ActionUnknown);
        assertEquals(actions.get(3).getAddress(), 13L);
        assertEquals(pcode(actions), "Push \"A\"\nTrace\nUnknown_a5 0xab, 0xcd, 0xef\nPush \"B\"\nTrace");
        byte[] expected = new ByteArrayRange(hex).getRangeData();
        assertEquals(Action.actionsToBytes(actions, true, 4), expected);
        ActionList assembled = parse(pcode(actions));
        assertEquals(assembled.get(3).getAddress(), 13L);
        assertEquals(Action.actionsToBytes(assembled, true, 4), expected);
    }

    @DataProvider(name = "emptyActions")
    public Object[][] emptyActions() {
        return new Object[][]{
            {"a500002600", "Unknown_a5\nTrace", 3L},
            {"7f2600", "Unknown_7f\nTrace", 1L}
        };
    }

    @Test(dataProvider = "emptyActions")
    public void testUnknownWithoutPayload(String hex, String source, long nextAddress) throws Exception {
        ActionList actions = read(hex);
        assertEquals(pcode(actions), source);
        assertEquals(actions.get(1).getAddress(), nextAddress);
        assertEquals(Action.actionsToBytes(parse(source), true, 4), new ByteArrayRange(hex).getRangeData());
    }

    @Test
    public void testJumpAcrossUnknownPayload() throws Exception {
        String hex = "a50300abcdef96020005019d02000500990200ebff2600";
        ActionList actions = read(hex);
        assertEquals(((ActionIf) actions.get(2)).getTargetAddress(), 21L);
        assertEquals(((ActionJump) actions.get(3)).getTargetAddress(), 0L);
        ActionList assembled = parse(pcode(actions));
        assertEquals(((ActionIf) assembled.get(2)).getTargetAddress(), 21L);
        assertEquals(((ActionJump) assembled.get(3)).getTargetAddress(), 0L);
        assertEquals(Action.actionsToBytes(assembled, true, 4), new ByteArrayRange(hex).getRangeData());
    }

    @DataProvider(name = "invalidData")
    public Object[][] invalidData() {
        return new Object[][]{
            {"Unknown_a5 0x100"},
            {"Unknown_a5 -1"},
            {"Unknown_a5 0xab,"},
            {"Unknown_7f 0xab"}
        };
    }

    @Test(dataProvider = "invalidData", expectedExceptions = ActionParseException.class)
    public void testInvalidUnknownData(String source) throws Exception {
        parse(source);
    }
}
