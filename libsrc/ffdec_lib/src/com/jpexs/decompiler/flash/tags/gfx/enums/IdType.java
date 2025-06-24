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
package com.jpexs.decompiler.flash.tags.gfx.enums;

import java.lang.reflect.Field;

/**
 * Type of Id.
 *
 * @author JPEXS
 */
public class IdType {

    private static final int IDTYPE_BIT_SWF = 0; // Id comes from SWF File.
    private static final int IDTYPE_BIT_STATIC = 1; // Id assigned uniquely during loading.
    private static final int IDTYPE_BIT_EXPORT = 2; // Id assigned uniquely during export.

    public static final int IDTYPE_NONE = 0;
    public static final int IDTYPE_INTERNALCONSTANT = 0 | IDTYPE_BIT_STATIC;
    public static final int IDTYPE_GRADIENTIMAGE = 4 | IDTYPE_BIT_STATIC;
    public static final int IDTYPE_DYNFONTIMAGE = 8 | IDTYPE_BIT_STATIC;
    public static final int IDTYPE_FONTIMAGE = 4 | IDTYPE_BIT_EXPORT;

    public static String idTypeToString(int idType) {
        try {
            for (Field f : IdType.class.getDeclaredFields()) {
                if (f.getName().startsWith("IDTYPE_BIT_")) {
                    continue;
                }
                if (f.getInt(IdType.class) == idType) {
                    return f.getName().substring(7); //strip "IDTYPE_" prefix
                }
            }
        } catch (IllegalAccessException iae) {
            return "UNKNOWN";
        }
        return "UNKNOWN";
    }
}
