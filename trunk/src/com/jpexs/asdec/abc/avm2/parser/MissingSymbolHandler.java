/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.parser;


public interface MissingSymbolHandler {
    public boolean missingString(String value);

    public boolean missingInt(long value);

    public boolean missingUInt(long value);

    public boolean missingDouble(double value);

}
