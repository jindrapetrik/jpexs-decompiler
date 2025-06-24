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
package com.jpexs.decompiler.flash.abc.avm2.deobfuscation;

/**
 * Level of deobfuscation enum.
 *
 * @author JPEXS
 */
public enum DeobfuscationLevel {

    LEVEL_REMOVE_DEAD_CODE(1),
    LEVEL_REMOVE_TRAPS(2);

    /**
     * Level of deobfuscation as number
     */
    private final int level;

    /**
     * Get level of deobfuscation as number
     *
     * @return Level of deobfuscation as number
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get deobfuscation level by level number.
     *
     * @param level Level number
     * @return Deobfuscation level or null if not found
     */
    public static DeobfuscationLevel getByLevel(int level) {
        switch (level) {
            case 1:
                return LEVEL_REMOVE_DEAD_CODE;
            case 2:
                return LEVEL_REMOVE_TRAPS;
        }

        return null;
    }

    DeobfuscationLevel(int level) {
        this.level = level;
    }
}
