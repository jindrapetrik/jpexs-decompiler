/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serialized dictionary object and its local relocation graph.
 */
public final class IggyObjectData {

    public final byte[] data;
    public final List<IggyFixup.Field> fields;
    public final Map<Long, Integer> characters;
    public final long address;
    public final int headerSize;
    public final int type;

    public IggyObjectData(byte[] data, List<IggyFixup.Field> fields,
            Map<Long, Integer> characters, long address, int headerSize, int type) {
        this.data = data.clone();
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        this.characters = Collections.unmodifiableMap(new LinkedHashMap<>(characters));
        this.address = address;
        this.headerSize = headerSize;
        this.type = type;
    }
}
