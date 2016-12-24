package com.jpexs.decompiler.flash.importers;

public class As3ScriptReplaceExceptionItem {

    private String file;
    private int line;
    private int col;
    private String message;

    public static final int COL_UNKNOWN = -1;
    public static final int LINE_UNKNOWN = -1;

    public As3ScriptReplaceExceptionItem(String file, String message, int line) {
        this(file, message, line, COL_UNKNOWN);
    }

    public As3ScriptReplaceExceptionItem(String file, String message) {
        this(file, message, LINE_UNKNOWN, COL_UNKNOWN);
    }

    public As3ScriptReplaceExceptionItem(String file, String message, int line, int col) {
        this.file = file;
        this.line = line;
        this.col = col;
        this.message = message;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return (file != null ? "" + file : "") + (line != LINE_UNKNOWN ? ("(" + line + ")") : "") + (col != COL_UNKNOWN ? (" col: " + col) : "");
    }

}
