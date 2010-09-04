/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.parser;


public class ParseException extends Exception {
    public long line;
    public String text;

    public ParseException(String text, long line) {
        super("ParseException:" + text + " on line " + line);
        this.line = line;
        this.text = text;
    }
}