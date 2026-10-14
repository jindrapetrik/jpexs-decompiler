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
 * A frame label or a scene; the last descriptor word is preserved.
 */
public final class IggyNamedFrame {

    private final String name;
    private final long frame;
    private final long reserved;

    public IggyNamedFrame(String name, long frame, long reserved) {
        this.name = name;
        this.frame = frame;
        this.reserved = reserved;
    }

    public String getName() {
        return name;
    }

    public long getFrame() {
        return frame;
    }

    public long getReserved() {
        return reserved;
    }
}
