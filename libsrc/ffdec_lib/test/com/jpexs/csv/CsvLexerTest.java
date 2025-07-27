/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.csv;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class CsvLexerTest {

    @Test
    public void testNoNewlineAtTheEnd() throws Exception {
        CsvLexer lexer = new CsvLexer("a;b;c\r\nd;e;f");
        CsvRow row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("a", "b", "c"));
        row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("d", "e", "f"));
        row = lexer.yylex();
        Assert.assertNull(row);
    }

    @Test
    public void testNewlineAtTheEnd() throws Exception {
        CsvLexer lexer = new CsvLexer("a;b;c\r\nd;e;f\r\n");
        CsvRow row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("a", "b", "c"));
        row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("d", "e", "f"));
        row = lexer.yylex();
        Assert.assertNull(row);
    }

    @Test
    public void testDifferentColumns() throws Exception {
        CsvLexer lexer = new CsvLexer("a;b;c\r\nd;e");
        CsvRow row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("a", "b", "c"));
        row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("d", "e"));
        row = lexer.yylex();
        Assert.assertNull(row);
    }

    @Test
    public void testQuotes() throws Exception {
        CsvLexer lexer = new CsvLexer("a;\"b b2; b3\";c\r\nd;e;f");
        CsvRow row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("a", "b b2; b3", "c"));
        row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("d", "e", "f"));
        row = lexer.yylex();
        Assert.assertNull(row);
    }

    @Test
    public void testQuotesInQuotes() throws Exception {
        CsvLexer lexer = new CsvLexer("a;\"b \"\"b2\"\" b3\";c\r\nd;e;f");
        CsvRow row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("a", "b \"b2\" b3", "c"));
        row = lexer.yylex();
        Assert.assertEquals(row.values, Arrays.asList("d", "e", "f"));
        row = lexer.yylex();
        Assert.assertNull(row);
    }
}
