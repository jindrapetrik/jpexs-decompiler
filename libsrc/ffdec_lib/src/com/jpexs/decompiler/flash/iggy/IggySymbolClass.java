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

/**
 * General object-to-AS3-class binding, not a font-specific record.
 */
public final class IggySymbolClass {

    private final long objectAddress;
    private final String name;
    private final long characterId;
    private final long reserved;

    public IggySymbolClass(long objectAddress, String name, long characterId, long reserved) {
        this.objectAddress = objectAddress;
        this.name = name;
        this.characterId = characterId;
        this.reserved = reserved;
    }

    public long getObjectAddress() {
        return objectAddress;
    }

    public String getName() {
        return name;
    }

    public long getCharacterId() {
        return characterId;
    }

    public long getReserved() {
        return reserved;
    }
}
