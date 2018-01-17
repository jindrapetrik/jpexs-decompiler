/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.parser.pcode;

import com.jpexs.decompiler.flash.abc.types.Decimal;
import com.jpexs.decompiler.flash.abc.types.Float4;

/**
 *
 * @author JPEXS
 */
public interface MissingSymbolHandler {

    public boolean missingString(String value);

    public boolean missingInt(long value);

    public boolean missingUInt(long value);

    public boolean missingDouble(double value);

    public boolean missingFloat(float value);

    public boolean missingFloat4(Float4 value);

    public boolean missingDecimal(Decimal value);
}
