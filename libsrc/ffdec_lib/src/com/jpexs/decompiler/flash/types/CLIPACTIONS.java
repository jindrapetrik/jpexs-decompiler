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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Define event handlers for a sprite character.
 *
 * @author JPEXS
 */
public class CLIPACTIONS implements Serializable {

    /**
     * Reserved
     */
    @Reserved
    @SWFType(BasicType.UI16)
    public int reserved;

    /**
     * All events used in these clip actions
     */
    public CLIPEVENTFLAGS allEventFlags;

    /**
     * Individual event handlers
     */
    public List<CLIPACTIONRECORD> clipActionRecords = new ArrayList<>();

    /**
     * Constructor
     */
    public CLIPACTIONS() {

    }

    /**
     * Calculates allEventFlags
     */
    public void calculateAllEventFlags() {

        allEventFlags = new CLIPEVENTFLAGS();
        Field[] fields = CLIPEVENTFLAGS.class.getDeclaredFields();
        try {
            for (Field f : fields) {
                if (!f.getName().startsWith("clipEvent")) {
                    continue;
                }
                f.set(allEventFlags, false);
                for (CLIPACTIONRECORD car : clipActionRecords) {
                    if (f.getBoolean(car.eventFlags)) {
                        f.set(allEventFlags, true);
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(CLIPACTIONS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
