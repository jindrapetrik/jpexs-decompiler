/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.console.commands.types;

/**
 *
 * @author JPEXS
 */
public class Range {

    public Integer min;

    public Integer max;

    public Range(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    public boolean contains(int index) {
        int minimum = min == null ? Integer.MIN_VALUE : min;
        int maximum = max == null ? Integer.MAX_VALUE : max;

        return index >= minimum && index <= maximum;
    }
}
