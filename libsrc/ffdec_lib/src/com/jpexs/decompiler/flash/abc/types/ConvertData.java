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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * Data for conversion.
 *
 * @author JPEXS
 */
public class ConvertData {

    public int deobfuscationMode;

    public Map<TraitSlotConst, AssignedValue> assignedValues = new HashMap<>();

    public boolean thisHasDefaultToPrimitive;

    public boolean ignoreFrameScripts;

    public boolean exportEmbed;

    public boolean exportEmbedFlaMode;

    public String assetsDir = "/_assets/";

    public ConvertData() {
        deobfuscationMode = Configuration.autoDeobfuscate.get() ? 1 : 0;
    }
}
