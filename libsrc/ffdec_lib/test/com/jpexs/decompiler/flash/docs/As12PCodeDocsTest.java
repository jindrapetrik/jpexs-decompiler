/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License, version 3 or later.
 */
package com.jpexs.decompiler.flash.docs;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class As12PCodeDocsTest {

    @Test
    public void testUnknownActionSignatureAndHighlight() {
        String docs = As12PCodeDocs.getDocsForIns("Unknown_a5", true, true, false, 1);
        assertTrue(docs.contains("<span class=\"instruction-code\">0xa5</span>"));
        assertTrue(docs.contains("<strong class=\"instruction-name\">Unknown_a5</strong>"));
        assertTrue(docs.matches("(?s).*<strong class=\"selected-operand\">[, ]*byte2:UI8</strong>.*"));
        assertTrue(docs.contains("byte1:UI8"));
        assertTrue(docs.contains("</strong>, ..."));
        assertTrue(docs.contains(As12PCodeDocs.getProperty("action.unknown.shortDescription")));
        assertTrue(docs.contains(As12PCodeDocs.getProperty("action.unknown.description")));
    }

    @Test
    public void testUnknownActionCacheKeepsNameAndTheme() {
        String first = As12PCodeDocs.getDocsForIns("Unknown_a5", true, true, false, -1);
        String second = As12PCodeDocs.getDocsForIns("Unknown_80", true, true, false, -1);
        String third = As12PCodeDocs.getDocsForIns("Unknown_A5", true, true, true, -1);
        String operands = "<span class=\"instruction-operands\">byte1:UI8, byte2:UI8, ...</span>";
        assertTrue(first.contains(operands));
        assertTrue(second.contains(operands));
        assertTrue(second.contains("<strong class=\"instruction-name\">Unknown_80</strong>"));
        assertTrue(third.contains("<strong class=\"instruction-name\">Unknown_A5</strong>"));
        assertTrue(third.contains("<body class=\"standalonenight\">"));
        assertTrue(third.contains(operands));
        assertFalse(first.contains("<strong class=\"selected-operand\">"));
    }

    @DataProvider(name = "shortUnknownActions")
    public Object[][] shortUnknownActions() {
        return new Object[][]{{"Unknown_01"}, {"Unknown_7f"}};
    }

    @Test(dataProvider = "shortUnknownActions")
    public void testShortUnknownActionHasNoOperands(String name) {
        String docs = As12PCodeDocs.getDocsForIns(name, true, false, false, 1);
        assertTrue(docs.contains("<strong class=\"instruction-name\">" + name + "</strong>"));
        assertTrue(docs.contains("<span class=\"instruction-operands\"></span>"));
        assertFalse(docs.contains("byte1:UI8"));
        assertTrue(docs.contains(As12PCodeDocs.getProperty("action.unknown.description.noOperands")));
        assertFalse(docs.contains(As12PCodeDocs.getProperty("action.unknown.description")));
    }

    @Test
    public void testKnownActionDocsRemainAvailable() {
        String docs = As12PCodeDocs.getDocsForIns("Jump", true, false, false, -1);
        assertTrue(docs.contains("<strong class=\"instruction-name\">Jump</strong>"));
        assertTrue(docs.contains("<span class=\"instruction-code\">0x99</span>"));
        assertTrue(docs.contains(As12PCodeDocs.getProperty("action.jump.operands")));
    }

    @Test
    public void testUnsupportedNamesHaveNoDocs() {
        assertNull(As12PCodeDocs.getDocsForIns("Unknown_zz", true, false, false, -1));
        assertNull(As12PCodeDocs.getDocsForIns("Unknown_a500", true, false, false, -1));
        assertNull(As12PCodeDocs.getDocsForIns("MissingInstruction", true, false, false, -1));
    }
}
